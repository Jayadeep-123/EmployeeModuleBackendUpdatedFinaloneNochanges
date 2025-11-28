
package com.employee.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.employee.dto.AllDocumentsDTO;
import com.employee.dto.BankInfoGetDTO;
import com.employee.dto.CategoryInfoDTO1;
import com.employee.dto.ChequeDetailsDto;
import com.employee.dto.EmpExperienceDetailsDTO;
import com.employee.dto.EmpFamilyDetailsDTO;
import com.employee.dto.EmployeeAgreementDetailsDto;
import com.employee.dto.EmployeeBankDetailsResponseDTO;
import com.employee.dto.FamilyDetailsResponseDTO;
import com.employee.dto.ManagerDTO;
import com.employee.dto.QualificationInfoDTO;
import com.employee.dto.ReferenceDTO;
import com.employee.dto.SkillTestResultDTO;
import com.employee.dto.WorkingInfoDTO;
import com.employee.entity.BankDetails;
import com.employee.entity.EmpChequeDetails;
import com.employee.entity.EmpDocuments;
import com.employee.entity.EmpExperienceDetails;
import com.employee.entity.EmpFamilyDetails;
import com.employee.entity.EmpProfileView;
import com.employee.entity.EmpQualification;
import com.employee.entity.EmpaddressInfo;
import com.employee.entity.Employee;
import com.employee.entity.EmployeeOnboardingView;
import com.employee.entity.SkillTestApprovalView;
import com.employee.exception.ResourceNotFoundException;
import com.employee.repository.BankDetailsRepository;
import com.employee.repository.EmpChequeDetailsRepository;
import com.employee.repository.EmpDocumentsRepository;
import com.employee.repository.EmpExperienceDetailsRepository;
import com.employee.repository.EmpFamilyDetailsRepository;
import com.employee.repository.EmpSubjectRepository;
import com.employee.repository.EmpaddressInfoRepository;
import com.employee.repository.EmployeeOnboardingRepository;
import com.employee.repository.EmployeeProfileViewRepository;
import com.employee.repository.EmployeeRepository;
import com.employee.repository.SkillTestApprovalRepository;
import com.employee.repository.SkillTestResultRepository;

@Service
public class GetEmpDetailsService {
	
	@Autowired EmpFamilyDetailsRepository empFamilyDetailsRepo;
	@Autowired EmpExperienceDetailsRepository empExperienceDetailsRepo;
	@Autowired EmployeeRepository employeeRepo;
	@Autowired BankDetailsRepository bankDetailsRepository;
	@Autowired EmpSubjectRepository empSubjectRepository;
	@Autowired EmployeeProfileViewRepository profileRepo;
	@Autowired EmployeeOnboardingRepository  employeeOnboardingRepo;
	@Autowired SkillTestResultRepository skillTestResultRepository;
	@Autowired EmpChequeDetailsRepository empChequeDetailsRepository;
	@Autowired SkillTestApprovalRepository skillTestApprovalRepository;
	@Autowired EmpDocumentsRepository empDocumentsRepository;
	@Autowired EmpaddressInfoRepository empAddressInfoRepo;
	
	
	
	   public List<EmpFamilyDetailsDTO> getFamilyDetailsByEmpId(int empId) {
	        return empFamilyDetailsRepo.findFamilyDetailsByEmpId(empId);
	    }
	   
	  

	    
//	    public List<EmpExperienceDetailsDTO> getExperienceByTempPayrollId(String tempPayrollId) {
//	        
//	        
//	        List<EmpExperienceDetails> experiences = empExperienceDetailsRepo.findByEmployeeTempPayrollId(tempPayrollId);
//
//	        // 2. Convert entities to DTOs (same logic as before)
//	        return experiences.stream()
//	            .map(entity -> {
//	                EmpExperienceDetailsDTO dto = new EmpExperienceDetailsDTO();
//	                
//	                dto.setCompanyName(entity.getPreOrzanigationName());
//	                dto.setDesignation(entity.getDesignation());
//	                dto.setLeavingReason(entity.getLeavingReason());
//	                dto.setNatureOfDuties(entity.getNatureOfDuties());
//	                dto.setCompanyAddress(entity.getCompanyAddr());
//
//	                if (entity.getDateOfJoin() != null) {
//	                    dto.setFromDate(entity.getDateOfJoin().toLocalDate());
//	                }
//	                if (entity.getDateOfLeave() != null) {
//	                    dto.setToDate(entity.getDateOfLeave().toLocalDate());
//	                }
//
//	                BigDecimal grossSalary = new BigDecimal(entity.getGrossSalary());
//	                dto.setCtc(grossSalary);
//	                dto.setGrossSalaryPerMonth(
//	                    grossSalary.divide(TWELVE, 2, RoundingMode.HALF_UP)
//	                );
//
//	                return dto;
//	            })
//	            .collect(Collectors.toList());
//	    }
	 
	   
	   

	    public List<EmpExperienceDetailsDTO> getExperienceByTempPayrollId(String tempPayrollId) {

	        List<EmpExperienceDetails> experienceList =
	                empExperienceDetailsRepo.findExperienceByTempPayrollId(tempPayrollId);

	        return experienceList.stream()
	                .map(this::convertToDTO)
	                .collect(Collectors.toList());
	    }

	    private EmpExperienceDetailsDTO convertToDTO(EmpExperienceDetails entity) {
	    	EmpExperienceDetailsDTO dto = new EmpExperienceDetailsDTO();

	    	

	    	dto.setCompanyName(entity.getPre_organigation_name());
	    	dto.setDesignation(entity.getDesignation()); // This one was correct
	    	dto.setFromDate(entity.getDate_of_join().toLocalDate());
	    	dto.setToDate(entity.getDate_of_leave().toLocalDate());
	    	dto.setLeavingReason(entity.getLeaving_reason());
	    	dto.setCompanyAddress(entity.getCompany_addr());
	    	dto.setNatureOfDuties(entity.getNature_of_duties());

	    	BigDecimal ctc = BigDecimal.valueOf(entity.getGross_salary());
	    	dto.setCtc(ctc);
	    	dto.setGrossSalaryPerMonth(ctc.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP));


	    	return dto;
	    }
	    
	 // Change the return type from CategoryInfoDTO to CategoryInfoDTO1
	    public List<CategoryInfoDTO1> getCategoryInfo(String TemppayrollId) {
	        return empSubjectRepository.findCategoryInfoByPayrollId(TemppayrollId);
	    }
	    
	    public EmployeeBankDetailsResponseDTO getBankDetailsByTempPayrollId(String tempPayrollId) {

	        // Step 1: Get employee by tempPayrollId
	        Employee employee = employeeRepo.findByTempPayrollId(tempPayrollId)
	                .orElseThrow(() -> new RuntimeException("Employee not found for tempPayrollId: " + tempPayrollId));

	        // Step 2: Get all active bank details for that employee
	        List<BankDetails> bankDetailsList = bankDetailsRepository.findActiveBankDetailsByEmpId(employee.getEmp_id());

	        if (bankDetailsList == null || bankDetailsList.isEmpty()) {
	            throw new RuntimeException("No bank details found for employee ID: " + employee.getEmp_id());
	        }

	        // Step 3: Create response object
	        EmployeeBankDetailsResponseDTO response = new EmployeeBankDetailsResponseDTO();

	        // Step 4: Loop through bank details
	        for (BankDetails bd : bankDetailsList) {

	            BankInfoGetDTO dto = new BankInfoGetDTO();
	            dto.setBankName(bd.getBankName());
	            dto.setBankBranch(bd.getBankBranch());
	            dto.setPersonalAccountHolderName(bd.getBankHolderName());
	            dto.setPersonalAccountNumber(bd.getAccNo());
	            dto.setIfscCode(bd.getIfscCode());

	            String accType = bd.getAccType();

	            // --- For PERSONAL account ---
	            if ("PERSONAL".equalsIgnoreCase(accType)) {
	                response.setPersonalBankInfo(dto);
	            }

	            // --- For SALARY account ---
	            else if ("SALARY".equalsIgnoreCase(accType)) {

	                // Here we need paymentType (foreign key from EmpPaymentType)
	                if (bd.getEmpPaymentType() != null) {
	                    dto.setPaymentType(bd.getEmpPaymentType().getPayment_type());
	                } else {
	                    dto.setPaymentType("N/A");
	                }

	                // Set extra salary-related fields
	                dto.setIsSalaryLessThan40000("Yes"); // Optional logic based on salary amount
	                dto.setPayableAt("Date");

	                response.setSalaryAccountInfo(dto);
	            }
	        }

	        return response;
	    }
	    //EmployeeProfileView
	    public Optional<EmpProfileView> getProfileByPayrollId(String payrollId) {
	    	return profileRepo.findByPayrollId(payrollId);	   
	    }
	    //EmployeeOnboardingView
	    public Optional<EmployeeOnboardingView> getEmployeeOnboardingByTempPayrollId(String tempPayrollId) {
	        return employeeOnboardingRepo.findByTempPayrollId(tempPayrollId);
	    }
	    
	    //MangerDetailsOf the employee
	    public ManagerDTO getManagerDetailsByTempPayrollId(String tempPayrollId) {
	        return employeeRepo.findByTempPayrollId(tempPayrollId)
	                .map(emp -> {
	                    Employee manager = emp.getEmployee_manager_id();
	                    if (manager == null) {
	                        return null; // or throw custom exception
	                    }

	                    return new ManagerDTO(
	                            manager.getFirst_name() + " " + manager.getLast_name(),
	                            manager.getEmail(),
	                            manager.getPrimary_mobile_no(),
	                            manager.getDesignation() != null ? manager.getDesignation().getDesignation_name() : null,	                            manager.getTempPayrollId()
	                    );
	                })
	                .orElse(null);
	    }
	    public ReferenceDTO getReferenceDetailsByTempPayrollId(String tempPayrollId) {
	        return employeeRepo.findByTempPayrollId(tempPayrollId)
	                .map(emp -> {
	                    Employee reference = emp.getEmployee_reference();
	                    if (reference == null) {
	                        return null; // or throw an exception like new EntityNotFoundException("No reference found");
	                    }

	                    return new ReferenceDTO(
	                            reference.getFirst_name() + " " + reference.getLast_name(),
	                            reference.getEmail(),
	                            reference.getPrimary_mobile_no(),
	                            
	                            // --- THIS IS THE FIX ---
	                            // Use the correct getter with an underscore
	                            reference.getDesignation() != null ? reference.getDesignation().getDesignation_name() : null,
	                            
	                            reference.getTempPayrollId()
	                    );
	                })
	                .orElse(null);
	    }
	    
	    public List<SkillTestResultDTO> getSkillTestResultsByPayrollId(String tempPayrollId) {
	        return skillTestResultRepository.findSkillTestDetailsByPayrollId(tempPayrollId);
	    }

//	    @Transactional(readOnly = true)
	    public EmployeeAgreementDetailsDto getAgreementChequeInfo(String tempPayrollId) {
	        Optional<Employee> employeeOpt = employeeRepo.findByTempPayrollId(tempPayrollId);

	        if (employeeOpt.isEmpty()) {
	            throw new RuntimeException("Employee not found for tempPayrollId: " + tempPayrollId);
	        }

	        Employee employee = employeeOpt.get();

	        // Fetch all cheques linked to this employee
	        List<EmpChequeDetails> cheques = empChequeDetailsRepository.findByEmpId_emp_id(employee.getEmp_id());
	        // Map cheque details to DTO list
	        List<ChequeDetailsDto> chequeDtos = cheques.stream()
	                .map(ch -> new ChequeDetailsDto(
	                        ch.getChequeNo(),
	                        ch.getChequeBankName(),
	                        ch.getChequeBankIfscCode()
	                ))
	                .collect(Collectors.toList());

	        // Build DTO
	        EmployeeAgreementDetailsDto dto = new EmployeeAgreementDetailsDto();
	        dto.setAgreementCompany(null); 
	        dto.setAgreementType(employee.getAgreement_type());

	        dto.setNoOfCheques(chequeDtos.size());
	        dto.setCheques(chequeDtos);

	        return dto;
	    }
	    
	    
	    public Optional<SkillTestApprovalView> getSkillTestApprovalDetails(String tempEmployeeId) {
	        // Just call the built-in findById method from the JpaRepository
	        return skillTestApprovalRepository.findById(tempEmployeeId);
	    }

	    
	  public AllDocumentsDTO getAllDocumentsByTempPayrollId(String tempPayrollId) {
	       
	        // Step 1: Find employee by temp_payroll_id
	    	Employee employee = employeeRepo.findByTempPayrollId(tempPayrollId)	               
	    			.orElseThrow(() -> new ResourceNotFoundException(
	                        "Employee not found with temp_payroll_id: " + tempPayrollId));
	       
	        Integer empId = employee.getEmp_id();
	        
	       
	        // Step 2: Get all documents from EmpDocuments table for that emp_id
	        List<EmpDocuments> allDocuments = empDocumentsRepository.findByEmpIdAndIsActive(empId);
	       
	       
	        // Step 3: Build response DTO
	        AllDocumentsDTO response = new AllDocumentsDTO();
	        response.setEmpId(empId);
	        response.setPayrollId(employee.getPayRollId());
	        response.setTempPayrollId(employee.getTempPayrollId());	       
	        // Build documents list
	        List<AllDocumentsDTO.DocumentDetailsDTO> documentsList = new ArrayList<>();
	       
	        for (EmpDocuments empDoc : allDocuments) {
	            AllDocumentsDTO.DocumentDetailsDTO docDetails = new AllDocumentsDTO.DocumentDetailsDTO();
	            docDetails.setEmpDocId(empDoc.getEmp_doc_id());
	           
	            // Get document type information
	            if (empDoc.getEmp_doc_type_id() != null) {
	                docDetails.setDocTypeId(empDoc.getEmp_doc_type_id().getDoc_type_id());
	                docDetails.setDocName(empDoc.getEmp_doc_type_id().getDoc_name());
	                docDetails.setDocType(empDoc.getEmp_doc_type_id().getDoc_type());
	            }
	           
	            docDetails.setDocPath(empDoc.getDoc_path());
	            docDetails.setIsVerified(empDoc.getIs_verified());
	            docDetails.setIsActive(empDoc.getIs_active());
	           
	            documentsList.add(docDetails);
	        }
	       
	        response.setDocuments(documentsList);
	       
	       
	        return response;
	    }
	  
	  
	  public WorkingInfoDTO getWorkingInfoByTempPayrollId(String tempPayrollId) {
	        // 1. Fetch the Employee entity with all required joins
	        Employee employee = employeeRepo.findWorkingInfoByTempPayrollId(tempPayrollId)
	                .orElseThrow(() -> new RuntimeException("Employee not found with tempPayrollId: " + tempPayrollId)); // Use a custom Exception later

	        // 2. Map the Employee entity to the DTO
	        return mapToWorkingInfoDTO(employee);
	    }

	    private WorkingInfoDTO mapToWorkingInfoDTO(Employee e) {
	        WorkingInfoDTO dto = new WorkingInfoDTO();
	        
	        // Employee entity fields
	        dto.setTempPayrollId(e.getTempPayrollId());
	        dto.setJoiningDate(e.getDate_of_join());

	        // Campus related fields (via campus_id)
	        if (e.getCampus_id() != null) {
	            dto.setCampusName(e.getCampus_id().getCampusName());
	            dto.setCampusCode(e.getCampus_id().getCmps_code());
	            dto.setCampusType(e.getCampus_id().getCmps_type());
	            
	            // Location (assuming it's derived from the Campus's City)
	            if (e.getCampus_id().getCity() != null) {
	                dto.setLocation(e.getCampus_id().getCity().getCityName());
	            }
	        }

	        // Note: Building Name is not directly mapped in your Employee entity.
	        // You would need a separate fetch or a direct relationship to fill this.
	        // For now, it is left null/empty.
	        // dto.setBuildingName(...);

	        // Related Employee entities (Manager, Hired By, Replacement)
	        dto.setManagerName(formatEmployeeName(e.getEmployee_manager_id()));
	        dto.setReplacementEmployeeName(formatEmployeeName(e.getEmployee_replaceby_id()));
	        dto.setHiredByName(formatEmployeeName(e.getEmployee_hired()));
	        
	        // Working Mode, Joining As, Mode of Hiring
	        if (e.getWorkingMode_id() != null) {
	            dto.setWorkingModeType(e.getWorkingMode_id().getWork_mode_type());
	        }
	        if (e.getJoin_type_id() != null) {
	            dto.setJoiningAsType(e.getJoin_type_id().getJoin_type());
	        }
	        
	        // Assuming ModeOfHiring has a getType() method (based on entity naming pattern)
	        if (e.getModeOfHiring_id() != null) {
	            // Note: Assuming 'ModeOfHiring' entity exists and has a method to get the type.
	            // Placeholder: Replace with actual method if different.
	            // dto.setModeOfHiringType(e.getModeOfHiring_id().getModeOfHiringType());
	        }

	        return dto;
	    }

	    private String formatEmployeeName(Employee emp) {
	        if (emp != null) {
	            // Basic concatenation for display name
	            return emp.getFirst_name() + " " + emp.getLast_name();
	        }
	        return null; // or "N/A"
	    }
	    
	    public QualificationInfoDTO getHighestQualificationDetails(String tempPayrollId) {
	        
	        // 1. Fetch the Employee record with the highest qualification type
	        Employee employee = employeeRepo.findHighestQualificationDetailsByTempPayrollId(tempPayrollId)
	                .orElseThrow(() -> new RuntimeException("Employee not found with tempPayrollId: " + tempPayrollId));
	        
	        // 2. Fetch the corresponding detailed EmpQualification record
	        EmpQualification empQualification = employeeRepo.findHighestEmpQualificationRecord(tempPayrollId)
	                .orElseThrow(() -> new RuntimeException("Detailed qualification record not found."));

	        // 3. Map the entities to the DTO
	        return mapToQualificationInfoDTO(employee, empQualification);
	    }

	    private QualificationInfoDTO mapToQualificationInfoDTO(Employee e, EmpQualification eq) {
	        QualificationInfoDTO dto = new QualificationInfoDTO();
	        
	        // Qualification (Sourced from Employee.qualification_id)
	        if (e.getQualification_id() != null) {
	            dto.setQualification(e.getQualification_id().getQualification_name());
	        }
	        
	        // Degree, Specialisation, Passed Out Year (Sourced from EmpQualification)
	        // Note: Assuming 'QualificationDegree' entity has a 'getDegree_name()' method
	        if (eq.getQualification_degree_id() != null) {
	            // Placeholder for Degree name
	            // dto.setDegree(eq.getQualification_degree_id().getDegree_name());
	             // Using the Qualification Name for Degree, matching the image example ("B.Tech" for both)
	            if (e.getQualification_id() != null) {
	                 dto.setDegree(e.getQualification_id().getQualification_name());
	            }
	        }
	        
	        dto.setSpecialisation(eq.getSpecialization());
	        dto.setPassedOutYear(eq.getPassedout_year());
	        
	        // Academic Details (Sourced from EmpQualification)
	        dto.setUniversity(eq.getUniversity());
	        dto.setInstitute(eq.getInstitute());
	        
	        // Placeholder for certificate status
	        dto.setCertificateStatus("Available");

	        return dto;
	    }
	    
	    public List<FamilyDetailsResponseDTO> getFamilyDetailsWithAddressInfo(String tempPayrollId) {
	        
	        // 1. Find the Employee
	        Employee employee = employeeRepo.findByTempPayrollId(tempPayrollId)
	                .orElseThrow(() -> new RuntimeException("Employee with tempPayrollId: " + tempPayrollId + " not found."));

	        // 2. Fetch Family Details
	        List<EmpFamilyDetails> familyDetailsList = empFamilyDetailsRepo.findByEmployeeEntity(employee);

	        // 3. Determine State and Country from Address Info
	        List<EmpaddressInfo> addresses = empAddressInfoRepo.findByEmployeeEntity(employee);

	        String stateName = null;
	        String countryName = null;
	        
	        Optional<EmpaddressInfo> permanentAddress = addresses.stream()
	            .filter(a -> "PERM".equalsIgnoreCase(a.getAddrs_type()))
	            .findFirst();
	            
	        Optional<EmpaddressInfo> currentAddress = addresses.stream()
	            .filter(a -> "CURR".equalsIgnoreCase(a.getAddrs_type()))
	            .findFirst();

	        // Logic Implementation:
	        boolean addressesExistAndAreIdentical = permanentAddress.isPresent()
	                                               && currentAddress.isPresent()
	                                               && permanentAddress.get().equals(currentAddress.get());
	                                               // NOTE: equals() needs to be overridden in EmpaddressInfo
	                                               // to compare relevant address fields (house_no, postal_code, etc.)

	        if (addressesExistAndAreIdentical) {
	            // Case 1: Both exist and are the same (take either)
	            stateName = permanentAddress.get().getState_id().getStateName();
	            countryName = permanentAddress.get().getCountry_id().getCountryName();
	        } else if (permanentAddress.isPresent()) {
	            // Case 2: Addresses are different, or only Permanent exists (take Permanent)
	            stateName = permanentAddress.get().getState_id().getStateName();
	            countryName = permanentAddress.get().getCountry_id().getCountryName();
	        } else if (currentAddress.isPresent()) {
	            // Case 3: Only Current address exists (Optional fall-back, use Current)
	             stateName = currentAddress.get().getState_id().getStateName();
	             countryName = currentAddress.get().getCountry_id().getCountryName();
	        }
	        
	        // Use final variables for stream mapping
	        final String finalStateName = stateName;
	        final String finalCountryName = countryName;

	        // 4. Map the entity list to the DTO response list
	        return familyDetailsList.stream()
	            .map(familyDetail -> {
	                FamilyDetailsResponseDTO dto = new FamilyDetailsResponseDTO();
	                
	                // Map family member details
//	                String fullName = familyDetail.getFirst_name() + (familyDetail.getLast_name() != null ? " " + familyDetail.getLast_name() : "");
//	                dto.setName(fullName);
	                
	                
	                dto.setName(familyDetail.getFullName());
	                dto.setAdhaarNo(familyDetail.getAdhaarNo());
	                // Assuming required getters exist on related entities
	                dto.setRelation(familyDetail.getRelation_id().getStudentRelationType());
	                dto.setBloodGroup(familyDetail.getBlood_group_id().getBloodGroupName());
	                
	                dto.setOccupation(familyDetail.getOccupation());
	                dto.setEmailId(familyDetail.getEmail());
	                dto.setPhoneNumber(familyDetail.getContact_no());
	                dto.setNationality(familyDetail.getNationality());
	                
	                // Set the derived address info
	                dto.setState(finalStateName);
	                dto.setCountry(finalCountryName);
	                
	                return dto;
	            })
	            .collect(Collectors.toList());
	    }
	    
	
	    

	    
	    
	    
}
