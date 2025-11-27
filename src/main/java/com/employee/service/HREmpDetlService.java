package com.employee.service;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.employee.dto.AddressResponseDTO;
import com.employee.dto.BankInfoGetDTO;
import com.employee.dto.CampusPrincipalDTO;
import com.employee.dto.ChequeDetailsDto;
import com.employee.dto.EducationalDocumentStatusDTO;
import com.employee.dto.EmpExperienceDetailsDTO;
import com.employee.dto.EmpFamilyDetailsDTO;
//import com.employee.dto.EmpQualificationDetailsDTO;
import com.employee.dto.EmployeeAgreementDetailsDto;
import com.employee.dto.EmployeeBankDetailsResponseDTO;
import com.employee.dto.EmployeeCampusInfoDTO;
import com.employee.dto.EmployeeCurrentInfoDTO;
import com.employee.dto.EmployeeCurrentInfoDTO.SubjectInfo;
import com.employee.dto.EmployeeRelationDTO;
import com.employee.dto.FamilyMemberInOrgDTO;
import com.employee.dto.QualificationDetailsDto;
import com.employee.entity.BankDetails;
import com.employee.entity.Campus;
import com.employee.entity.CampusContact;
import com.employee.entity.EmpChequeDetails;
import com.employee.entity.EmpDocType;
import com.employee.entity.EmpDocuments;
import com.employee.entity.EmpExperienceDetails;
import com.employee.entity.EmpFamilyDetails;
import com.employee.entity.EmpQualification;
import com.employee.entity.EmpSubject;
import com.employee.entity.EmpaddressInfo;
import com.employee.entity.Employee;
import com.employee.entity.EmployeeBasicInfoView;
import com.employee.entity.Organization;
import com.employee.exception.ResourceNotFoundException;
import com.employee.repository.BankDetailsRepository;
import com.employee.repository.CampusContactRepository;
import com.employee.repository.EmpBasicInfoViewRepo;
import com.employee.repository.EmpChequeDetailsRepository;
import com.employee.repository.EmpDocTypeRepository;
import com.employee.repository.EmpDocumentsRepository;
import com.employee.repository.EmpExperienceDetailsRepository;
import com.employee.repository.EmpFamilyDetailsRepository;
import com.employee.repository.EmpQualificationRepository;
import com.employee.repository.EmpSubjectRepository;
import com.employee.repository.EmpaddressInfoRepository;
import com.employee.repository.EmployeeRepository;
import com.employee.repository.OrganizationRepository;

import jakarta.transaction.Transactional;

@Service
public class HREmpDetlService {
	
	
	private static final Logger logger = LoggerFactory.getLogger(HREmpDetlService.class);
	@Autowired
    private EmpFamilyDetailsRepository empFamilyDetailsRepository;
	
	
	@Autowired EmpaddressInfoRepository empAddressInfoRepository;
	@Autowired EmployeeRepository employeeRepository;
	@Autowired BankDetailsRepository bankDetailsRepository;
	@Autowired EmpSubjectRepository empSubjectRepository;
	@Autowired EmpExperienceDetailsRepository empExperienceDetailsRepository;
	@Autowired CampusContactRepository campusContactRepository;
	@Autowired EmpBasicInfoViewRepo employeeBasicInfoRepository;
	@Autowired EmpChequeDetailsRepository empChequeDetailsRepository;
	@Autowired EmpQualificationRepository empQualificationRepository;
	@Autowired OrganizationRepository organizationRepository;
	@Autowired
	private EmpDocumentsRepository empDocumentsRepository;
	@Autowired
	private EmpDocTypeRepository empDocTypeRepository;
	

	public List<EmpFamilyDetailsDTO> getFamilyMembersByPayrollId(String payrollId) {
		// Caller code (Service/Controller)

		List<EmpFamilyDetails> familyList = 
		    empFamilyDetailsRepository.findByEmp_id_PayrollIdAndIsActive(payrollId, 1);

        return familyList.stream().map(fam -> {
            EmpFamilyDetailsDTO dto = new EmpFamilyDetailsDTO();
            dto.setEmpFamilyDetlId(fam.getEmp_family_detl_id());
            dto.setFirstName(fam.getFirst_name());
            dto.setLastName(fam.getLast_name());
            dto.setOccupation(fam.getOccupation());
            dto.setGender(fam.getGender_id().getGenderName());
            dto.setBloodGroup(fam.getBlood_group_id().getBloodGroupName());
            dto.setNationality(fam.getNationality());
            dto.setRelation(fam.getRelation_id().getStudentRelationType());
            dto.setIsDependent(fam.getIs_dependent());
            dto.setIsLate(fam.getIs_late());
            dto.setEmail(fam.getEmail());
            dto.setContactNumber(fam.getContact_no()); // ✅ correct field name
            return dto;
        }).collect(Collectors.toList());
    }
	
	public Map<String, List<AddressResponseDTO>> getAddressByPayrollIdGrouped(String payrollId) {
	    List<EmpaddressInfo> addresses = empAddressInfoRepository.findByEmpId_PayrollId(payrollId);

	    // Convert entity list into DTO list grouped by address type
	    return addresses.stream()
	            .map(address -> new AddressResponseDTO(
	                    address.getAddrs_type(),
	                    address.getHouse_no(),
	                    address.getLandmark(),
	                    address.getPostal_code(),
	                    address.getCity_id() != null ? address.getCity_id().getCityName() : null,
	                    address.getState_id() != null ? address.getState_id().getStateName() : null,
	                    address.getCountry_id() != null ? address.getCountry_id().getCountryName() : null
	            ))
	            .collect(Collectors.groupingBy(
	                    AddressResponseDTO::getAddressType,
	                    LinkedHashMap::new,
	                    Collectors.toList()
	            ));
	}
	
	 public List<FamilyMemberInOrgDTO> getFamilyMembersInOrganization(String payrollId) {
	        // Step 1: Get the main employee
	        Employee employee = employeeRepository.findByPayRollId(payrollId)
	                .orElseThrow(() -> new RuntimeException("Employee not found for payrollId: " + payrollId));

	        // Step 2: Get family records
	        List<EmpFamilyDetails> familyList = empFamilyDetailsRepository.findByEmp_id_EmpId(employee.getEmp_id());

	        // Step 3: Filter and map to DTO
	        return familyList.stream()
	                .filter(f -> f.getIs_sri_chaitanya_emp() == 1 && f.getParent_emp_id() != null)
	                .map(f -> {
	                    Employee parent = f.getParent_emp_id();
	                    return new FamilyMemberInOrgDTO(
	                            parent.getFirst_name() + " " + parent.getLast_name(),
	                            parent.getPayRollId(),
	                            parent.getEmail(),
	                            parent.getPrimary_mobile_no(),
	                            parent.getDesignation() != null ? parent.getDesignation().getDesignation_name() : null
	                    );
	                })
	                .collect(Collectors.toList());
	    }
	// 🔹 Manager Details
	    public EmployeeRelationDTO getManagerDetails(String payrollId) {
	        Employee employee = employeeRepository.findByPayRollId(payrollId)
	                .orElseThrow(() -> new RuntimeException("Employee not found"));

	        Employee manager = employee.getEmployee_manager_id();
	        return mapToDTO(manager);
	    }

	    // 🔹 Reference Details
	    public EmployeeRelationDTO getReferenceDetails(String payrollId) {
	        Employee employee = employeeRepository.findByPayRollId(payrollId)
	                .orElseThrow(() -> new RuntimeException("Employee not found"));

	        Employee reference = employee.getEmployee_reference();
	        return mapToDTO(reference);
	    }

	    // 🔹 Reporting Manager Details
	    public EmployeeRelationDTO getReportingManagerDetails(String payrollId) {
	        Employee employee = employeeRepository.findByPayRollId(payrollId)
	                .orElseThrow(() -> new RuntimeException("Employee not found"));

	        Employee reportingManager = employee.getEmployee_reporting_id();
	        return mapToDTO(reportingManager);
	    }

	    // 🔹 Utility Mapper
	    private EmployeeRelationDTO mapToDTO(Employee emp) {
	        if (emp == null) return null;

	        return new EmployeeRelationDTO(
	                emp.getFirst_name() + " " + emp.getLast_name(),
	                emp.getPayRollId(),
	                emp.getEmail(),
	                emp.getPrimary_mobile_no(),
	                emp.getDesignation() != null ? emp.getDesignation().getDesignation_name(): null
	        );
	    }
	    

	    public EmployeeBankDetailsResponseDTO getBankDetailsByPayrollId(String payrollId) {

	        // ✅ 1. Find employee
	        Optional<Employee> employeeOpt = employeeRepository.findByPayRollId(payrollId);
	        if (employeeOpt.isEmpty()) {
	            throw new RuntimeException("Employee not found for payrollId: " + payrollId);
	        }

	        Employee employee = employeeOpt.get();

	        // ✅ 2. Fetch active bank details
	        List<BankDetails> bankDetailsList = bankDetailsRepository.findActiveBankDetailsByEmpId(employee.getEmp_id());

	        if (bankDetailsList.isEmpty()) {
	            throw new RuntimeException("No bank details found for payrollId: " + payrollId);
	        }

	        BankInfoGetDTO personalBankInfo = null;
	        BankInfoGetDTO salaryAccountInfo = null;

	        // ✅ 3. Map entity → DTO
	        for (BankDetails bank : bankDetailsList) {
	            BankInfoGetDTO dto = new BankInfoGetDTO();
	            dto.setPaymentType(bank.getEmpPaymentType() != null ? bank.getEmpPaymentType().getPayment_type() : null);
	            dto.setBankName(bank.getBankName());
	            dto.setBankBranch(bank.getBankBranch());
	            dto.setPersonalAccountHolderName(bank.getBankHolderName());
	            dto.setPersonalAccountNumber(bank.getAccNo());
	            dto.setIfscCode(bank.getIfscCode());
	            // Set payableAt from BankDetails entity
	            dto.setPayableAt(bank.getPayableAt());

	            // Note: isSalaryLessThan40000 calculation removed as netPayable field no longer exists
	            // This information should come from BankInfoDTO.salaryLessThan40000 if needed
	            dto.setIsSalaryLessThan40000("N/A");

	            // ✅ Classify based on accType
	            if ("SALARY".equalsIgnoreCase(bank.getAccType()) || "SALARY ACCOUNT".equalsIgnoreCase(bank.getAccType())) {
	                salaryAccountInfo = dto;
	            } else {
	                personalBankInfo = dto;
	            }
	        }

	        // ✅ 4. Build final DTO response
	        EmployeeBankDetailsResponseDTO response = new EmployeeBankDetailsResponseDTO();
	        response.setPersonalBankInfo(personalBankInfo);
	        response.setSalaryAccountInfo(salaryAccountInfo);

	        return response;
	    }
	    
	    public EmployeeCurrentInfoDTO getCurrentInfoByPayrollId(String payrollId) {

	        // ✅ Use your existing Optional<Employee> method
	        Optional<Employee> optionalEmp = employeeRepository.findByPayRollId(payrollId);

	        if (optionalEmp.isEmpty()) {
	            throw new RuntimeException("Employee not found for payrollId: " + payrollId);
	        }

	        Employee emp = optionalEmp.get();

	        // Fetch subjects assigned to this employee
	        List<EmpSubject> empSubjects = empSubjectRepository.findActiveSubjectsByEmpId(emp.getEmp_id());

	        List<SubjectInfo> subjectInfoList = empSubjects.stream()
	                .map(sub -> new SubjectInfo(
	                        sub.getSubject_id() != null ? sub.getSubject_id().getSubject_name(): null,
//	                        sub.getDepartmentId() != null ? sub.getDepartmentId().getDepartmentName() : null,
//	                        sub.getDesignationId() != null ? sub.getDesignationId().getDesignationName() : null,
	                        sub.getAgree_no_period()
	                ))
	                .collect(Collectors.toList());

	        // Prepare final DTO
	        EmployeeCurrentInfoDTO dto = new EmployeeCurrentInfoDTO();
	        dto.setEmployeeName(emp.getFirst_name() + " " + emp.getLast_name());
	        dto.setDateOfJoining(emp.getDate_of_join());
	        dto.setHiredBy(emp.getEmployee_hired() != null
	                ? emp.getEmployee_hired().getFirst_name() + " " + emp.getEmployee_hired().getLast_name()
	                : null);
	        dto.setReferredBy(emp.getEmployee_reference() != null
	                ? emp.getEmployee_reference().getFirst_name() + " " + emp.getEmployee_reference().getLast_name()
	                : null);
	        dto.setSubjects(subjectInfoList);

	        return dto;
	    }
	    
	    public List<EmpExperienceDetailsDTO> getEmployeeExperienceByPayrollId(String payrollId) {

	        // ✅ Step 1: Find employee
	        Optional<Employee> empOpt = employeeRepository.findByPayRollId(payrollId);
	        if (empOpt.isEmpty()) {
	            throw new RuntimeException("Employee not found for payrollId: " + payrollId);
	        }

	        Employee employee = empOpt.get();

	        // ✅ Step 2: Fetch experience records
	        List<EmpExperienceDetails> expList = empExperienceDetailsRepository.findActiveByEmployeeId(employee.getEmp_id());

	        if (expList.isEmpty()) {
	            throw new RuntimeException("No experience records found for employee: " + payrollId);
	        }

	        // ✅ Step 3: Convert to DTO list
//	        return expList.stream().map(exp -> {
//	            EmpExperienceDetailsDTO dto = new EmpExperienceDetailsDTO();
//	            dto.setCompanyName(exp.getPre_organigation_name()));
//	            dto.setDesignation(exp.getDesignation());
//	            dto.setFromDate(exp.getDateOfJoin().toLocalDate());
//	            dto.setToDate(exp.getDateOfLeave().toLocalDate());
//	            dto.setLeavingReason(exp.getLeavingReason());
//	            dto.setCompanyAddress(exp.getCompanyAddr());
//	            dto.setNatureOfDuties(exp.getNatureOfDuties());
//
//	            BigDecimal ctc = BigDecimal.valueOf(exp.getGrossSalary());
//	            dto.setCtc(ctc);
//	            dto.setGrossSalaryPerMonth(ctc.divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP));
//
//	            return dto;
//	        }).collect(Collectors.toList());
	        return expList.stream().map(exp -> {
	            EmpExperienceDetailsDTO dto = new EmpExperienceDetailsDTO();

	            // --- THIS IS THE FIX ---
	            // Changed the extra parenthesis ')' to a semicolon ';'
	            dto.setCompanyName(exp.getPre_organigation_name());

	            // These fields from your entity map to the DTO
	            dto.setDesignation(exp.getDesignation());
	            dto.setFromDate(exp.getDate_of_join().toLocalDate());
	            dto.setToDate(exp.getDate_of_leave().toLocalDate());
	            dto.setLeavingReason(exp.getLeaving_reason());
	            dto.setCompanyAddress(exp.getCompany_addr());
	            dto.setNatureOfDuties(exp.getNature_of_duties());

	            // Handle Salary Calculation
	            BigDecimal ctc = BigDecimal.valueOf(exp.getGross_salary());
	            dto.setCtc(ctc);

	            // Calculate per-month salary (using RoundingMode enum is preferred)
	            dto.setGrossSalaryPerMonth(ctc.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP));

	            return dto;
	        }).collect(Collectors.toList());
	    }
	    public Optional<EmployeeBasicInfoView> getBasicInfoByPayrollId(String payrollId) {
	        
	        // ==== NEW, MORE EFFICIENT LOGIC ====
	        // We no longer need to find the Employee first.
	        // We can query the EmployeeBasicInfoRepository directly.
	        return employeeBasicInfoRepository.findByPayrollId(payrollId);
	        }
	    
//	    public EmployeeAgreementDetailsDto getChequeDetailsByPayrollId(String payrollId) {
//	        Optional<Employee> empOpt = employeeRepository.findByPayrollId(payrollId);
//	        if (empOpt.isEmpty()) {
//	            throw new RuntimeException("Employee not found for payrollId: " + payrollId);
//	        }
//
//	        Employee emp = empOpt.get();
//	        List<EmpChequeDetails> cheques = empChequeDetailsRepository.findActiveChequesByEmpId(emp.getEmpId());
//
//	        List<ChequeDetailsDto> chequeDtos = cheques.stream()
//	                .map(chq -> new ChequeDetailsDto(
//	                        chq.getChequeNo(),
//	                        chq.getChequeBankName(),
//	                        chq.getChequeBankIfscCode()
//	                ))
//	                .collect(Collectors.toList());
//
//	        EmployeeAgreementDetailsDto response = new EmployeeAgreementDetailsDto();
//	        response.setAgreementCompany(
//	                emp.getAgreementOrgId() != null ? emp.getAgreementOrgId().getOrganizationName() : "N/A"
//	        );
//	        response.setAgreementType(emp.getAgreementType());
//	        response.setNoOfCheques(chequeDtos.size());
//	        response.setCheques(chequeDtos);
//
//	        return response;
//	    }
	    
	    
	    
	    
	    
	    
	    
//	    
//	  
//
//	    public EmployeeAgreementDetailsDto getChequeDetailsByPayrollId(String payrollId) {
//	        
//	        // --- FIX 1 ---
//	        // Renamed to 'findByPayRollId' to match your Employee entity's 'payRollId' field
//	        Optional<Employee> empOpt = employeeRepository.findByPayRollId(payrollId);
//	        
//	        if (empOpt.isEmpty()) {
//	            throw new RuntimeException("Employee not found for payrollId: " + payrollId);
//	        }
//
//	        Employee emp = empOpt.get();
//
//	        // --- FIX 2 ---
//	        // Changed 'emp.getEmpId()' to 'emp.getEmp_id()' to match your Employee entity
//	        List<EmpChequeDetails> cheques = empChequeDetailsRepository.findActiveChequesByEmpId(emp.getEmp_id());
//
//	        // This part was correct and matches your ChequeDetailsDto
//	        List<ChequeDetailsDto> chequeDtos = cheques.stream()
//	                .map(chq -> new ChequeDetailsDto(
//	                        chq.getChequeNo(),
//	                        chq.getChequeBankName(),
//	                        chq.getChequeBankIfscCode()
//	                ))
//	                .collect(Collectors.toList());
//
//	        EmployeeAgreementDetailsDto response = new EmployeeAgreementDetailsDto();
//	        String orgName = "N/A";
//	        Integer orgId = emp.getAgreement_org_id();
//	        if (orgId != null) {
//                // 2. Use the findById method to get the Organization
//	            Optional<Organization> orgOpt = organizationRepository.findById(orgId);
//	            
//	            if (orgOpt.isPresent()) {
//                    // 3. Get the name from the found object
//	                orgName = orgOpt.get().getOrganizationName(); 
//	            }
//	        }
//	        response.setAgreementCompany(orgName);
//
//	        // --- FIX 4 ---
//	        response.setAgreementType(emp.getAgreement_type());
//
//	        // These fields were correct
//	        response.setNoOfCheques(chequeDtos.size());
//	        response.setCheques(chequeDtos);
//
//	        return response;
//	    
//	        
//	    }
	    
	    
	 // Assuming this is inside a Service class that has @Autowired employeeRepository, 
	 // empChequeDetailsRepository, AND organizationRepository.
	    
	 @Transactional
	 public EmployeeAgreementDetailsDto getChequeDetailsByPayrollId(String payrollId) {
	     
	     // 1. Find employee using correct field name payRollId (capital R)
	     Optional<Employee> empOpt = employeeRepository.findByPayRollId(payrollId);
	     
	     if (empOpt.isEmpty()) {
	         throw new RuntimeException("Employee not found for payrollId: " + payrollId);
	     }

	     Employee emp = empOpt.get();

	     // 2. Fetch Cheques using correct getter emp.getEmp_id()
	     List<EmpChequeDetails> cheques = empChequeDetailsRepository.findActiveChequesByEmpId(emp.getEmp_id());

	     // 3. Map Cheque entities to DTOs (This logic is correct)
	     List<ChequeDetailsDto> chequeDtos = cheques.stream()
	             .map(chq -> new ChequeDetailsDto(
	                     chq.getChequeNo(),
	                     chq.getChequeBankName(),
	                     chq.getChequeBankIfscCode()
	             ))
	             .collect(Collectors.toList());

	     EmployeeAgreementDetailsDto response = new EmployeeAgreementDetailsDto();
	     
	     // 4. Resolve Organization Name using the manually fetched ID
	     String orgName = "N/A";
	     Integer orgId = emp.getAgreement_org_id(); // Correctly uses emp.getAgreement_org_id()
	     
	     if (orgId != null) {
	         Optional<Organization> orgOpt = organizationRepository.findById(orgId);
	         
	         if (orgOpt.isPresent()) {
	             orgName = orgOpt.get().getOrganizationName(); 
	         }
	     }
	     
	     // 5. Set response fields using correct getters
	     response.setAgreementCompany(orgName);
	     response.setAgreementType(emp.getAgreement_type()); // Correctly uses emp.getAgreement_type()

	     // 6. Final DTO population
	     response.setNoOfCheques(chequeDtos.size());
	     response.setCheques(chequeDtos);

	     return response;
	 } 
	    
	    
	    
	    public EmployeeCampusInfoDTO getEmployeeCampusInfo(String payrollId) {
	        
	        // 1. Fetch the employee by payrollId
	        Employee employee = employeeRepository.findByPayRollId(payrollId)
	                .orElseThrow(() -> new RuntimeException("Employee not found with payrollId: " + payrollId));

	        Campus campus = employee.getCampus_id();
	        if (campus == null) {
	            throw new RuntimeException("Employee is not associated with any campus.");
	        }

	        EmployeeCampusInfoDTO dto = new EmployeeCampusInfoDTO();

	        // 2. Populate Top Card (Current Campus Info)
	        dto.setCampusName(campus.getCampusName());
	        dto.setCampusCode(campus.getCmps_code());
	        dto.setCampusType(campus.getCmps_type());

	        if (employee.getDesignation() != null) {
	            dto.setDesignationName(employee.getDesignation().getDesignation_name()); // Assumes getter
	        }
	        if (employee.getWorkingMode_id() != null) {
	            dto.setWorkMode(employee.getWorkingMode_id().getWork_mode_type()); // Assumes getter
	        }
	        if (employee.getJoin_type_id() != null) {
	            dto.setJoiningAs(employee.getJoin_type_id().getJoin_type()); // Assumes getter
	        }
	        if (employee.getEmployee_replaceby_id() != null) {
	            dto.setReplacementEmployeeName(
	                    employee.getEmployee_replaceby_id().getFirst_name() + " " +
	                    employee.getEmployee_replaceby_id().getLast_name()
	            );
	        }

	        // 3. Populate Principal Card (Find by Designation "Principal")
	        Optional<CampusContact> principalOpt = campusContactRepository
	                .findByCmpsIdAndDesignation(campus, "PRINCIPAL"); // <-- Assuming designation is "Principal"

	        if (principalOpt.isPresent()) {
	            CampusContact principal = principalOpt.get();
	            CampusPrincipalDTO pDto = new CampusPrincipalDTO(
	                    principal.getEmpName(),
	                    principal.getDesignation(),
	                    principal.getContactNo(),
	                    principal.getEmail()
	            );
	            dto.setPrincipalInfo(pDto);
	        }

	        // 4. Populate Campus Address Card
//	        CampusAddressDTO aDto = new CampusAddressDTO();
//	        aDto.setCampusName(campus.getCampusName());
//	        
//	        if (campus.getCity() != null) {
//	            aDto.setCity(campus.getCity().getName()); // Assumes getter
//	        }
//	        if (campus.getState() != null) {
//	            aDto.setState(campus.getState().getName()); // Assumes getter
//	        }
//	         if (campus.getZone() != null) {
//	            aDto.setZone(campus.getZone().getName()); // Assumes getter
//	        }
//	        dto.setAddressInfo(aDto);

	        return dto;
	    }
	    
	    public List<String> getQualificationNamesByPayrollId(String payrollId) {
	        Optional<Employee> employee = employeeRepository.findByPayRollId(payrollId);
	        
	        List<EmpQualification> qualifications = empQualificationRepository
	                .findActiveQualificationsByEmployee(employee);
 
	        return qualifications.stream()
	                .map(eq -> eq.getQualification_id().getQualification_name()) // Assumes getQualificationName()
	                .distinct()
	                .collect(Collectors.toList());
	    }

	    /**
	     * API 2: Gets the details for a specific qualification name.
	     */
	    public List<QualificationDetailsDto> getQualificationsByPayrollId(String payrollId) {
	        List<EmpQualification> qualifications = empQualificationRepository.findByEmployeePayrollId(payrollId);

	     // Assuming 'qualifications' is List<EmpQualification>
	        return qualifications.stream().map(eq -> {
	            QualificationDetailsDto dto = new QualificationDetailsDto();
	            
	            // --- FIX 1 (Syntax Error) ---
	            // The getter method call was incomplete and had a parenthesis error.
	            dto.setQualificationName(eq.getQualification_id().getQualification_name());
	            
	            // --- FIX 2 (Field Name Mismatch) ---
	            // Corrected getter: eq.getQualification_degree_id().getDegree_name()
	            dto.setQualificationDegree(eq.getQualification_degree_id().getDegree_name());
	            
	            // --- FIX 3 (Field Name Mismatch) ---
	            // Corrected setter: dto.setPassedoutYear()
	            dto.setPassedoutYear(eq.getPassedout_year()); // Assuming entity uses getPassedout_year()
	            
	            // These fields are assumed correct (based on your entity's field names)
	            dto.setSpecialization(eq.getSpecialization());
	            dto.setInstitute(eq.getInstitute());
	            dto.setUniversity(eq.getUniversity());
	            
	            return dto;
	        }).collect(Collectors.toList());
	    }
	    
	    
	    /**
	     * Retrieves the comprehensive status (Required, Uploaded, Missing) of educational documents 
	     * for an employee based on their specific qualifications.
	     * * @param payrollId The employee's payroll ID.
	     * @return DTO containing lists of required, uploaded, and missing educational documents.
	     * @throws ResourceNotFoundException if the employee or their qualifications are not found.
	     */
	    public EducationalDocumentStatusDTO getEducationalDocumentsStatusByPayrollId(String payrollId) {
	        logger.info("Getting educational documents status for payroll_id: {}", payrollId);
	        
	        Employee employee = employeeRepository.findByPayrollId(payrollId)
	                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with payroll_id: " + payrollId));
	        
	        Integer empId = employee.getEmp_id();
	        logger.info("Found employee - emp_id: {}, payroll_id: {}, temp_payroll_id: {}", 
	                empId, employee.getPayRollId(), employee.getTempPayrollId()); 
	        
	        List<EmpQualification> employeeQualifications = empQualificationRepository.findByEmpIdAndIsActive(empId);
	        
	        if (employeeQualifications.isEmpty()) {
	            throw new ResourceNotFoundException("Employee (emp_id: " + empId + ") does not have any qualifications in EmpQualification table");
	        }
	        
	        Set<Integer> employeeQualificationIds = employeeQualifications.stream()
	                .map(eq -> eq.getQualification_id().getQualification_id())
	                .collect(Collectors.toSet());
	        
	        logger.info("Employee has qualifications with IDs: {}", employeeQualificationIds);
	        
	        Integer highestQualificationId = employee.getQualification_id() != null ? 
	                employee.getQualification_id().getQualification_id() : 
	                employeeQualificationIds.stream().mapToInt(Integer::intValue).max().orElse(0);
	        
	        String qualificationName = employee.getQualification_id() != null ? 
	                employee.getQualification_id().getQualification_name() : "Multiple Qualifications";
	        
	        List<EmpDocType> educationalDocTypes = empDocTypeRepository.findByDocTypeAndIsActive("Educational Document");
	        if (educationalDocTypes.isEmpty()) {
	            logger.warn("No active Educational Document types found, trying all records");
	            educationalDocTypes = empDocTypeRepository.findByDocType("Educational Document");
	        }
	        
	        educationalDocTypes.sort((a, b) -> Integer.compare(a.getDoc_type_id(), b.getDoc_type_id()));
	        
	        List<EmpDocType> requiredDocTypes = new ArrayList<>();
	        for (Integer qualId : employeeQualificationIds) {
	            EmpDocType matchingDoc = educationalDocTypes.stream()
	                    .filter(dt -> dt.getDoc_type_id() == qualId)
	                    .findFirst()
	                    .orElse(null);
	            if (matchingDoc != null && !requiredDocTypes.contains(matchingDoc)) {
	                requiredDocTypes.add(matchingDoc);
	            }
	        }
	        
	        requiredDocTypes.sort((a, b) -> Integer.compare(a.getDoc_type_id(), b.getDoc_type_id()));
	        
	        List<Integer> requiredDocTypeIds = requiredDocTypes.stream()
	                .mapToInt(EmpDocType::getDoc_type_id)
	                .boxed()
	                .collect(Collectors.toList());
	        
	        List<EmpDocuments> uploadedDocs = empDocumentsRepository.findByEmpIdAndDocTypeIds(empId, requiredDocTypeIds);
	        Set<Integer> uploadedDocTypeIds = uploadedDocs.stream()
	                .map(doc -> doc.getEmp_doc_type_id().getDoc_type_id())
	                .collect(Collectors.toSet());
	        
	        EducationalDocumentStatusDTO response = new EducationalDocumentStatusDTO();
	        response.setEmpId(empId);
	        response.setPayrollId(employee.getPayRollId());
	        response.setTempPayrollId(employee.getTempPayrollId());
	        response.setQualificationId(highestQualificationId);
	        response.setQualificationName(qualificationName);
	        
	        List<EducationalDocumentStatusDTO.DocumentStatusDTO> requiredDocumentsList = new ArrayList<>();
	        List<EducationalDocumentStatusDTO.DocumentStatusDTO> uploadedDocumentsList = new ArrayList<>();
	        List<EducationalDocumentStatusDTO.DocumentStatusDTO> missingDocumentsList = new ArrayList<>();
	        
	        for (EmpDocType docType : requiredDocTypes) {
	            EducationalDocumentStatusDTO.DocumentStatusDTO docStatus = new EducationalDocumentStatusDTO.DocumentStatusDTO();
	            docStatus.setDocTypeId(docType.getDoc_type_id());
	            docStatus.setDocName(docType.getDoc_name());
	            docStatus.setDocType(docType.getDoc_type());
	            
	            boolean isUploaded = uploadedDocTypeIds.contains(docType.getDoc_type_id());
	            docStatus.setIsUploaded(isUploaded);
	            
	            if (isUploaded) {
	                EmpDocuments uploadedDoc = uploadedDocs.stream()
	                        .filter(doc -> doc.getEmp_doc_type_id().getDoc_type_id() == docType.getDoc_type_id())
	                        .findFirst()
	                        .orElse(null);
	                if (uploadedDoc != null) {
	                    docStatus.setDocPath(uploadedDoc.getDoc_path());
	                }
	                uploadedDocumentsList.add(docStatus);
	            } else {
	                missingDocumentsList.add(docStatus);
	            }
	            requiredDocumentsList.add(docStatus);
	        }
	        
	        response.setRequiredDocuments(requiredDocumentsList);
	        response.setUploadedDocuments(uploadedDocumentsList);
	        response.setMissingDocuments(missingDocumentsList);
	        
	        return response;
	    }

	    /**
	     * Retrieves only the list of educational documents that have already been uploaded by the employee.
	     * * @param payrollId The employee's payroll ID.
	     * @return List of uploaded educational documents.
	     */
	    public List<EducationalDocumentStatusDTO.DocumentStatusDTO> getUploadedEducationalDocuments(String payrollId) {
	        logger.info("Getting uploaded educational documents for payroll_id: {}", payrollId);
	        EducationalDocumentStatusDTO status = getEducationalDocumentsStatusByPayrollId(payrollId);
	        return status.getUploadedDocuments();
	    }

	    /**
	     * Retrieves only the list of educational documents that are missing and need to be uploaded.
	     * * @param payrollId The employee's payroll ID.
	     * @return List of missing educational documents.
	     */
	    public List<EducationalDocumentStatusDTO.DocumentStatusDTO> getMissingEducationalDocuments(String payrollId) {
	        logger.info("Getting missing educational documents for payroll_id: {}", payrollId);
	        EducationalDocumentStatusDTO status = getEducationalDocumentsStatusByPayrollId(payrollId);
	        return status.getMissingDocuments();
	    }

	    /**
	     * Retrieves the comprehensive status (Required, Uploaded, Missing) of ID Proof documents.
	     * * @param payrollId The employee's payroll ID.
	     * @return DTO containing lists of required, uploaded, and missing ID proof documents.
	     * @throws ResourceNotFoundException if the employee is not found.
	     */
	    public EducationalDocumentStatusDTO getIdProofDocumentsStatusByPayrollId(String payrollId) {
	        logger.info("Getting ID Proof documents status for payroll_id: {}", payrollId);
	        
	        Employee employee = employeeRepository.findByPayrollId(payrollId)
	                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with payroll_id: " + payrollId));
	        
	        Integer empId = employee.getEmp_id();
	        logger.info("Found employee - emp_id: {}, payroll_id: {}", empId, employee.getPayRollId());
	        
	        List<EmpDocType> idProofDocTypes = empDocTypeRepository.findByDocTypeAndIsActive("ID Proof");
	        if (idProofDocTypes.isEmpty()) {
	            logger.warn("No active ID Proof Document types found, trying all records");
	            idProofDocTypes = empDocTypeRepository.findByDocType("ID Proof");
	        }
	        
	        idProofDocTypes.sort((a, b) -> Integer.compare(a.getDoc_type_id(), b.getDoc_type_id()));
	        
	        List<Integer> idProofDocTypeIds = idProofDocTypes.stream()
	                .mapToInt(EmpDocType::getDoc_type_id)
	                .boxed()
	                .collect(Collectors.toList());
	        
	        List<EmpDocuments> uploadedDocs = empDocumentsRepository.findByEmpIdAndDocTypeIds(empId, idProofDocTypeIds);
	        Set<Integer> uploadedDocTypeIds = uploadedDocs.stream()
	                .map(doc -> doc.getEmp_doc_type_id().getDoc_type_id())
	                .collect(Collectors.toSet());
	        
	        EducationalDocumentStatusDTO response = new EducationalDocumentStatusDTO();
	        response.setEmpId(empId);
	        response.setPayrollId(employee.getPayRollId());
	        response.setTempPayrollId(employee.getTempPayrollId());
	        
	        List<EducationalDocumentStatusDTO.DocumentStatusDTO> requiredDocumentsList = new ArrayList<>();
	        List<EducationalDocumentStatusDTO.DocumentStatusDTO> uploadedDocumentsList = new ArrayList<>();
	        List<EducationalDocumentStatusDTO.DocumentStatusDTO> missingDocumentsList = new ArrayList<>();
	        
	        for (EmpDocType docType : idProofDocTypes) {
	            EducationalDocumentStatusDTO.DocumentStatusDTO docStatus = new EducationalDocumentStatusDTO.DocumentStatusDTO();
	            docStatus.setDocTypeId(docType.getDoc_type_id());
	            docStatus.setDocName(docType.getDoc_name());
	            docStatus.setDocType(docType.getDoc_type());
	            
	            boolean isUploaded = uploadedDocTypeIds.contains(docType.getDoc_type_id());
	            docStatus.setIsUploaded(isUploaded);
	            
	            if (isUploaded) {
	                EmpDocuments uploadedDoc = uploadedDocs.stream()
	                        .filter(doc -> doc.getEmp_doc_type_id().getDoc_type_id() == docType.getDoc_type_id())
	                        .findFirst()
	                        .orElse(null);
	                if (uploadedDoc != null) {
	                    docStatus.setDocPath(uploadedDoc.getDoc_path());
	                }
	                uploadedDocumentsList.add(docStatus);
	            } else {
	                missingDocumentsList.add(docStatus);
	            }
	            requiredDocumentsList.add(docStatus);
	        }
	        
	        response.setRequiredDocuments(requiredDocumentsList);
	        response.setUploadedDocuments(uploadedDocumentsList);
	        response.setMissingDocuments(missingDocumentsList);
	        
	        return response;
	    }

	    /**
	     * Retrieves only the list of ID Proof documents that have already been uploaded.
	     * * @param payrollId The employee's payroll ID.
	     * @return List of uploaded ID proof documents.
	     */
	    public List<EducationalDocumentStatusDTO.DocumentStatusDTO> getUploadedIdProofDocuments(String payrollId) {
	        logger.info("Getting uploaded ID Proof documents for payroll_id: {}", payrollId);
	        EducationalDocumentStatusDTO status = getIdProofDocumentsStatusByPayrollId(payrollId);
	        return status.getUploadedDocuments();
	    }

	    /**
	     * Retrieves only the list of ID Proof documents that are missing.
	     * * @param payrollId The employee's payroll ID.
	     * @return List of missing ID proof documents.
	     */
	    public List<EducationalDocumentStatusDTO.DocumentStatusDTO> getMissingIdProofDocuments(String payrollId) {
	        logger.info("Getting missing ID Proof documents for payroll_id: {}", payrollId);
	        EducationalDocumentStatusDTO status = getIdProofDocumentsStatusByPayrollId(payrollId);
	        return status.getMissingDocuments();
	    }

	    /**
	     * Retrieves the status of specific required documents (Personal, Previous Employment, Gratuity).
	     * * @param payrollId The employee's payroll ID.
	     * @return DTO containing lists of required, uploaded, and missing specific documents.
	     * @throws ResourceNotFoundException if the employee is not found.
	     */
	    public EducationalDocumentStatusDTO getSpecificDocumentsStatusByPayrollId(String payrollId) {
	        logger.info("Getting specific documents status for payroll_id: {}", payrollId);
	        
	        Employee employee = employeeRepository.findByPayrollId(payrollId)
	                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with payroll_id: " + payrollId));
	        
	        Integer empId = employee.getEmp_id();
	        logger.info("Found employee - emp_id: {}, payroll_id: {}", empId, employee.getPayRollId());
	        
	        List<EmpDocType> personalDocTypes = empDocTypeRepository.findByDocTypeAndIsActive("Personal Document");
	        if (personalDocTypes.isEmpty()) {
	            personalDocTypes = empDocTypeRepository.findByDocType("Personal Document");
	        }
	        
	        Optional<EmpDocType> prevEmployeeDocOpt = empDocTypeRepository.findByDocNameAndIsActive("PrevEmployeeDoc", 1);
	        if (prevEmployeeDocOpt.isEmpty()) {
	            prevEmployeeDocOpt = empDocTypeRepository.findByDocName("PrevEmployeeDoc");
	        }
	        
	        Optional<EmpDocType> gratuityDocOpt = empDocTypeRepository.findByDocNameAndIsActive("Gratuity", 1);
	        if (gratuityDocOpt.isEmpty()) {
	            gratuityDocOpt = empDocTypeRepository.findByDocName("Gratuity");
	        }
	        if (gratuityDocOpt.isEmpty()) {
	            gratuityDocOpt = empDocTypeRepository.findByDocName("Gratuity From");
	        }
	        
	        List<EmpDocType> requiredDocTypes = new ArrayList<>(personalDocTypes);
	        if (prevEmployeeDocOpt.isPresent()) {
	            requiredDocTypes.add(prevEmployeeDocOpt.get());
	        }
	        if (gratuityDocOpt.isPresent()) {
	            requiredDocTypes.add(gratuityDocOpt.get());
	        }
	        
	        requiredDocTypes.sort((a, b) -> Integer.compare(a.getDoc_type_id(), b.getDoc_type_id()));
	        
	        List<Integer> requiredDocTypeIds = requiredDocTypes.stream()
	                .mapToInt(EmpDocType::getDoc_type_id)
	                .boxed()
	                .collect(Collectors.toList());
	        
	        List<EmpDocuments> uploadedDocs = empDocumentsRepository.findByEmpIdAndDocTypeIds(empId, requiredDocTypeIds);
	        Set<Integer> uploadedDocTypeIds = uploadedDocs.stream()
	                .map(doc -> doc.getEmp_doc_type_id().getDoc_type_id())
	                .collect(Collectors.toSet());
	        
	        EducationalDocumentStatusDTO response = new EducationalDocumentStatusDTO();
	        response.setEmpId(empId);
	        response.setPayrollId(employee.getPayRollId());
	        response.setTempPayrollId(employee.getTempPayrollId());
	        
	        List<EducationalDocumentStatusDTO.DocumentStatusDTO> requiredDocumentsList = new ArrayList<>();
	        List<EducationalDocumentStatusDTO.DocumentStatusDTO> uploadedDocumentsList = new ArrayList<>();
	        List<EducationalDocumentStatusDTO.DocumentStatusDTO> missingDocumentsList = new ArrayList<>();
	        
	        for (EmpDocType docType : requiredDocTypes) {
	            EducationalDocumentStatusDTO.DocumentStatusDTO docStatus = new EducationalDocumentStatusDTO.DocumentStatusDTO();
	            docStatus.setDocTypeId(docType.getDoc_type_id());
	            docStatus.setDocName(docType.getDoc_name());
	            docStatus.setDocType(docType.getDoc_type());
	            
	            boolean isUploaded = uploadedDocTypeIds.contains(docType.getDoc_type_id());
	            docStatus.setIsUploaded(isUploaded);
	            
	            if (isUploaded) {
	                EmpDocuments uploadedDoc = uploadedDocs.stream()
	                        .filter(doc -> doc.getEmp_doc_type_id().getDoc_type_id() == docType.getDoc_type_id())
	                        .findFirst()
	                        .orElse(null);
	                if (uploadedDoc != null) {
	                    docStatus.setDocPath(uploadedDoc.getDoc_path());
	                }
	                uploadedDocumentsList.add(docStatus);
	            } else {
	                missingDocumentsList.add(docStatus);
	            }
	            requiredDocumentsList.add(docStatus);
	        }
	        
	        response.setRequiredDocuments(requiredDocumentsList);
	        response.setUploadedDocuments(uploadedDocumentsList);
	        response.setMissingDocuments(missingDocumentsList);
	        
	        return response;
	    }

	    /**
	     * Retrieves only the list of specific documents (Personal/Gratuity/PrevEmployee) that have been uploaded.
	     * * @param payrollId The employee's payroll ID.
	     * @return List of uploaded specific documents.
	     */
	    public List<EducationalDocumentStatusDTO.DocumentStatusDTO> getUploadedSpecificDocuments(String payrollId) {
	        logger.info("Getting uploaded specific documents for payroll_id: {}", payrollId);
	        EducationalDocumentStatusDTO status = getSpecificDocumentsStatusByPayrollId(payrollId);
	        return status.getUploadedDocuments();
	    }

	    /**
	     * Retrieves only the list of specific documents (Personal/Gratuity/PrevEmployee) that are missing.
	     * * @param payrollId The employee's payroll ID.
	     * @return List of missing specific documents.
	     */
	    public List<EducationalDocumentStatusDTO.DocumentStatusDTO> getMissingSpecificDocuments(String payrollId) {
	        logger.info("Getting missing specific documents for payroll_id: {}", payrollId);
	        EducationalDocumentStatusDTO status = getSpecificDocumentsStatusByPayrollId(payrollId);
	        return status.getMissingDocuments();
	    }
	  
}