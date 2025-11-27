package com.employee.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employee.dto.BasicInfoDTO;
import com.employee.dto.EmployeeOnboardingDTO;
import com.employee.dto.QualificationDTO;
import com.employee.dto.TempPayrollIdResponseDTO;
import com.employee.entity.BankDetails;
import com.employee.entity.EmpaddressInfo;
import com.employee.entity.EmpDetails;
import com.employee.entity.EmpDocuments;
import com.employee.entity.EmpExperienceDetails;
import com.employee.entity.EmpFamilyDetails;
import com.employee.entity.EmpPfDetails;
import com.employee.entity.EmpQualification;
import com.employee.entity.Employee;
import com.employee.entity.EmployeeCheckListStatus;
import com.employee.entity.Campus;
import com.employee.exception.ResourceNotFoundException;
import com.employee.repository.CampusRepository;
import com.employee.repository.EmpaddressInfoRepository;
import com.employee.repository.EmpDetailsRepository;
import com.employee.repository.EmpDocumentsRepository;
import com.employee.repository.EmpPfDetailsRepository;
import com.employee.repository.EmpExperienceDetailsRepository;
import com.employee.repository.EmpFamilyDetailsRepository;
import com.employee.repository.EmpQualificationRepository;
import com.employee.repository.EmployeeCheckListStatusRepository;
import com.employee.repository.EmployeeRepository;
import com.employee.repository.QualificationRepository;
import com.employee.repository.SkillTestDetailsRepository;
import com.employee.repository.BankDetailsRepository;
import com.employee.repository.EmpSubjectRepository;
import com.employee.repository.SubjectRepository;
import com.employee.entity.EmpSubject;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeOnboardingService {

	private static final Logger logger = LoggerFactory.getLogger(EmployeeOnboardingService.class);

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private EmpDetailsRepository empDetailsRepository;

	@Autowired
	private EmpPfDetailsRepository empPfDetailsRepository;

	@Autowired
	private EmpaddressInfoRepository empaddressInfoRepository;

	@Autowired
	private EmpFamilyDetailsRepository empFamilyDetailsRepository;

	@Autowired
	private EmpExperienceDetailsRepository empExperienceDetailsRepository;

	@Autowired
	private EmpQualificationRepository empQualificationRepository;

	@Autowired
	private EmpDocumentsRepository empDocumentsRepository;

	@Autowired
	private BankDetailsRepository bankDetailsRepository;

	@Autowired
	private CampusRepository campusRepository;

	@Autowired
	private SkillTestDetailsRepository skillTestDetailsRepository;

	@Autowired
	private EmployeeCheckListStatusRepository employeeCheckListStatusRepository;

	@Autowired
	private EmployeeValidationService employeeValidationService;

	@Autowired
	private EmployeeEntityPreparationService entityPreparationService;

	@Autowired
	private QualificationRepository qualificationRepository;

	@Autowired
	private EmpSubjectRepository empSubjectRepository;

	@Autowired
	private SubjectRepository subjectRepository;

	/**
	 * Generate or validate temp_payroll_id for NEW Employee
	 * * This method creates a NEW employee and handles temp_payroll_id generation with validation logic:
	 * 1. If tempPayrollId is provided from frontend:
	 * - Check if it exists in SkillTestDetails table
	 * - If exists, use it (don't generate new one)
	 * - If not exists in SkillTestDetails, throw error
	 * * 2. If tempPayrollId is NOT provided from frontend:
	 * - Check aadharNum + phoneNumber in SkillTestDetails table
	 * - If found, cannot generate (employee already exists in SkillTestDetails)
	 * - If not found, generate new tempPayrollId
	 * * 3. Generation logic:
	 * - Get campus code from BasicInfoDTO.campusId
	 * - Format: TEMP{campusCode}{4-digit-number}
	 * - Check max tempPayrollId in BOTH SkillTestDetails and Employee tables
	 * - Generate next number (increment from max)
	 * * @param hrEmployeeId HR Employee ID (emp_id) - used for created_by field (the recruiter creating the new employee)
	 * @param basicInfo BasicInfoDTO containing employee details, campusId, aadharNum, primaryMobileNo, and optional tempPayrollId
	 * @return TempPayrollIdResponseDTO containing tempPayrollId and the auto-generated employee ID
	 */
	@Transactional
	public TempPayrollIdResponseDTO generateOrValidateTempPayrollId(Integer hrEmployeeId, BasicInfoDTO basicInfo) {

		logger.info("Creating NEW employee and generating/validating temp_payroll_id. HR Employee ID (created_by): {}", hrEmployeeId);

		// Validate BasicInfoDTO
		if (basicInfo == null) {
			throw new ResourceNotFoundException("Basic Info is required");
		}

		// Use validation service to validate basic info
		// Create a partial EmployeeOnboardingDTO with just basicInfo for validation
		try {
			EmployeeOnboardingDTO partialOnboardingDTO = new EmployeeOnboardingDTO();
			partialOnboardingDTO.setBasicInfo(basicInfo);
			// Validate basic info using validation service
			employeeValidationService.validateOnboardingData(partialOnboardingDTO);
			employeeValidationService.performPreFlightChecks(partialOnboardingDTO);
		} catch (Exception e) {
			logger.error("❌ ERROR: Employee onboarding validation failed in generateOrValidateTempPayrollId. Error: {}",
					e.getMessage(), e);
			throw e;
		}

		// Step 1: Validate HR Employee exists (for created_by) and get HR employee's campus
		Employee hrEmployee = employeeRepository.findById(hrEmployeeId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"HR Employee not found with emp_id: " + hrEmployeeId));

		// Step 2: Get Campus from HR Employee's campus_id (not from new employee's campusId)
		Campus campus = hrEmployee.getCampus_id();
		if (campus == null) {
			throw new ResourceNotFoundException(
					"HR Employee (emp_id: " + hrEmployeeId + ") does not have a campus assigned. Cannot generate temp_payroll_id.");
		}

		// Validate campus is active
		if (campus.getIsActive() == null || campus.getIsActive() != 1) {
			throw new ResourceNotFoundException(
					"HR Employee's campus (campus_id: " + campus.getCampusId() + ") is not active. Cannot generate temp_payroll_id.");
		}

		int campusCodeInt = campus.getCode();
		if (campusCodeInt == 0) {
			throw new ResourceNotFoundException(
					"Campus code is 0 or not set for HR Employee's campus_id: " + campus.getCampusId() +
							". Cannot generate temp_payroll_id.");
		}

		String campusCode = String.valueOf(campusCodeInt);
		String baseKey = "TEMP" + campusCode; // e.g., "TEMP1062"
		logger.info("Base key for temp_payroll_id generation using HR Employee's campus code: {}", baseKey);

		// Step 3: Check if tempPayrollId is provided from frontend
		String tempPayrollIdFromFrontend = basicInfo.getTempPayrollId();

		// Step 4: Get aadharNum and phoneNumber from BasicInfoDTO (only needed if tempPayrollId is NOT provided)
		Long aadharNum = basicInfo.getAadharNum();
		Long phoneNumber = basicInfo.getPrimaryMobileNo();

		logger.info("Received values - tempPayrollId: '{}', aadharNum: '{}', phoneNumber: {}",
				tempPayrollIdFromFrontend, aadharNum, phoneNumber);

		// Validate aadharNum and phoneNumber ONLY if tempPayrollId is NOT provided (for generation)
		if (tempPayrollIdFromFrontend == null || tempPayrollIdFromFrontend.trim().isEmpty()) {
			
			// CHANGED: Removed .trim() because aadharNum is Long. Checks for null or 0.
			if (aadharNum == null || aadharNum <= 0) {
				logger.error("❌ Validation failed: aadharNum is null or empty. Received BasicInfoDTO: {}", basicInfo);
				throw new ResourceNotFoundException(
						"Aadhaar number (aadharNum) is required for temp_payroll_id generation when tempPayrollId is not provided. Please provide 'aadharNum' field in the request body.");
			}

			if (phoneNumber == null || phoneNumber <= 0) {
				logger.error("❌ Validation failed: primaryMobileNo is null. Received BasicInfoDTO: {}", basicInfo);
				throw new ResourceNotFoundException(
						"Phone number (primaryMobileNo) is required for temp_payroll_id generation when tempPayrollId is not provided. Please provide 'primaryMobileNo' field in the request body.");
			}
		}

		// Step 5: Handle tempPayrollId validation/generation
		String finalTempPayrollId = null;
		Employee employee = null;
		boolean isUpdate = false;

		if (tempPayrollIdFromFrontend != null && !tempPayrollIdFromFrontend.trim().isEmpty()) {
			// Case 1: tempPayrollId is provided from frontend
			tempPayrollIdFromFrontend = tempPayrollIdFromFrontend.trim();
			logger.info("tempPayrollId provided from frontend: {}", tempPayrollIdFromFrontend);

			// Check if it exists in SkillTestDetails table (only active records)
			Optional<com.employee.entity.SkillTestDetails> skillTestDetails = skillTestDetailsRepository
					.findActiveByTempPayrollId(tempPayrollIdFromFrontend);

			if (skillTestDetails.isPresent()) {
				// tempPayrollId exists in SkillTestDetails - use it
				logger.info("✅ tempPayrollId '{}' found in SkillTestDetails table. Using existing tempPayrollId.",
						tempPayrollIdFromFrontend);

				// Check if it already exists in Employee table
				Optional<Employee> existingEmployee = employeeRepository.findByTempPayrollId(tempPayrollIdFromFrontend);
				if (existingEmployee.isPresent()) {
					// UPDATE MODE: Employee with this tempPayrollId already exists - update it
					employee = existingEmployee.get();
					isUpdate = true;
					logger.info("🔄 UPDATE MODE: Employee with temp_payroll_id '{}' already exists (emp_id: {}). Updating existing employee.",
							tempPayrollIdFromFrontend, employee.getEmp_id());

					// DO NOT update app status here - it will be set when remaining tabs are saved
					// Status updates should only happen in remaining tabs method

					// Update existing employee with new data from BasicInfoDTO
					entityPreparationService.updateEmployeeEntity(employee, basicInfo);

					// Override updated_by with HR Employee ID ONLY if status is "Confirm"
					if (employee.getEmp_check_list_status_id() != null) {
						String currentStatus = employee.getEmp_check_list_status_id().getCheck_app_status_name();
						if ("Confirm".equals(currentStatus)) {
							employee.setUpdated_by(hrEmployeeId);
							employee.setUpdated_date(new Timestamp(System.currentTimeMillis()));
						}
					}
				} else {
					// INSERT MODE: Create new employee
					employee = entityPreparationService.prepareEmployeeEntity(basicInfo);
					// Set created_by only if provided from frontend, otherwise leave as null (entity default will handle)
					// Note: prepareEmployeeEntity already handles createdBy from basicInfo, so we don't need to set it again here
					// Set app status to "Incompleted" when generating temp ID
					EmployeeCheckListStatus incompletedStatus = employeeCheckListStatusRepository
							.findByCheck_app_status_name("Incompleted")
							.orElseThrow(() -> new ResourceNotFoundException(
									"EmployeeCheckListStatus with name 'Incompleted' not found"));
					employee.setEmp_check_list_status_id(incompletedStatus);
					logger.info("➕ INSERT MODE: Creating new employee with temp_payroll_id: {} (app status set to 'Incompleted')", tempPayrollIdFromFrontend);
				}

				// Use the provided tempPayrollId
				finalTempPayrollId = tempPayrollIdFromFrontend;
			} else {
				// tempPayrollId NOT found in SkillTestDetails - throw error
				throw new ResourceNotFoundException(
						"tempPayrollId '" + tempPayrollIdFromFrontend +
								"' not found in SkillTestDetails table. Please provide a valid tempPayrollId from SkillTestDetails.");
			}
		} else {
			// Case 2: tempPayrollId is NOT provided from frontend
			logger.info("tempPayrollId NOT provided from frontend. Checking aadhar OR phone in SkillTestDetails, Employee, and EmpDetails tables...");

			// Check if EITHER aadharNum OR phoneNumber exists in ANY table:
			// 1. SkillTestDetails table (has both aadhaar_no and contact_number) - ONLY check active records (is_active = 1)
			// 2. EmpDetails table (has adhaar_no field - Aadhaar number is stored here)
			// 3. Employee table (has primary_mobile_no field - Phone number is stored here)
			// If ANY ONE matches, cannot generate new tempPayrollId
			// NOTE: If SkillTestDetails record has is_active = 0, it will be treated as new and allow creating new temp ID

			// Check 1: Aadhaar number in SkillTestDetails table (only active records)
			Optional<com.employee.entity.SkillTestDetails> existingByAadhaarInSkillTest = skillTestDetailsRepository
					.findActiveByAadhaarNo(aadharNum);

			// Check 2: Aadhaar number in EmpDetails table
			// Note: Aadhaar number is stored in EmpDetails.adhaar_no (NOT in Employee table)
			Optional<EmpDetails> existingByAadhaarInEmployee = empDetailsRepository.findByAdhaar_no(aadharNum);

			// Check 3: Phone number in SkillTestDetails table (only active records)
			Optional<com.employee.entity.SkillTestDetails> existingByPhoneInSkillTest = skillTestDetailsRepository
					.findActiveByContactNumber(phoneNumber);

			// Check 4: Phone number in Employee table
			// Note: Primary phone number is stored in Employee.primary_mobile_no (NOT in EmpDetails table)
			Optional<Employee> existingByPhoneInEmployee = employeeRepository.findByPrimary_mobile_no(phoneNumber);

			// Validation: If Aadhaar exists in SkillTestDetails (only active records)
			if (existingByAadhaarInSkillTest.isPresent()) {
				String existingTempPayrollId = existingByAadhaarInSkillTest.get().getTempPayrollId();
				Long existingPhone = existingByAadhaarInSkillTest.get().getContact_number();
				throw new ResourceNotFoundException(
						"Employee with Aadhaar number '" + aadharNum +
								"' already exists in SkillTestDetails table (active) with tempPayrollId: '" +
								existingTempPayrollId +
								"' and phone number: '" + existingPhone +
								"'. Cannot generate new tempPayrollId. Please use the existing tempPayrollId from SkillTestDetails.");
			}

			// Validation: If Aadhaar exists in EmpDetails table
			// EmpDetails is linked to Employee via emp_id, so we get the Employee to check tempPayrollId
			if (existingByAadhaarInEmployee.isPresent()) {
				EmpDetails empDetails = existingByAadhaarInEmployee.get();
				Employee existingEmp = empDetails.getEmployee_id();
				String existingTempPayrollId = existingEmp != null ? existingEmp.getTempPayrollId() : "N/A";
				int existingEmpId = existingEmp != null ? existingEmp.getEmp_id() : 0;
				throw new ResourceNotFoundException(
						"Employee with Aadhaar number '" + aadharNum +
								"' already exists in EmpDetails table (linked to Employee emp_id: " + existingEmpId +
								") with tempPayrollId: '" + existingTempPayrollId +
								"'. Cannot generate new tempPayrollId.");
			}

			// Validation: If Phone exists in SkillTestDetails (only active records)
			if (existingByPhoneInSkillTest.isPresent()) {
				String existingTempPayrollId = existingByPhoneInSkillTest.get().getTempPayrollId();
				Long existingAadhaar = existingByPhoneInSkillTest.get().getAadhaar_no();
				throw new ResourceNotFoundException(
						"Employee with phone number '" + phoneNumber +
								"' already exists in SkillTestDetails table (active) with tempPayrollId: '" +
								existingTempPayrollId +
								"' and Aadhaar number: '" + existingAadhaar +
								"'. Cannot generate new tempPayrollId. Please use the existing tempPayrollId from SkillTestDetails.");
			}

			// Validation: If Phone exists in Employee table
			if (existingByPhoneInEmployee.isPresent()) {
				Employee existingEmp = existingByPhoneInEmployee.get();
				String existingTempPayrollId = existingEmp.getTempPayrollId();
				int existingEmpId = existingEmp.getEmp_id();
				throw new ResourceNotFoundException(
						"Employee with phone number '" + phoneNumber +
								"' already exists in Employee table (emp_id: " + existingEmpId +
								") with tempPayrollId: '" + existingTempPayrollId +
								"'. Cannot generate new tempPayrollId.");
			}

			// Employee NOT found in any table - generate new tempPayrollId
			logger.info("Employee NOT found in SkillTestDetails, Employee, or EmpDetails tables. Generating new tempPayrollId...");

			// Step 7: Find max tempPayrollId in BOTH SkillTestDetails and Employee tables
			String maxInSkillTest = skillTestDetailsRepository.findMaxTempPayrollIdByKey(baseKey + "%");
			String maxInEmployee = employeeRepository.findMaxTempPayrollIdByKey(baseKey + "%");

			logger.info("Max tempPayrollId in SkillTestDetails: {}", maxInSkillTest);
			logger.info("Max tempPayrollId in Employee: {}", maxInEmployee);

			// Step 8: Find the highest number from both tables
			int maxValue = 0;

			if (maxInSkillTest != null) {
				try {
					String numberPart = maxInSkillTest.substring(baseKey.length());
					int value = Integer.parseInt(numberPart);
					if (value > maxValue) {
						maxValue = value;
					}
				} catch (NumberFormatException e) {
					logger.warn("Could not parse number part from SkillTestDetails: {}", maxInSkillTest);
				}
			}

			if (maxInEmployee != null) {
				try {
					String numberPart = maxInEmployee.substring(baseKey.length());
					int value = Integer.parseInt(numberPart);
					if (value > maxValue) {
						maxValue = value;
					}
				} catch (NumberFormatException e) {
					logger.warn("Could not parse number part from Employee: {}", maxInEmployee);
				}
			}

			// Step 9: Generate next number
			int nextValue = maxValue + 1;
			String paddedValue = String.format("%04d", nextValue); // 4-digit padding
			finalTempPayrollId = baseKey + paddedValue; // e.g., "TEMP10620001"

			logger.info("Generated new tempPayrollId: {} (next value: {})", finalTempPayrollId, nextValue);

			// INSERT MODE: Create new employee
			employee = entityPreparationService.prepareEmployeeEntity(basicInfo);
			// Set created_by only if provided from frontend, otherwise leave as null (entity default will handle)
			// Note: prepareEmployeeEntity already handles createdBy from basicInfo, so we don't need to set it again here
			// Set app status to "Incompleted" when generating temp ID
			EmployeeCheckListStatus incompletedStatus = employeeCheckListStatusRepository
					.findByCheck_app_status_name("Incompleted")
					.orElseThrow(() -> new ResourceNotFoundException(
							"EmployeeCheckListStatus with name 'Incompleted' not found"));
			employee.setEmp_check_list_status_id(incompletedStatus);
			logger.info("➕ INSERT MODE: Creating new employee with generated temp_payroll_id: {} (app status set to 'Incompleted')", finalTempPayrollId);
		}

		// Step 10: Set tempPayrollId in Employee (for both insert and update)
		employee.setTempPayrollId(finalTempPayrollId);

		// Step 11: Save Employee (INSERT or UPDATE)
		// Set updated_by and updated_date for UPDATE mode ONLY if status is "Confirm"
		if (isUpdate && employee.getEmp_check_list_status_id() != null) {
			String currentStatus = employee.getEmp_check_list_status_id().getCheck_app_status_name();
			if ("Confirm".equals(currentStatus)) {
				employee.setUpdated_by(hrEmployeeId);
				employee.setUpdated_date(new Timestamp(System.currentTimeMillis()));
			}
		}
		employee = employeeRepository.save(employee);

		// Get the employee ID (auto-generated for new, existing for update)
		Integer employeeId = employee.getEmp_id();

		// Step 12: Prepare and save EmpDetails with email validation
		// Note: AddressInfoDTO is null here since we only have BasicInfoDTO
		// Get createdBy from basicInfo (can be null if not provided)
		Integer createdBy = basicInfo.getCreatedBy();
		EmpDetails empDetails = entityPreparationService.prepareEmpDetailsEntity(basicInfo, null, employee, createdBy);

		if (isUpdate) {
			// UPDATE MODE: Check if email already exists
			// First, try to find by emp_id
			Optional<EmpDetails> existingDetails = empDetailsRepository.findById(employeeId);

			if (existingDetails.isPresent()) {
				// Update existing record - copy all fields from new to existing
				EmpDetails existing = existingDetails.get();
				entityPreparationService.updateEmpDetailsFields(existing, empDetails);
				// Set updated_by and updated_date (hrEmployeeId is the updater)
				existing.setUpdated_by(hrEmployeeId);
				existing.setUpdated_date(new Timestamp(System.currentTimeMillis()));
				empDetailsRepository.save(existing);
				logger.info("Updated existing EmpDetails for employee (emp_id: {})", employeeId);
			} else {
				// Not found by emp_id - check by email if email is provided
				if (empDetails.getPersonal_email() != null && !empDetails.getPersonal_email().trim().isEmpty()) {
					Optional<EmpDetails> existingByEmail = empDetailsRepository
							.findByPersonal_email(empDetails.getPersonal_email().trim());

					if (existingByEmail.isPresent()) {
						// Email already exists - update that record (preserve email, update other fields)
						EmpDetails existing = existingByEmail.get();
						// Update all fields except email (preserve existing email to avoid unique constraint violation)
						entityPreparationService.updateEmpDetailsFieldsExceptEmail(existing, empDetails);
						// Update employee_id to point to current employee
						existing.setEmployee_id(employee);
						// Set updated_by and updated_date (hrEmployeeId is the updater)
						existing.setUpdated_by(hrEmployeeId);
						existing.setUpdated_date(new Timestamp(System.currentTimeMillis()));
						empDetailsRepository.save(existing);
						logger.info("Updated existing EmpDetails found by email for employee (emp_id: {}), email: {}",
								employeeId, empDetails.getPersonal_email());
					} else {
						// Email doesn't exist - create new record
						empDetailsRepository.save(empDetails);
						logger.info("Created new EmpDetails for employee (emp_id: {})", employeeId);
					}
				} else {
					// No email provided - create new record
					empDetailsRepository.save(empDetails);
					logger.info("Created new EmpDetails for employee (emp_id: {})", employeeId);
				}
			}
		} else {
			// INSERT MODE: Check if email already exists before creating
			if (empDetails.getPersonal_email() != null && !empDetails.getPersonal_email().trim().isEmpty()) {
				Optional<EmpDetails> existingByEmail = empDetailsRepository
						.findByPersonal_email(empDetails.getPersonal_email().trim());

				if (existingByEmail.isPresent()) {
					// Email already exists - update that record (preserve email, update other fields)
					EmpDetails existing = existingByEmail.get();
					// Update all fields except email (preserve existing email to avoid unique constraint violation)
					entityPreparationService.updateEmpDetailsFieldsExceptEmail(existing, empDetails);
					// Update employee_id to point to current employee
					existing.setEmployee_id(employee);
					// Set updated_by and updated_date (hrEmployeeId is the updater)
					existing.setUpdated_by(hrEmployeeId);
					existing.setUpdated_date(new Timestamp(System.currentTimeMillis()));
					empDetailsRepository.save(existing);
					logger.info("Updated existing EmpDetails found by email during INSERT for employee (emp_id: {}), email: {}",
							employeeId, empDetails.getPersonal_email());
				} else {
					// Email doesn't exist - create new record
					empDetailsRepository.save(empDetails);
					logger.info("Created new EmpDetails for employee (emp_id: {})", employeeId);
				}
			} else {
				// No email provided - create new record
				empDetailsRepository.save(empDetails);
				logger.info("Created new EmpDetails for employee (emp_id: {})", employeeId);
			}
		}

		// Step 13: Prepare and save EmpPfDetails (pre_uan and pre_esi)
		// Only previous UAN and previous ESI numbers are stored at HR level (not current PF/ESI/UAN)
		EmpPfDetails empPfDetails = entityPreparationService.prepareEmpPfDetailsEntity(basicInfo, employee, createdBy);

		if (empPfDetails != null) {
			empPfDetails.setEmployee_id(employee);

			// Update existing or create new
			if (isUpdate) {
				Optional<EmpPfDetails> existingPfDetails = empPfDetailsRepository.findByEmployeeId(employeeId);

				if (existingPfDetails.isPresent()) {
					// Update existing record
					EmpPfDetails existing = existingPfDetails.get();
					existing.setPre_esi_no(empPfDetails.getPre_esi_no());
					existing.setIs_active(empPfDetails.getIs_active());
					// Set updated_by and updated_date (hrEmployeeId is the updater)
					existing.setUpdated_by(hrEmployeeId);
					existing.setUpdated_date(new Timestamp(System.currentTimeMillis()));
					empPfDetailsRepository.save(existing);
					logger.info("Updated existing EmpPfDetails for employee (emp_id: {})", employeeId);
				} else {
					// Create new record
					empPfDetailsRepository.save(empPfDetails);
					logger.info("Created new EmpPfDetails for employee (emp_id: {})", employeeId);
				}
			} else {
				// INSERT MODE: Create new
				empPfDetailsRepository.save(empPfDetails);
				logger.info("Created new EmpPfDetails for employee (emp_id: {})", employeeId);
			}
		}

		if (isUpdate) {
			logger.info("✅ Successfully UPDATED employee (emp_id: {}) with tempPayrollId '{}'. Updated by HR Employee (emp_id: {})",
					employeeId, finalTempPayrollId, hrEmployeeId);
		} else {
			logger.info("✅ Successfully created NEW employee (emp_id: {}) with tempPayrollId '{}'. Created by HR Employee (emp_id: {})",
					employeeId, finalTempPayrollId, hrEmployeeId);
		}

		// Return response DTO with both tempPayrollId and employee ID
		TempPayrollIdResponseDTO response = new TempPayrollIdResponseDTO();
		response.setTempPayrollId(finalTempPayrollId);
		response.setEmployeeId(employeeId);
		response.setMessage(isUpdate ? "Employee updated successfully with Temp Payroll ID"
				: "New employee created successfully with Temp Payroll ID");
		response.setBasicInfo(basicInfo); // Include posted BasicInfoDTO in response

		return response;
	}


}