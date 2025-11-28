package com.employee.service;
 
import java.util.List;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import com.employee.dto.AddressInfoDTO;
import com.employee.dto.BankInfoDTO;
import com.employee.dto.BasicInfoDTO;
import com.employee.dto.CategoryInfoDTO;
import com.employee.dto.DocumentDTO;
import com.employee.dto.EmployeeOnboardingDTO;
import com.employee.dto.FamilyInfoDTO;
import com.employee.dto.PreviousEmployerInfoDTO;
import com.employee.dto.QualificationDTO;
import com.employee.entity.BankDetails;
import com.employee.entity.EmpaddressInfo;
import com.employee.entity.EmpDetails;
import com.employee.entity.EmpDocuments;
import com.employee.entity.EmpExperienceDetails;
import com.employee.entity.EmpFamilyDetails;
import com.employee.entity.EmpPfDetails;
import com.employee.entity.EmpQualification;
import com.employee.entity.Employee;
import com.employee.exception.ResourceNotFoundException;
import com.employee.repository.BloodGroupRepository;
import com.employee.repository.BuildingRepository;
import com.employee.repository.CampusRepository;
import com.employee.repository.CategoryRepository;
import com.employee.repository.CityRepository;
import com.employee.repository.CountryRepository;
import com.employee.repository.DepartmentRepository;
import com.employee.repository.DesignationRepository;
import com.employee.repository.EmpDocTypeRepository;
import com.employee.repository.EmployeeRepository;
import com.employee.repository.EmployeeTypeRepository;
import com.employee.repository.GenderRepository;
import com.employee.repository.JoiningAsRepository;
import com.employee.repository.ModeOfHiringRepository;
import com.employee.repository.OrgBankBranchRepository;
import com.employee.repository.OrgBankRepository;
import com.employee.repository.QualificationDegreeRepository;
import com.employee.repository.QualificationRepository;
import com.employee.repository.RelationRepository;
import com.employee.repository.SkillTestDetailsRepository;
import com.employee.repository.StateRepository;
import com.employee.repository.SubjectRepository;
import com.employee.repository.WorkingModeRepository;
import com.employee.repository.EmpPaymentTypeRepository;
 
/**
 * Service for Employee Validation operations.
 * Contains validation methods extracted from EmployeeService for better organization.
 * * This service handles:
 * - Pre-flight validation checks
 * - Onboarding data validation
 * - Prepared entities validation
 */
@Service
public class EmployeeValidationService {
 
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private GenderRepository genderRepository;
    @Autowired
    private BloodGroupRepository bloodGroupRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private DesignationRepository designationRepository;
    @Autowired
    private EmployeeTypeRepository employeeTypeRepository;
    @Autowired
    private QualificationRepository qualificationRepository;
    @Autowired
    private QualificationDegreeRepository qualificationDegreeRepository;
    @Autowired
    private WorkingModeRepository workingModeRepository;
    @Autowired
    private JoiningAsRepository joiningAsRepository;
    @Autowired
    private ModeOfHiringRepository modeOfHiringRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private StateRepository stateRepository;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private RelationRepository relationRepository;
    @Autowired
    private EmpDocTypeRepository empDocTypeRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private SkillTestDetailsRepository skillTestDetlRepository;
    @Autowired
    private CampusRepository campusRepository;
    @Autowired
    private BuildingRepository buildingRepository;
    @Autowired
    private EmpPaymentTypeRepository empPaymentTypeRepository;
    @Autowired
    private OrgBankRepository orgBankRepository;
    @Autowired
    private OrgBankBranchRepository orgBankBranchRepository;
 
    /**
     * Pre-flight checks: Validate data formats, lengths, and constraints that could cause runtime errors
     * This catches issues that might cause failures AFTER employee.save() (which would consume emp_id)
     * Called AFTER validateOnboardingData but BEFORE any database saves
     */
    public void performPreFlightChecks(EmployeeOnboardingDTO onboardingDTO) {
        if (onboardingDTO == null || onboardingDTO.getBasicInfo() == null) {
            return;
        }
 
        BasicInfoDTO basicInfo = onboardingDTO.getBasicInfo();
 
        if (basicInfo.getFirstName() != null && basicInfo.getFirstName().length() > 50) {
            throw new ResourceNotFoundException("First Name cannot exceed 50 characters");
        }
 
        if (basicInfo.getLastName() != null && basicInfo.getLastName().length() > 50) {
            throw new ResourceNotFoundException("Last Name cannot exceed 50 characters");
        }
 
        // Email validation - email goes to EmpDetails.personal_email only (not Employee entity)
        if (basicInfo.getEmail() != null && basicInfo.getEmail().length() > 50) {
            throw new ResourceNotFoundException("Email cannot exceed 50 characters");
        }
 
        String username = (basicInfo.getFirstName() + "." + basicInfo.getLastName()).toLowerCase();
        if (username.length() > 50) {
            username = username.substring(0, 50);
        }
 
    }
 
    /**
     * Validate all onboarding data BEFORE any database operation
     * This prevents ID consumption when validation fails
     * Call this method BEFORE saveBasicInfo to avoid consuming emp_id
     * * Note: basicInfo can be null when validating only remaining tabs (e.g., in saveRemainingTabs)
     */
    public void validateOnboardingData(EmployeeOnboardingDTO onboardingDTO) {
        if (onboardingDTO == null) {
            throw new ResourceNotFoundException("Employee onboarding data is required");
        }
 
        BasicInfoDTO basicInfo = onboardingDTO.getBasicInfo();
        // Allow basicInfo to be null for remaining tabs validation
        // Only validate basicInfo if it's provided
        if (basicInfo != null) {
 
        if (basicInfo.getFirstName() == null || basicInfo.getFirstName().trim().isEmpty()) {
            throw new ResourceNotFoundException("First Name is required");
        }
 
        if (basicInfo.getLastName() == null || basicInfo.getLastName().trim().isEmpty()) {
            throw new ResourceNotFoundException("Last Name is required");
        }
 
        if (basicInfo.getDateOfJoin() == null) {
            throw new ResourceNotFoundException("Date of Join is required");
        }
 
        if (basicInfo.getPrimaryMobileNo() == null || basicInfo.getPrimaryMobileNo() == 0) {
            throw new ResourceNotFoundException("Primary Mobile Number is required");
        }
 
        // Email is optional - goes to EmpDetails.personal_email only (not Employee entity)
        // Email validation removed - personal_email is nullable in database
 
        if (basicInfo.getGenderId() != null) {
            genderRepository.findById(basicInfo.getGenderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Gender not found with ID: " + basicInfo.getGenderId()));
        } else {
            throw new ResourceNotFoundException("Gender ID is required (NOT NULL column)");
        }
 
        // Note: designationId and departmentId are now handled in CategoryInfoDTO only
        // Validation for these fields should be done when CategoryInfo is saved
 
        if (basicInfo.getCategoryId() != null) {
            categoryRepository.findById(basicInfo.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + basicInfo.getCategoryId()));
        } else {
            throw new ResourceNotFoundException("Category ID is required (NOT NULL column)");
        }
 
        if (basicInfo.getReferenceEmpId() != null && basicInfo.getReferenceEmpId() > 0) {
            employeeRepository.findByIdAndIs_active(basicInfo.getReferenceEmpId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Reference Employee not found with ID: " + basicInfo.getReferenceEmpId()));
        }
 
        if (basicInfo.getHiredByEmpId() != null && basicInfo.getHiredByEmpId() > 0) {
            employeeRepository.findByIdAndIs_active(basicInfo.getHiredByEmpId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Hired By Employee not found with ID: " + basicInfo.getHiredByEmpId()));
        }
 
        if (basicInfo.getManagerId() != null && basicInfo.getManagerId() > 0) {
            employeeRepository.findByIdAndIs_active(basicInfo.getManagerId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Manager not found with ID: " + basicInfo.getManagerId()));
        }
 
        if (basicInfo.getReportingManagerId() != null && basicInfo.getReportingManagerId() > 0) {
            employeeRepository.findByIdAndIs_active(basicInfo.getReportingManagerId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Reporting Manager not found with ID: " + basicInfo.getReportingManagerId()));
        }
 
        if (basicInfo.getReplacedByEmpId() != null && basicInfo.getReplacedByEmpId() > 0) {
            employeeRepository.findByIdAndIs_active(basicInfo.getReplacedByEmpId(), 0)
                    .orElseThrow(() -> new ResourceNotFoundException("Inactive Replacement Employee not found with ID: " + basicInfo.getReplacedByEmpId() + ". Only inactive employees (is_active = 0) can be used as replacement."));
        }
 
        // Validate preChaitanyaId: if entered, must be an inactive employee (is_active = 0)
        if (basicInfo.getPreChaitanyaId() != null && basicInfo.getPreChaitanyaId() > 0) {
            employeeRepository.findByIdAndIs_active(basicInfo.getPreChaitanyaId(), 0)
                    .orElseThrow(() -> new ResourceNotFoundException("Previous Chaitanya Employee not found with ID: " + basicInfo.getPreChaitanyaId() + ". Only inactive employees (is_active = 0) can be used as previous Chaitanya employee."));
        }
 
        if (basicInfo.getCampusId() != null && basicInfo.getCampusId() > 0) {
            campusRepository.findByCampusIdAndIsActive(basicInfo.getCampusId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Campus not found with ID: " + basicInfo.getCampusId()));
        }
 
        if (basicInfo.getEmpTypeId() != null) {
            employeeTypeRepository.findById(basicInfo.getEmpTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee Type not found with ID: " + basicInfo.getEmpTypeId()));
        }
 
        // Note: qualificationId is now passed from BasicInfoDTO (not from qualification tab's isHighest)
        if (basicInfo.getQualificationId() != null) {
            qualificationRepository.findById(basicInfo.getQualificationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Qualification not found with ID: " + basicInfo.getQualificationId()));
        }
 
        if (basicInfo.getEmpWorkModeId() != null) {
            workingModeRepository.findById(basicInfo.getEmpWorkModeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Working Mode not found with ID: " + basicInfo.getEmpWorkModeId()));
        }
 
        if (basicInfo.getJoinTypeId() != null) {
            joiningAsRepository.findById(basicInfo.getJoinTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Join Type not found with ID: " + basicInfo.getJoinTypeId()));
 
            // If joinTypeId = 3 (Replacement), replacedByEmpId is MANDATORY
            if (basicInfo.getJoinTypeId() == 3) {
                if (basicInfo.getReplacedByEmpId() == null || basicInfo.getReplacedByEmpId() <= 0) {
                    throw new ResourceNotFoundException(
                            "replacedByEmpId is required when joinTypeId is 3 (Replacement). Please provide a valid replacement employee ID.");
                }
            }
        }
 
        if (basicInfo.getModeOfHiringId() != null) {
            modeOfHiringRepository.findById(basicInfo.getModeOfHiringId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mode of Hiring not found with ID: " + basicInfo.getModeOfHiringId()));
        }
 
        // Validate BloodGroup ID (REQUIRED - NOT NULL column in EmpDetails)
        if (basicInfo.getBloodGroupId() == null) {
            throw new ResourceNotFoundException("BloodGroup ID is required (NOT NULL column)");
        }
        bloodGroupRepository.findByIdAndIsActive(basicInfo.getBloodGroupId(), 1)
                .orElseThrow(() -> new ResourceNotFoundException("Active BloodGroup not found with ID: " + basicInfo.getBloodGroupId()));
 
        // Validate Caste ID (REQUIRED - NOT NULL column in EmpDetails)
        if (basicInfo.getCasteId() == null) {
            throw new ResourceNotFoundException("Caste ID is required (NOT NULL column)");
        }
        // Note: Caste validation will be done in entity preparation as casteRepository is not available in validation service
        // This check ensures the field is provided
 
        // Validate Religion ID (REQUIRED - NOT NULL column in EmpDetails)
        if (basicInfo.getReligionId() == null) {
            throw new ResourceNotFoundException("Religion ID is required (NOT NULL column)");
        }
        // Note: Religion validation will be done in entity preparation as religionRepository is not available in validation service
        // This check ensures the field is provided
 
        // Validate MaritalStatus ID (REQUIRED - NOT NULL column in EmpDetails)
        if (basicInfo.getMaritalStatusId() == null) {
            throw new ResourceNotFoundException("MaritalStatus ID is required (NOT NULL column)");
        }
        // Note: MaritalStatus validation will be done in entity preparation as maritalStatusRepository is not available in validation service
        // This check ensures the field is provided
 
        // Validate Emergency Relation ID (OPTIONAL - can be null)
        if (basicInfo.getEmergencyRelationId() != null && basicInfo.getEmergencyRelationId() > 0) {
            relationRepository.findById(basicInfo.getEmergencyRelationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Emergency Relation not found with ID: " + basicInfo.getEmergencyRelationId()));
        }
 
        // Validate Emergency Phone Number (REQUIRED - NOT NULL column in EmpDetails)
        if (basicInfo.getEmergencyPhNo() == null || basicInfo.getEmergencyPhNo().trim().isEmpty()) {
            throw new ResourceNotFoundException("Emergency contact phone number (emergencyPhNo) is required (NOT NULL column)");
        }
 
        // Validate Aadhaar Number format IF provided (optional field)
        // CHANGED: Fixed compilation error by handling Long type correctly
        if (basicInfo.getAdhaarNo() != null && basicInfo.getAdhaarNo() > 0) {
            // Convert Long to String for validation
            String aadhar = String.valueOf(basicInfo.getAdhaarNo());
           
            // Layer 1: Format validation - must be exactly 12 numeric digits
            if (!aadhar.matches("^[0-9]{12}$")) {
                throw new ResourceNotFoundException("Aadhaar must be exactly 12 numeric digits.");
            }
           
            // Layer 2: Verhoeff algorithm validation - checks mathematical validity
            if (!isValidAadhaar(aadhar)) {
                throw new ResourceNotFoundException("Invalid Aadhaar number format. Please verify the Aadhaar number and try again.");
            }
        }
 
        // Validate tempPayrollId - check BOTH SkillTestDetails and Employee tables
        // tempPayrollId is valid if it exists in EITHER table
        if (basicInfo.getTempPayrollId() != null && !basicInfo.getTempPayrollId().trim().isEmpty()) {
            String tempPayrollId = basicInfo.getTempPayrollId().trim();
            boolean foundInSkillTest = skillTestDetlRepository.findByTempPayrollId(tempPayrollId).isPresent();
            boolean foundInEmployee = employeeRepository.findByTempPayrollId(tempPayrollId).isPresent();
           
            if (!foundInSkillTest && !foundInEmployee) {
                throw new ResourceNotFoundException("Temp Payroll ID not found in Skill Test Details or Employee table: " + tempPayrollId + ". Please provide a valid temp payroll ID.");
            }
           
            // Note: tempPayrollId is valid if found in EITHER SkillTestDetails OR Employee table
            // The generateOrValidateTempPayrollId method will handle both INSERT (new employee) and UPDATE (existing employee) cases
        }
        } // End of basicInfo validation block
    }
 
    /**
     * Validate all prepared entities before saving to catch any issues before ID consumption
     * This is the FINAL check before employee.save() consumes a sequence number
     */
    public void validatePreparedEntities(Employee employee, EmpDetails empDetails, EmpPfDetails empPfDetails) {
 
        if (employee == null) {
            throw new ResourceNotFoundException("Employee entity cannot be null");
        }
 
        if (employee.getFirst_name() == null || employee.getFirst_name().trim().isEmpty()) {
            throw new ResourceNotFoundException("Employee first name is required");
        }
 
        if (employee.getLast_name() == null || employee.getLast_name().trim().isEmpty()) {
            throw new ResourceNotFoundException("Employee last name is required");
        }
 
        // Email validation removed - email is not stored in Employee entity
 
        if (employee.getCreated_by() == null || employee.getCreated_by() <= 0) {
            throw new ResourceNotFoundException("Employee created_by must be set (NOT NULL column)");
        }
 
        if (empDetails == null) {
            throw new ResourceNotFoundException("EmpDetails entity cannot be null");
        }
 
        if (empDetails.getCreated_by() == null || empDetails.getCreated_by() <= 0) {
            empDetails.setCreated_by(employee.getCreated_by());
        }
 
        if (empDetails.getEmergency_ph_no() == null || empDetails.getEmergency_ph_no().trim().isEmpty()) {
            throw new ResourceNotFoundException("EmpDetails: Emergency Phone Number is required (NOT NULL column)");
        }
 
        if (empPfDetails != null && (empPfDetails.getCreated_by() == null || empPfDetails.getCreated_by() <= 0)) {
            empPfDetails.setCreated_by(employee.getCreated_by());
        }
 
    }
 
    // ============================================================================
    // TAB-BASED VALIDATION METHODS (for EmployeeBasicInfoTabService and EmployeeRemainingTabService)
    // ============================================================================
 
    /**
     * Validate Basic Info DTO for tab-based API
     */
    public void validateBasicInfo(BasicInfoDTO basicInfo, String tempPayrollId) {
        if (basicInfo == null) {
            throw new ResourceNotFoundException("Basic Info is required");
        }
 
        if (basicInfo.getFirstName() == null || basicInfo.getFirstName().trim().isEmpty()) {
            throw new ResourceNotFoundException("First Name is required");
        }
 
        if (basicInfo.getFirstName().length() > 50) {
            throw new ResourceNotFoundException("First Name cannot exceed 50 characters");
        }
 
        if (basicInfo.getLastName() == null || basicInfo.getLastName().trim().isEmpty()) {
            throw new ResourceNotFoundException("Last Name is required");
        }
 
        if (basicInfo.getLastName().length() > 50) {
            throw new ResourceNotFoundException("Last Name cannot exceed 50 characters");
        }
 
        if (basicInfo.getDateOfJoin() == null) {
            throw new ResourceNotFoundException("Date of Join is required");
        }
 
        if (basicInfo.getPrimaryMobileNo() == null || basicInfo.getPrimaryMobileNo() == 0) {
            throw new ResourceNotFoundException("Primary Mobile Number is required");
        }
 
        if (basicInfo.getEmail() != null && basicInfo.getEmail().length() > 50) {
            throw new ResourceNotFoundException("Email cannot exceed 50 characters");
        }
 
        if (basicInfo.getGenderId() != null) {
            genderRepository.findById(basicInfo.getGenderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Gender not found with ID: " + basicInfo.getGenderId()));
        } else {
            throw new ResourceNotFoundException("Gender ID is required (NOT NULL column)");
        }
 
        // Note: designationId and departmentId are now handled in CategoryInfoDTO only
        // Validation for these fields should be done when CategoryInfo is saved
 
        if (basicInfo.getCategoryId() != null) {
            categoryRepository.findById(basicInfo.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + basicInfo.getCategoryId()));
        } else {
            throw new ResourceNotFoundException("Category ID is required (NOT NULL column)");
        }
 
        if (basicInfo.getReferenceEmpId() != null && basicInfo.getReferenceEmpId() > 0) {
            employeeRepository.findByIdAndIs_active(basicInfo.getReferenceEmpId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Reference Employee not found with ID: " + basicInfo.getReferenceEmpId()));
        }
 
        if (basicInfo.getHiredByEmpId() != null && basicInfo.getHiredByEmpId() > 0) {
            employeeRepository.findByIdAndIs_active(basicInfo.getHiredByEmpId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Hired By Employee not found with ID: " + basicInfo.getHiredByEmpId()));
        }
 
        if (basicInfo.getManagerId() != null && basicInfo.getManagerId() > 0) {
            employeeRepository.findByIdAndIs_active(basicInfo.getManagerId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Manager not found with ID: " + basicInfo.getManagerId()));
        }
 
        if (basicInfo.getReportingManagerId() != null && basicInfo.getReportingManagerId() > 0) {
            employeeRepository.findByIdAndIs_active(basicInfo.getReportingManagerId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Reporting Manager not found with ID: " + basicInfo.getReportingManagerId()));
        }
 
        if (basicInfo.getReplacedByEmpId() != null && basicInfo.getReplacedByEmpId() > 0) {
            employeeRepository.findByIdAndIs_active(basicInfo.getReplacedByEmpId(), 0)
                    .orElseThrow(() -> new ResourceNotFoundException("Inactive Replacement Employee not found with ID: " + basicInfo.getReplacedByEmpId() + ". Only inactive employees (is_active = 0) can be used as replacement."));
        }
 
        // Validate preChaitanyaId: if entered, must be an inactive employee (is_active = 0)
        if (basicInfo.getPreChaitanyaId() != null && basicInfo.getPreChaitanyaId() > 0) {
            employeeRepository.findByIdAndIs_active(basicInfo.getPreChaitanyaId(), 0)
                    .orElseThrow(() -> new ResourceNotFoundException("Previous Chaitanya Employee not found with ID: " + basicInfo.getPreChaitanyaId() + ". Only inactive employees (is_active = 0) can be used as previous Chaitanya employee."));
        }
 
        if (basicInfo.getCampusId() != null && basicInfo.getCampusId() > 0) {
            campusRepository.findByCampusIdAndIsActive(basicInfo.getCampusId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Campus not found with ID: " + basicInfo.getCampusId()));
        }
 
        if (basicInfo.getEmpTypeId() != null) {
            employeeTypeRepository.findById(basicInfo.getEmpTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee Type not found with ID: " + basicInfo.getEmpTypeId()));
        }
 
        if (basicInfo.getEmpWorkModeId() != null) {
            workingModeRepository.findById(basicInfo.getEmpWorkModeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Working Mode not found with ID: " + basicInfo.getEmpWorkModeId()));
        }
 
        if (basicInfo.getJoinTypeId() != null) {
            joiningAsRepository.findById(basicInfo.getJoinTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Join Type not found with ID: " + basicInfo.getJoinTypeId()));
 
            if (basicInfo.getJoinTypeId() == 3) {
                if (basicInfo.getReplacedByEmpId() == null || basicInfo.getReplacedByEmpId() <= 0) {
                    throw new ResourceNotFoundException(
                            "replacedByEmpId is required when joinTypeId is 3 (Replacement). Please provide a valid replacement employee ID.");
                }
            }
        }
 
        if (basicInfo.getModeOfHiringId() != null) {
            modeOfHiringRepository.findById(basicInfo.getModeOfHiringId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mode of Hiring not found with ID: " + basicInfo.getModeOfHiringId()));
        }
 
        // Validate tempPayrollId - check BOTH SkillTestDetails and Employee tables
        // tempPayrollId is valid if it exists in EITHER table
        if (tempPayrollId != null && !tempPayrollId.trim().isEmpty()) {
            String trimmedTempPayrollId = tempPayrollId.trim();
            boolean foundInSkillTest = skillTestDetlRepository.findByTempPayrollId(trimmedTempPayrollId).isPresent();
            boolean foundInEmployee = employeeRepository.findByTempPayrollId(trimmedTempPayrollId).isPresent();
           
            if (!foundInSkillTest && !foundInEmployee) {
                throw new ResourceNotFoundException("Temp Payroll ID not found in Skill Test Details or Employee table: " + trimmedTempPayrollId + ". Please provide a valid temp payroll ID.");
            }
           
            // Note: tempPayrollId is valid if found in EITHER SkillTestDetails OR Employee table
        }
    }
 
 
    /**
     * Validate Aadhaar number using Verhoeff algorithm
     * This algorithm checks the mathematical validity of the Aadhaar number structure
     * * @param aadhaar 12-digit Aadhaar number
     * @return true if Aadhaar format is valid, false otherwise
     */
    private boolean isValidAadhaar(String aadhaar) {
 
        if (aadhaar == null || !aadhaar.matches("\\d{12}")) {
            return false;
        }
 
        // Aadhaar cannot start with 0 or 1 (UIDAI rule)
        if (aadhaar.charAt(0) == '0' || aadhaar.charAt(0) == '1') {
            return false;
        }
 
        final int[][] d = {
                {0,1,2,3,4,5,6,7,8,9},
                {1,2,3,4,0,6,7,8,9,5},
                {2,3,4,0,1,7,8,9,5,6},
                {3,4,0,1,2,8,9,5,6,7},
                {4,0,1,2,3,9,5,6,7,8},
                {5,9,8,7,6,0,4,3,2,1},
                {6,5,9,8,7,1,0,4,3,2},
                {7,6,5,9,8,2,1,0,4,3},
                {8,7,6,5,9,3,2,1,0,4},
                {9,8,7,6,5,4,3,2,1,0}
        };
 
        final int[][] p = {
                {0,1,2,3,4,5,6,7,8,9},
                {1,5,7,6,2,8,3,0,9,4},
                {5,8,0,3,7,9,6,1,4,2},
                {8,9,1,6,0,4,3,5,2,7},
                {9,4,5,3,1,2,6,8,7,0},
                {4,2,8,6,5,7,3,9,0,1},
                {2,7,9,3,8,0,6,4,1,5},
                {7,0,4,6,9,1,3,2,5,8}
        };
 
        int c = 0;
        int[] arr = new int[aadhaar.length()];
 
        // Reverse digits
        for (int i = 0; i < aadhaar.length(); i++) {
            arr[i] = Character.getNumericValue(aadhaar.charAt(aadhaar.length() - 1 - i));
        }
 
        // Apply Verhoeff checksum
        for (int i = 0; i < arr.length; i++) {
            c = d[c][p[i % 8][arr[i]]];
        }
 
        return c == 0;
    }
}
 