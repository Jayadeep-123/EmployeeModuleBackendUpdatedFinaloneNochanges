
package com.employee.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employee.dto.BackToCampusDTO;
import com.employee.dto.SalaryInfoDTO;
import com.employee.entity.BankDetails;
import com.employee.entity.CostCenter;
import com.employee.entity.EmpGrade;
import com.employee.entity.EmpPaymentType;
import com.employee.entity.EmpPfDetails;
import com.employee.entity.EmpSalaryInfo;
import com.employee.entity.EmpStructure;
import com.employee.entity.Employee;
import com.employee.entity.EmployeeCheckListStatus;
import com.employee.exception.ResourceNotFoundException;
import com.employee.repository.BankDetailsRepository;
import com.employee.repository.CostCenterRepository;
import com.employee.repository.EmpGradeRepository;
import com.employee.repository.EmpPaymentTypeRepository;
import com.employee.repository.EmpPfDetailsRepository;
import com.employee.repository.EmpAppCheckListDetlRepository;
import com.employee.repository.EmpSalaryInfoRepository;
import com.employee.repository.EmpStructureRepository;
import com.employee.repository.EmployeeRepository;
import com.employee.repository.EmployeeCheckListStatusRepository;

@Service
@Transactional
public class EmpSalaryInfoService {

	private static final Logger logger = LoggerFactory.getLogger(EmpSalaryInfoService.class);

	@Autowired
	private EmpSalaryInfoRepository empSalaryInfoRepository;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private BankDetailsRepository bankDetailsRepository;

	@Autowired
	private EmpPaymentTypeRepository empPaymentTypeRepository;
	
	@Autowired
	private EmpStructureRepository empStructureRepository;
	
	@Autowired
	private CostCenterRepository costCenterRepository;
	
	@Autowired
	private EmpGradeRepository empGradeRepository;
	
	@Autowired
	private EmpPfDetailsRepository empPfDetailsRepository;
	
	@Autowired
	private EmployeeCheckListStatusRepository employeeCheckListStatusRepository;
	
	@Autowired
	private EmpAppCheckListDetlRepository empAppCheckListDetlRepository;

	/**
	 * Create salary info based on temp_payroll_id and forward to Central Office
	 * 
	 * IMPORTANT: This method ONLY works when employee current status is "Pending at DO" or "Back to DO"
	 * This allows forwarding to CO again after CO rejection (when status is "Back to DO")
	 * If employee has any other status, this method will throw an error
	 * 
	 * Flow:
	 * 1. Find employee by temp_payroll_id (emp_id will be fetched from Employee table)
	 * 2. Validate that current status is "Pending at DO" or "Back to DO" (required)
	 * 3. Get emp_payment_type_id from BankDetails table for that emp_id
	 * 4. Check if EmpSalaryInfo already exists for this emp_id:
	 *    - If exists: Update existing record (prevents duplicates)
	 *    - If not exists: Create new record
	 * 5. Save/Update PF/ESI/UAN details in EmpPfDetails table
	 * 6. Update checklist in Employee table (emp_app_check_list_detl_id)
	 * 7. Update org_id (Company/Organization) in Employee table (if provided)
	 * 8. Update app status to "Pending at CO" (when forwarding to Central Office)
	 * 9. Clear remarks
	 * 
	 * Returns DTO with all the saved data (no entity relationships to avoid circular references)
	 * @throws ResourceNotFoundException if employee status is not "Pending at DO" or "Back to DO"
	 */
	public SalaryInfoDTO createSalaryInfo(SalaryInfoDTO salaryInfoDTO) {
		// Validation: Check if tempPayrollId is provided
		if (salaryInfoDTO.getTempPayrollId() == null || salaryInfoDTO.getTempPayrollId().trim().isEmpty()) {
			throw new ResourceNotFoundException("tempPayrollId is required. Please provide a valid temp_payroll_id.");
		}
		
		// Validation: Check required salary fields
		if (salaryInfoDTO.getMonthlyCtc() == null) {
			throw new ResourceNotFoundException("monthlyCtc is required (NOT NULL column)");
		}
		if (salaryInfoDTO.getYearlyCtc() == null) {
			throw new ResourceNotFoundException("yearlyCtc is required (NOT NULL column)");
		}
		if (salaryInfoDTO.getEmpStructureId() == null) {
			throw new ResourceNotFoundException("empStructureId is required (NOT NULL column)");
		}
		
		logger.info("Creating salary info for temp_payroll_id: {}", salaryInfoDTO.getTempPayrollId());

		// Step 1: Find employee by temp_payroll_id
		Employee employee = employeeRepository.findByTempPayrollId(salaryInfoDTO.getTempPayrollId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Employee not found with temp_payroll_id: '" + salaryInfoDTO.getTempPayrollId() + 
						"'. Please verify the temp_payroll_id is correct and the employee exists in the system."));

		Integer empId = employee.getEmp_id();
		logger.info("Found employee - temp_payroll_id: {}, emp_id: {}", 
				employee.getTempPayrollId(), empId);
		
		// Additional validation: Check if employee is active
		if (employee.getIs_active() != 1) {
			throw new ResourceNotFoundException(
					"Employee with temp_payroll_id: '" + salaryInfoDTO.getTempPayrollId() + 
					"' is not active. emp_id: " + empId);
		}
		
		// Validation: Check if current status is "Pending at DO" or "Back to DO" - forward to CO works for both statuses
		// This allows forwarding again after CO rejection (when status is "Back to DO")
		if (employee.getEmp_check_list_status_id() == null) {
			throw new ResourceNotFoundException(
					"Cannot forward employee to Central Office: Employee (emp_id: " + empId + 
					", temp_payroll_id: '" + salaryInfoDTO.getTempPayrollId() + 
					"') does not have a status set. This method only works when employee status is 'Pending at DO' or 'Back to DO'.");
		}
		
		String currentStatusName = employee.getEmp_check_list_status_id().getCheck_app_status_name();
		if (!"Pending at DO".equals(currentStatusName) && !"Back to DO".equals(currentStatusName)) {
			throw new ResourceNotFoundException(
					"Cannot forward employee to Central Office: Current employee status is '" + currentStatusName + 
					"' (emp_id: " + empId + ", temp_payroll_id: '" + salaryInfoDTO.getTempPayrollId() + 
					"'). This method only works when employee status is 'Pending at DO' or 'Back to DO'.");
		}
		
		logger.info("Employee (emp_id: {}) current status is '{}', proceeding with forward to Central Office", empId, currentStatusName);

		// Step 2: Get emp_payment_type_id from BankDetails table (where emp_id matches)
		EmpPaymentType empPaymentType = null;
		List<BankDetails> bankDetailsList = bankDetailsRepository.findByEmpId_Emp_id(empId);
		
		if (bankDetailsList != null && !bankDetailsList.isEmpty()) {
			logger.info("Found {} BankDetails record(s) for emp_id: {}", bankDetailsList.size(), empId);
			// Get payment type from bank details - prefer salary account if available
			for (BankDetails bankDetail : bankDetailsList) {
				logger.info("Checking BankDetails - acc_type: {}, emp_payment_type_id: {}", 
						bankDetail.getAccType(), 
						bankDetail.getEmpPaymentType() != null ? bankDetail.getEmpPaymentType().getEmp_payment_type_id() : "null");
				
				if (bankDetail.getEmpPaymentType() != null) {
					// Reload the entity from repository to ensure it's managed
					Integer paymentTypeId = bankDetail.getEmpPaymentType().getEmp_payment_type_id();
					empPaymentType = empPaymentTypeRepository.findById(paymentTypeId)
							.orElse(null);
					
					if (empPaymentType != null) {
						logger.info("Loaded emp_payment_type_id: {} from repository (acc_type: {}) for emp_id: {}", 
								empPaymentType.getEmp_payment_type_id(), bankDetail.getAccType(), empId);
						// Prefer salary account if available
						if ("SALARY".equalsIgnoreCase(bankDetail.getAccType())) {
							logger.info("Using emp_payment_type_id from SALARY account type");
							break;
						}
					} else {
						logger.warn("EmpPaymentType with ID {} not found in repository", paymentTypeId);
					}
				}
			}
			
			if (empPaymentType == null) {
				logger.warn("No emp_payment_type_id found in any BankDetails records for emp_id: {}. Will be set to null.", empId);
			}
		} else {
			logger.warn("No BankDetails found for emp_id: {}. emp_payment_type_id will be null.", empId);
		}

		// Step 3: Check if EmpSalaryInfo already exists for this emp_id, if yes update, else create new
		Optional<EmpSalaryInfo> existingSalaryInfoOpt = empSalaryInfoRepository.findByEmpIdAndIsActive(empId, 1);
		EmpSalaryInfo empSalaryInfo;
		
		if (existingSalaryInfoOpt.isPresent()) {
			// Update existing record
			empSalaryInfo = existingSalaryInfoOpt.get();
			logger.info("Found existing EmpSalaryInfo record (emp_sal_info_id: {}) for emp_id: {}, updating instead of creating duplicate", 
					empSalaryInfo.getEmpSalInfoId(), empId);
		} else {
			// Create new record
			empSalaryInfo = new EmpSalaryInfo();
			empSalaryInfo.setEmpId(employee); // Set emp_id (FK to Employee table) - fetched from Employee using temp_payroll_id
			logger.info("No existing EmpSalaryInfo found for emp_id: {}, creating new record", empId);
		}
		
		// Set temp_payroll_id from Employee table (store in temp_payroll_id column)
		if (employee.getTempPayrollId() != null && !employee.getTempPayrollId().trim().isEmpty()) {
			empSalaryInfo.setTempPayrollId(employee.getTempPayrollId());
			logger.info("Storing temp_payroll_id '{}' in temp_payroll_id column", employee.getTempPayrollId());
		} else {
			throw new ResourceNotFoundException("Employee with temp_payroll_id: '" + salaryInfoDTO.getTempPayrollId() + 
					"' does not have temp_payroll_id set in Employee table.");
		}
		
		// Set payroll_id - keep existing value if updating, or set to null if creating new
		if (empSalaryInfo.getPayrollId() == null) {
			empSalaryInfo.setPayrollId(null);
			logger.info("Setting payroll_id to null (will be updated later when available)");
		} else {
			logger.info("Keeping existing payroll_id: {} (not overwriting)", empSalaryInfo.getPayrollId());
		}
		
		// Set emp_payment_type_id from BankDetails (for that emp_id)
		empSalaryInfo.setEmpPaymentType(empPaymentType);
		if (empPaymentType != null) {
			logger.info("Storing emp_payment_type_id: {} in EmpSalaryInfo (retrieved from BankDetails)", 
					empPaymentType.getEmp_payment_type_id());
		} else {
			logger.warn("emp_payment_type_id is null - not found in BankDetails for emp_id: {}", empId);
		}
		empSalaryInfo.setMonthlyCtc(salaryInfoDTO.getMonthlyCtc()); // From swagger
		empSalaryInfo.setCtcWords(salaryInfoDTO.getCtcWords()); // From swagger (optional)
		empSalaryInfo.setYearlyCtc(salaryInfoDTO.getYearlyCtc()); // From swagger
		
		// Set emp_structure_id as relationship (fetch from master table)
		if (salaryInfoDTO.getEmpStructureId() != null) {
			EmpStructure empStructure = empStructureRepository.findByIdAndIsActive(salaryInfoDTO.getEmpStructureId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active EmpStructure not found with ID: " + salaryInfoDTO.getEmpStructureId()));
			empSalaryInfo.setEmpStructure(empStructure);
			logger.info("Set emp_structure_id: {} (structure name: {})", empStructure.getEmpStructureId(), empStructure.getStructreName());
		} else {
			throw new ResourceNotFoundException("empStructureId is required (NOT NULL column)");
		}
		
		// Set grade_id as relationship (optional)
		if (salaryInfoDTO.getGradeId() != null) {
			EmpGrade empGrade = empGradeRepository.findByIdAndIsActive(salaryInfoDTO.getGradeId(), 1)
					.orElseThrow(() -> new ResourceNotFoundException("Active EmpGrade not found with ID: " + salaryInfoDTO.getGradeId()));
			empSalaryInfo.setGrade(empGrade);
			logger.info("Set grade_id: {} (grade name: {})", empGrade.getEmpGradeId(), empGrade.getGradeName());
		} else {
			empSalaryInfo.setGrade(null);
			logger.info("grade_id is null (optional field)");
		}
		
		// Set cost_center_id as relationship (optional)
		if (salaryInfoDTO.getCostCenterId() != null) {
			CostCenter costCenter = costCenterRepository.findById(salaryInfoDTO.getCostCenterId())
					.orElseThrow(() -> new ResourceNotFoundException("CostCenter not found with ID: " + salaryInfoDTO.getCostCenterId()));
			empSalaryInfo.setCostCenter(costCenter);
			logger.info("Set cost_center_id: {} (cost center name: {})", costCenter.getCostCenterId(), costCenter.getCostCenterName());
		} else {
			empSalaryInfo.setCostCenter(null);
			logger.info("cost_center_id is null (optional field)");
		}
		
		// Set is_pf_eligible (required NOT NULL, convert Boolean to Integer: true = 1, false = 0)
		if (salaryInfoDTO.getIsPfEligible() != null) {
			empSalaryInfo.setIsPfEligible(salaryInfoDTO.getIsPfEligible() ? 1 : 0);
		} else {
			empSalaryInfo.setIsPfEligible(0); // Default to 0 (false) if not provided
		}
		logger.info("Set is_pf_eligible: {} (from boolean: {})", empSalaryInfo.getIsPfEligible(), salaryInfoDTO.getIsPfEligible());
		
		// Set is_esi_eligible (required NOT NULL, convert Boolean to Integer: true = 1, false = 0)
		if (salaryInfoDTO.getIsEsiEligible() != null) {
			empSalaryInfo.setIsEsiEligible(salaryInfoDTO.getIsEsiEligible() ? 1 : 0);
		} else {
			empSalaryInfo.setIsEsiEligible(0); // Default to 0 (false) if not provided
		}
		logger.info("Set is_esi_eligible: {} (from boolean: {})", empSalaryInfo.getIsEsiEligible(), salaryInfoDTO.getIsEsiEligible());
		
		// Set isActive to 1 (for both new and existing records)
		empSalaryInfo.setIsActive(1);

		// Step 4: Save to salary table
		// Log before save to verify relationships are set
		logger.info("Before save - emp_payment_type_id: {}, grade_id: {}", 
				empSalaryInfo.getEmpPaymentType() != null ? empSalaryInfo.getEmpPaymentType().getEmp_payment_type_id() : "null",
				empSalaryInfo.getGrade() != null ? empSalaryInfo.getGrade().getEmpGradeId() : "null");
		
		EmpSalaryInfo savedSalaryInfo = empSalaryInfoRepository.save(empSalaryInfo);
		
		// Flush to ensure data is persisted immediately
		empSalaryInfoRepository.flush();
		
		// Log after save to verify relationships are saved
		logger.info("After save - emp_sal_info_id: {}, emp_id: {}, emp_payment_type_id: {}, grade_id: {}", 
				savedSalaryInfo.getEmpSalInfoId(), 
				empId,
				savedSalaryInfo.getEmpPaymentType() != null ? savedSalaryInfo.getEmpPaymentType().getEmp_payment_type_id() : "null",
				savedSalaryInfo.getGrade() != null ? savedSalaryInfo.getGrade().getEmpGradeId() : "null");

		// Step 4.5: Update PF/ESI/UAN details in EmpPfDetails table
		// First, verify employee exists based on temp_payroll_id (already validated above, but double-check emp_id exists)
		if (empId == null || empId <= 0) {
			throw new ResourceNotFoundException(
					"Cannot update PF details: Employee not found or invalid emp_id for temp_payroll_id: '" + 
					salaryInfoDTO.getTempPayrollId() + "'");
		}
		
		// Verify employee still exists in database
		Employee employeeForPf = employeeRepository.findById(empId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Cannot update PF details: Employee with emp_id: " + empId + 
						" (temp_payroll_id: '" + salaryInfoDTO.getTempPayrollId() + "') does not exist in the system."));
		
		// Find existing EmpPfDetails or create new one (only if employee exists)
		EmpPfDetails empPfDetails = empPfDetailsRepository.findByEmployeeId(empId)
				.orElse(new EmpPfDetails());
		
		boolean pfDetailsNeedsSave = false;
		
		// Save PF data if provided (regardless of isPfEligible value - 0 or 1)
		if (salaryInfoDTO.getPfNo() != null && !salaryInfoDTO.getPfNo().trim().isEmpty()) {
			empPfDetails.setPf_no(salaryInfoDTO.getPfNo());
			pfDetailsNeedsSave = true;
			logger.info("Set PF number: {} for emp_id: {} (isPfEligible: {})", 
					salaryInfoDTO.getPfNo(), empId, empSalaryInfo.getIsPfEligible());
		}
		if (salaryInfoDTO.getPfJoinDate() != null) {
			empPfDetails.setPf_join_date(salaryInfoDTO.getPfJoinDate());
			pfDetailsNeedsSave = true;
			logger.info("Set PF join date: {} for emp_id: {} (isPfEligible: {})", 
					salaryInfoDTO.getPfJoinDate(), empId, empSalaryInfo.getIsPfEligible());
		}
		
		// Save ESI data if provided (regardless of isEsiEligible value - 0 or 1)
		if (salaryInfoDTO.getEsiNo() != null) {
			empPfDetails.setEsi_no(salaryInfoDTO.getEsiNo());
			pfDetailsNeedsSave = true;
			logger.info("Set ESI number: {} for emp_id: {} (isEsiEligible: {})", 
					salaryInfoDTO.getEsiNo(), empId, empSalaryInfo.getIsEsiEligible());
		}
		
		// Save record if PF/ESI/UAN data is provided OR if eligible (for tracking)
		if (empSalaryInfo.getIsPfEligible() != null && empSalaryInfo.getIsPfEligible() == 1) {
			pfDetailsNeedsSave = true; // Save record when PF eligible (even if data not provided)
		}
		if (empSalaryInfo.getIsEsiEligible() != null && empSalaryInfo.getIsEsiEligible() == 1) {
			pfDetailsNeedsSave = true; // Save record when ESI eligible (even if data not provided)
		}
		
		// Always set UAN number if provided (no eligibility check)
		if (salaryInfoDTO.getUanNo() != null) {
			empPfDetails.setUan_no(salaryInfoDTO.getUanNo());
			pfDetailsNeedsSave = true;
			logger.info("Set UAN number: {} for emp_id: {}", salaryInfoDTO.getUanNo(), empId);
		}
		
		// Save EmpPfDetails if any changes were made (only if employee exists)
		if (pfDetailsNeedsSave) {
			// Use the verified employee object
			empPfDetails.setEmployee_id(employeeForPf);
			empPfDetails.setIs_active(1);
			if (empPfDetails.getEmp_pf_esi_uan_info_id() == 0) {
				// New record - set created_by
				empPfDetails.setCreated_by(employeeForPf.getCreated_by());
			}
			empPfDetailsRepository.save(empPfDetails);
			logger.info("Saved/Updated PF/ESI/UAN details for emp_id: {} (temp_payroll_id: '{}')", 
					empId, salaryInfoDTO.getTempPayrollId());
		} else {
			logger.info("No PF/ESI/UAN details to save for emp_id: {} (temp_payroll_id: '{}')", 
					empId, salaryInfoDTO.getTempPayrollId());
		}

		// Step 5: Update checklist in Employee table (emp_app_check_list_detl_id)
		boolean needsUpdate = false;
		if (salaryInfoDTO.getCheckListIds() != null && !salaryInfoDTO.getCheckListIds().trim().isEmpty()) {
			// Validate checklist IDs before saving
			validateCheckListIds(salaryInfoDTO.getCheckListIds());
			employee.setEmp_app_check_list_detl_id(salaryInfoDTO.getCheckListIds());
			needsUpdate = true;
			logger.info("Setting checklist IDs for employee (emp_id: {}): {}", empId, salaryInfoDTO.getCheckListIds());
		}
		
		// Step 6: Update org_id (Company/Organization) in Employee table (if provided)
		if (salaryInfoDTO.getOrgId() != null) {
			employee.setOrg_id(salaryInfoDTO.getOrgId());
			needsUpdate = true;
			logger.info("Updated org_id (Company) for employee (emp_id: {}): {}", empId, salaryInfoDTO.getOrgId());
		}
		
		// Step 7: Update app status to "Pending at CO" when forwarding to Central Office
		EmployeeCheckListStatus pendingAtCOStatus = employeeCheckListStatusRepository.findByCheck_app_status_name("Pending at CO")
				.orElseThrow(() -> new ResourceNotFoundException("EmployeeCheckListStatus with name 'Pending at CO' not found"));
		employee.setEmp_check_list_status_id(pendingAtCOStatus);
		needsUpdate = true;
		logger.info("Updated employee (emp_id: {}) app status to 'Pending at CO' (ID: {}) when forwarding to central office", empId, pendingAtCOStatus.getEmp_app_status_id());
		
		// Clear remarks when forwarding to central office (after rectification)
		employee.setRemarks(null);
		needsUpdate = true;
		logger.info("Cleared remarks for employee (emp_id: {}) when forwarding to central office", empId);
		
		// Save employee updates (checklist, status, and cleared remarks)
		if (needsUpdate) {
			employeeRepository.save(employee);
			if (salaryInfoDTO.getCheckListIds() != null && !salaryInfoDTO.getCheckListIds().trim().isEmpty()) {
				logger.info("Updated employee (emp_id: {}) with checklist IDs: {}", 
						empId, salaryInfoDTO.getCheckListIds());
			}
		}
		
		// Return the DTO with all the data (no entity relationships to avoid circular references)
		// The DTO already contains all the input data, and we've updated the employee with checklist IDs
		// No need to refresh - we already have the checklist IDs in the DTO and employee object
		// The checklist IDs are already set in salaryInfoDTO from the input, so just return it
		return salaryInfoDTO;
	}

	/**
	 * Get salary info by temp_payroll_id (returns entity - for internal use)
	 */
	public EmpSalaryInfo getSalaryInfoByTempPayrollId(String tempPayrollId) {
		Employee employee = employeeRepository.findByTempPayrollId(tempPayrollId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Employee not found with temp_payroll_id: " + tempPayrollId));

		return empSalaryInfoRepository.findByEmpIdAndIsActive(employee.getEmp_id(), 1)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Salary info not found for employee with temp_payroll_id: " + tempPayrollId));
	}
	
	/**
	 * Get salary info by temp_payroll_id and return as DTO (for API response)
	 */
	public SalaryInfoDTO getSalaryInfoByTempPayrollIdAsDTO(String tempPayrollId) {
		Employee employee = employeeRepository.findByTempPayrollId(tempPayrollId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Employee not found with temp_payroll_id: " + tempPayrollId));

		EmpSalaryInfo empSalaryInfo = empSalaryInfoRepository.findByEmpIdAndIsActive(employee.getEmp_id(), 1)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Salary info not found for employee with temp_payroll_id: " + tempPayrollId));
		
		// Convert entity to DTO
		SalaryInfoDTO salaryInfoDTO = new SalaryInfoDTO();
		salaryInfoDTO.setTempPayrollId(tempPayrollId);
		salaryInfoDTO.setMonthlyCtc(empSalaryInfo.getMonthlyCtc());
		salaryInfoDTO.setCtcWords(empSalaryInfo.getCtcWords());
		salaryInfoDTO.setYearlyCtc(empSalaryInfo.getYearlyCtc());
		salaryInfoDTO.setEmpStructureId(empSalaryInfo.getEmpStructure() != null ? empSalaryInfo.getEmpStructure().getEmpStructureId() : null);
		salaryInfoDTO.setGradeId(empSalaryInfo.getGrade() != null ? empSalaryInfo.getGrade().getEmpGradeId() : null);
		salaryInfoDTO.setCostCenterId(empSalaryInfo.getCostCenter() != null ? empSalaryInfo.getCostCenter().getCostCenterId() : null);
		// Convert Integer to Boolean (1 = true, 0 = false, null = false)
		salaryInfoDTO.setIsPfEligible(empSalaryInfo.getIsPfEligible() != null && empSalaryInfo.getIsPfEligible() == 1);
		salaryInfoDTO.setIsEsiEligible(empSalaryInfo.getIsEsiEligible() != null && empSalaryInfo.getIsEsiEligible() == 1);
		
		// Get PF/ESI/UAN details from EmpPfDetails
		Optional<EmpPfDetails> empPfDetailsOpt = empPfDetailsRepository.findByEmployeeId(employee.getEmp_id());
		if (empPfDetailsOpt.isPresent()) {
			EmpPfDetails empPfDetails = empPfDetailsOpt.get();
			salaryInfoDTO.setPfNo(empPfDetails.getPf_no());
			salaryInfoDTO.setPfJoinDate(empPfDetails.getPf_join_date());
			salaryInfoDTO.setEsiNo(empPfDetails.getEsi_no());
			salaryInfoDTO.setUanNo(empPfDetails.getUan_no());
		}
		
		salaryInfoDTO.setCheckListIds(employee.getEmp_app_check_list_detl_id());
		
		return salaryInfoDTO;
	}
	
	/**
	 * Forward to Central Office
	 * This is an alias/wrapper for createSalaryInfo that explicitly forwards the employee
	 * Sets emp_app_status_id to "Pending at CO" status and clears any previous remarks
	 * 
	 * IMPORTANT: This method ONLY works when employee current status is "Pending at DO" or "Back to DO"
	 * This allows forwarding to CO again after CO rejection (when status is "Back to DO")
	 * If employee has any other status, this method will throw an error
	 * 
	 * @throws ResourceNotFoundException if employee status is not "Pending at DO" or "Back to DO"
	 */
	public SalaryInfoDTO forwardToCentralOffice(SalaryInfoDTO salaryInfoDTO) {
		logger.info("Forwarding employee to Central Office - temp_payroll_id: {}", salaryInfoDTO.getTempPayrollId());
		// createSalaryInfo already handles setting status to "Pending at CO" and clearing remarks
		return createSalaryInfo(salaryInfoDTO);
	}
	
	/**
	 * Back to Campus
	 * Sends employee back to campus for corrections
	 * 
	 * IMPORTANT: This method ONLY works when employee current status is "Pending at DO"
	 * If employee has any other status, this method will throw an error
	 * 
	 * Flow:
	 * 1. Find employee by temp_payroll_id
	 * 2. Validate that current status is "Pending at DO" (required)
	 * 3. Set emp_app_status_id to "Back to Campus" status
	 * 4. Save remarks (reason for sending back)
	 * 5. Optionally update checklist IDs (capture current state similar to forward)
	 * 
	 * @param backToCampusDTO DTO containing tempPayrollId, remarks, and optional checkListIds
	 * @return BackToCampusDTO with the saved data
	 * @throws ResourceNotFoundException if employee status is not "Pending at DO"
	 */
	public BackToCampusDTO backToCampus(BackToCampusDTO backToCampusDTO) {
		// Validation: Check if tempPayrollId is provided
		if (backToCampusDTO.getTempPayrollId() == null || backToCampusDTO.getTempPayrollId().trim().isEmpty()) {
			throw new ResourceNotFoundException("tempPayrollId is required. Please provide a valid temp_payroll_id.");
		}
		
		// Validation: Check if remarks is provided
		if (backToCampusDTO.getRemarks() == null || backToCampusDTO.getRemarks().trim().isEmpty()) {
			throw new ResourceNotFoundException("remarks is required. Please provide a reason for sending back to campus.");
		}
		
		// Validation: Check remarks length (max 250 characters)
		if (backToCampusDTO.getRemarks().length() > 250) {
			throw new IllegalArgumentException("remarks cannot exceed 250 characters. Current length: " + backToCampusDTO.getRemarks().length());
		}
		
		logger.info("Sending employee back to campus - temp_payroll_id: {}, remarks: {}", 
				backToCampusDTO.getTempPayrollId(), backToCampusDTO.getRemarks());
		
		// Step 1: Find employee by temp_payroll_id
		Employee employee = employeeRepository.findByTempPayrollId(backToCampusDTO.getTempPayrollId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Employee not found with temp_payroll_id: '" + backToCampusDTO.getTempPayrollId() + 
						"'. Please verify the temp_payroll_id is correct and the employee exists in the system."));
		
		Integer empId = employee.getEmp_id();
		logger.info("Found employee - temp_payroll_id: {}, emp_id: {}", 
				employee.getTempPayrollId(), empId);
		
		// Additional validation: Check if employee is active
		if (employee.getIs_active() != 1) {
			throw new ResourceNotFoundException(
					"Employee with temp_payroll_id: '" + backToCampusDTO.getTempPayrollId() + 
					"' is not active. emp_id: " + empId);
		}
		
		// Validation: Check if current status is "Pending at DO" - this method only works for "Pending at DO" status
		if (employee.getEmp_check_list_status_id() == null) {
			throw new ResourceNotFoundException(
					"Cannot send employee back to campus: Employee (emp_id: " + empId + 
					", temp_payroll_id: '" + backToCampusDTO.getTempPayrollId() + 
					"') does not have a status set. This method only works when employee status is 'Pending at DO'.");
		}
		
		String currentStatusName = employee.getEmp_check_list_status_id().getCheck_app_status_name();
		if (!"Pending at DO".equals(currentStatusName)) {
			throw new ResourceNotFoundException(
					"Cannot send employee back to campus: Current employee status is '" + currentStatusName + 
					"' (emp_id: " + empId + ", temp_payroll_id: '" + backToCampusDTO.getTempPayrollId() + 
					"'). This method only works when employee status is 'Pending at DO'.");
		}
		
		logger.info("Employee (emp_id: {}) current status is 'Pending at DO', proceeding with back to campus", empId);
		
		// Step 2: Update app status to "Back to Campus"
		EmployeeCheckListStatus backToCampusStatus = employeeCheckListStatusRepository.findByCheck_app_status_name("Back to Campus")
				.orElseThrow(() -> new ResourceNotFoundException("EmployeeCheckListStatus with name 'Back to Campus' not found"));
		employee.setEmp_check_list_status_id(backToCampusStatus);
		logger.info("Updated employee (emp_id: {}) app status to 'Back to Campus' (ID: {})", empId, backToCampusStatus.getEmp_app_status_id());
		
		// Step 3: Save remarks
		employee.setRemarks(backToCampusDTO.getRemarks().trim());
		logger.info("Saved remarks for employee (emp_id: {}): {}", empId, backToCampusDTO.getRemarks());
		
		// Step 4: Optionally update checklist IDs (capture current state similar to forward to central office)
		if (backToCampusDTO.getCheckListIds() != null && !backToCampusDTO.getCheckListIds().trim().isEmpty()) {
			// Validate checklist IDs before saving
			validateCheckListIds(backToCampusDTO.getCheckListIds());
			employee.setEmp_app_check_list_detl_id(backToCampusDTO.getCheckListIds());
			logger.info("Updated checklist IDs for employee (emp_id: {}): {}", empId, backToCampusDTO.getCheckListIds());
		}
		
		// Save employee updates (status, remarks, and optional checklist)
		employeeRepository.save(employee);
		logger.info("Successfully sent employee (emp_id: {}, temp_payroll_id: '{}') back to campus with remarks", 
				empId, backToCampusDTO.getTempPayrollId());
		
		// Return the DTO with saved data
		return backToCampusDTO;
	}
	
	/**
	 * Validate checklist IDs - checks if all IDs in comma-separated string exist in EmpAppCheckListDetl table
	 * @param checkListIds Comma-separated string of checklist IDs (e.g., "1,2,3,4,5,6,7")
	 * @throws ResourceNotFoundException if any checklist ID is invalid or not found
	 */
	private void validateCheckListIds(String checkListIds) {
		if (checkListIds == null || checkListIds.trim().isEmpty()) {
			return; // No validation needed if empty
		}
		
		// Split comma-separated string into individual IDs
		String[] idArray = checkListIds.split(",");
		
		for (String idStr : idArray) {
			idStr = idStr.trim(); // Remove whitespace
			
			if (idStr.isEmpty()) {
				continue; // Skip empty strings
			}
			
			try {
				Integer checklistId = Integer.parseInt(idStr);
				
				// Check if checklist ID exists and is active in EmpAppCheckListDetl table
				empAppCheckListDetlRepository.findByIdAndIsActive(checklistId, 1)
					.orElseThrow(() -> new ResourceNotFoundException(
							"Checklist ID " + checklistId + " not found or inactive in checklist master table. " +
							"Please provide valid checklist IDs. Provided checklist IDs: " + checkListIds));
				
				logger.debug("Validated checklist ID: {} exists and is active", checklistId);
				
			} catch (NumberFormatException e) {
				throw new ResourceNotFoundException(
						"Invalid checklist ID format: '" + idStr + "'. Checklist IDs must be numeric. " +
						"Provided checklist IDs: " + checkListIds);
			}
		}
		
		logger.info("✅ All checklist IDs validated successfully: {}", checkListIds);
	}
}
