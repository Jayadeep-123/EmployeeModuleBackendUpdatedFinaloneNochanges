package com.employee.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employee.dto.AddressInfoDTO;
import com.employee.dto.BasicInfoDTO;
import com.employee.dto.FamilyInfoDTO;
import com.employee.dto.PreviousEmployerInfoDTO;
import com.employee.entity.Building;
import com.employee.entity.EmpaddressInfo;
import com.employee.entity.EmpDetails;
import com.employee.entity.EmpDocuments;
import com.employee.entity.EmpExperienceDetails;
import com.employee.entity.EmpFamilyDetails;
import com.employee.entity.EmpPfDetails;
import com.employee.entity.Employee;
import com.employee.entity.EmployeeCheckListStatus;
import com.employee.exception.ResourceNotFoundException;
import com.employee.repository.BloodGroupRepository;
import com.employee.repository.BuildingRepository;
import com.employee.repository.CampusRepository;
import com.employee.repository.CasteRepository;
import com.employee.repository.CategoryRepository;
import com.employee.repository.CityRepository;
import com.employee.repository.CountryRepository;
import com.employee.repository.DepartmentRepository;
import com.employee.repository.DesignationRepository;
import com.employee.repository.EmpDetailsRepository;
import com.employee.repository.EmpDocTypeRepository;
import com.employee.repository.EmpDocumentsRepository;
import com.employee.repository.EmpExperienceDetailsRepository;
import com.employee.repository.EmpFamilyDetailsRepository;
import com.employee.repository.EmpPfDetailsRepository;
import com.employee.repository.EmpaddressInfoRepository;
import com.employee.repository.EmployeeCheckListStatusRepository;
import com.employee.repository.EmployeeRepository;
import com.employee.repository.EmployeeTypeRepository;
import com.employee.repository.GenderRepository;
import com.employee.repository.JoiningAsRepository;
import com.employee.repository.MaritalStatusRepository;
import com.employee.repository.ModeOfHiringRepository;
import com.employee.repository.RelationRepository;
import com.employee.repository.RelegionRepository;
import com.employee.repository.SkillTestDetailsRepository;
import com.employee.repository.StateRepository;
import com.employee.repository.WorkingModeRepository;
import com.employee.repository.OccupationRepository;
import com.employee.repository.QualificationRepository;

/**
 * Service for handling Basic Info related tabs (4 APIs).
 * Contains: Basic Info, Address Info, Family Info, Previous Employer Info
 * * This service is completely independent and does not use EmployeeEntityPreparationService.
 * All helper methods are implemented directly within this service.
 */
@Service
@Transactional
public class EmployeeBasicInfoTabService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeBasicInfoTabService.class);

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private QualificationRepository qualificationRepository;

    @Autowired
    private EmpDetailsRepository empDetailsRepository;

    @Autowired
    private EmpPfDetailsRepository empPfDetailsRepository;

    @Autowired
    private EmpDocumentsRepository empDocumentsRepository;

    @Autowired
    private EmployeeCheckListStatusRepository employeeCheckListStatusRepository;

    @Autowired
    private EmpaddressInfoRepository empaddressInfoRepository;

    @Autowired
    private EmpFamilyDetailsRepository empFamilyDetailsRepository;

    @Autowired
    private EmpExperienceDetailsRepository empExperienceDetailsRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private RelationRepository relationRepository;

    @Autowired
    private GenderRepository genderRepository;

    @Autowired
    private BloodGroupRepository bloodGroupRepository;

    @Autowired
    private EmpDocTypeRepository empDocTypeRepository;

    @Autowired
    private CampusRepository campusRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private EmployeeTypeRepository employeeTypeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MaritalStatusRepository maritalStatusRepository;

    @Autowired
    private WorkingModeRepository workingModeRepository;

    @Autowired
    private JoiningAsRepository joiningAsRepository;

    @Autowired
    private ModeOfHiringRepository modeOfHiringRepository;

    @Autowired
    private CasteRepository casteRepository;

    @Autowired
    private RelegionRepository relegionRepository;

    @Autowired
    private OccupationRepository occupationRepository;

    @Autowired
    private SkillTestDetailsRepository skillTestDetailsRepository;

    // ============================================================================
    // API METHODS (4 APIs)
    // ============================================================================

    /**
     * API 1: Save Basic Info (Tab 1)
     * Creates or updates Employee, EmpDetails, and EmpPfDetails
     * * @param basicInfo Basic Info DTO (contains empId and tempPayrollId)
     * @return Saved BasicInfoDTO with empId
     */
    public BasicInfoDTO saveBasicInfo(BasicInfoDTO basicInfo) {
        Integer empId = basicInfo.getEmpId();
        String tempPayrollId = basicInfo.getTempPayrollId();
        logger.info("Saving Basic Info for empId: {}, tempPayrollId: {}", empId, tempPayrollId);

        try {
            // Step 1: Validate DTO data BEFORE any database operations
            validateBasicInfo(basicInfo, tempPayrollId);
        } catch (Exception e) {
            logger.error("❌ ERROR: Basic Info validation failed. NO ID consumed. Error: {}", e.getMessage(), e);
            throw e;
        }

        Employee employee = null;
        boolean isUpdate = false;

        // Step 2: Check if employee exists (read-only operation)
        if (empId != null && empId > 0) {
            employee = employeeRepository.findById(empId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + empId));
            isUpdate = true;
            logger.info("UPDATE MODE: Updating existing employee (emp_id: {})", empId);
        } else if (tempPayrollId != null && !tempPayrollId.trim().isEmpty()) {
            Optional<Employee> existing = employeeRepository.findByTempPayrollId(tempPayrollId.trim());
            if (existing.isPresent()) {
                employee = existing.get();
                isUpdate = true;
                logger.info("UPDATE MODE: Found existing employee by tempPayrollId: {}", tempPayrollId);
            } else {
                isUpdate = false;
                logger.info("INSERT MODE: Creating new employee with tempPayrollId: {}", tempPayrollId);
            }
        } else {
            throw new ResourceNotFoundException("Either empId or tempPayrollId must be provided");
        }

        try {
            // Step 3: Prepare all entities in memory (NO database writes yet)
            if (!isUpdate) {
                // INSERT MODE: Create new employee entity
                employee = prepareEmployeeEntity(basicInfo);
                employee.setTempPayrollId(tempPayrollId.trim());
                setIncompletedStatus(employee);
            } else {
                // UPDATE MODE: Update existing employee entity
                updateEmployeeEntity(employee, basicInfo);
                // Set updated_by and updated_date ONLY if status is "Confirm"
                if (employee.getEmp_check_list_status_id() != null) {
                    String currentStatus = employee.getEmp_check_list_status_id().getCheck_app_status_name();
                    if ("Confirm".equals(currentStatus)) {
                        Integer updatedBy = basicInfo.getUpdatedBy() != null ? basicInfo.getUpdatedBy() : 1;
                        employee.setUpdated_by(updatedBy);
                        employee.setUpdated_date(new Timestamp(System.currentTimeMillis()));
                    }
                }
            }

            // Prepare EmpDetails and EmpPfDetails entities (in memory only)
            EmpDetails empDetails = prepareEmpDetailsEntity(basicInfo, employee);
            EmpPfDetails empPfDetails = prepareEmpPfDetailsEntity(basicInfo, employee);

            // Step 4: Validate prepared entities BEFORE saving
            validatePreparedEntities(employee, empDetails, empPfDetails);

            // Step 5: Save to database ONLY after all validations pass
            employee = employeeRepository.save(employee);
            logger.info("✅ Employee ID {} {} - proceeding with child entity saves",
                    isUpdate ? "updated" : "generated and consumed from sequence", employee.getEmp_id());

            // Save EmpDetails
            empDetails.setEmployee_id(employee);
            Integer updatedBy = basicInfo.getUpdatedBy() != null ? basicInfo.getUpdatedBy() : 1;
            saveEmpDetailsEntity(empDetails, employee, isUpdate ? updatedBy : null);

            // Save EmpPfDetails
            if (empPfDetails != null) {
                empPfDetails.setEmployee_id(employee);
                saveEmpPfDetailsEntity(empPfDetails, employee, isUpdate ? updatedBy : null);
            }

            basicInfo.setEmpId(employee.getEmp_id());
            logger.info("✅ Basic Info saved successfully for emp_id: {}", employee.getEmp_id());
            return basicInfo;

        } catch (Exception e) {
            if (employee != null && employee.getEmp_id() > 0) {
                logger.error("❌ ERROR: Basic Info save failed AFTER ID consumption. Employee ID {} was consumed but transaction rolled back. Root cause: {}",
                        employee.getEmp_id(), e.getMessage(), e);
            } else {
                logger.error("❌ ERROR: Basic Info save failed DURING PREPARATION. NO ID consumed. Error: {}",
                        e.getMessage(), e);
            }
            throw e;
        }
    }

    /**
     * API 2: Save Address Info (Tab 2)
     * * @param tempPayrollId Temp Payroll ID
     * @param addressInfo Address Info DTO
     * @return Saved AddressInfoDTO object
     */
    public AddressInfoDTO saveAddressInfo(String tempPayrollId, AddressInfoDTO addressInfo) {
        logger.info("Saving Address Info for tempPayrollId: {}", tempPayrollId);

        try {
            // Step 1: Validate DTO data BEFORE any database operations
            validateAddressInfo(addressInfo);
        } catch (Exception e) {
            logger.error("❌ ERROR: Address Info validation failed. NO data saved. Error: {}", e.getMessage(), e);
            throw e;
        }

        try {
            // Step 2: Find employee (read-only operation)
            Employee employee = findEmployeeByTempPayrollId(tempPayrollId);

            // Step 3: Save to database ONLY after all validations pass
            Integer createdBy = addressInfo.getCreatedBy();
            Integer updatedBy = addressInfo.getUpdatedBy();
            int count = saveAddressEntities(employee, addressInfo, createdBy, updatedBy);

            logger.info("✅ Saved {} address records for emp_id: {} (tempPayrollId: {})", 
                    count, employee.getEmp_id(), tempPayrollId);
            
            // Return the saved DTO object
            return addressInfo;

        } catch (Exception e) {
            logger.error("❌ ERROR: Address Info save failed. Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * API 3: Save Family Info (Tab 3)
     * * @param tempPayrollId Temp Payroll ID
     * @param familyInfo Family Info DTO
     * @return Saved FamilyInfoDTO object
     */
    public FamilyInfoDTO saveFamilyInfo(String tempPayrollId, FamilyInfoDTO familyInfo) {
        logger.info("Saving Family Info for tempPayrollId: {}", tempPayrollId);

        try {
            // Step 1: Validate DTO data BEFORE any database operations
            validateFamilyInfo(familyInfo);
        } catch (Exception e) {
            logger.error("❌ ERROR: Family Info validation failed. NO data saved. Error: {}", e.getMessage(), e);
            throw e;
        }

        try {
            // Step 2: Find employee (read-only operation)
            Employee employee = findEmployeeByTempPayrollId(tempPayrollId);

            // Step 3: Save to database ONLY after all validations pass
            Integer createdBy = familyInfo.getCreatedBy();
            Integer updatedBy = familyInfo.getUpdatedBy();
            int count = saveFamilyEntities(employee, familyInfo, createdBy, updatedBy);
            saveFamilyGroupPhoto(employee, familyInfo);

            logger.info("✅ Saved {} family member records for emp_id: {} (tempPayrollId: {})", 
                    count, employee.getEmp_id(), tempPayrollId);
            
            // Return the saved DTO object
            return familyInfo;

        } catch (Exception e) {
            logger.error("❌ ERROR: Family Info save failed. Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * API 4: Save Previous Employer Info (Tab 4)
     * * @param tempPayrollId Temp Payroll ID
     * @param previousEmployerInfo Previous Employer Info DTO
     * @return Saved PreviousEmployerInfoDTO object
     */
    public PreviousEmployerInfoDTO savePreviousEmployerInfo(String tempPayrollId, PreviousEmployerInfoDTO previousEmployerInfo) {
        logger.info("Saving Previous Employer Info for tempPayrollId: {}", tempPayrollId);

        try {
            // Step 1: Validate DTO data BEFORE any database operations
            validatePreviousEmployerInfo(previousEmployerInfo);
        } catch (Exception e) {
            logger.error("❌ ERROR: Previous Employer Info validation failed. NO data saved. Error: {}", e.getMessage(), e);
            throw e;
        }

        try {
            // Step 2: Find employee (read-only operation)
            Employee employee = findEmployeeByTempPayrollId(tempPayrollId);

            // Step 3: Save to database ONLY after all validations pass
            Integer createdBy = previousEmployerInfo.getCreatedBy();
            Integer updatedBy = previousEmployerInfo.getUpdatedBy();
            int count = saveExperienceEntities(employee, previousEmployerInfo, createdBy, updatedBy);

            logger.info("✅ Saved {} experience records for emp_id: {} (tempPayrollId: {})", 
                    count, employee.getEmp_id(), tempPayrollId);
            
            // Return the saved DTO object
            return previousEmployerInfo;

        } catch (Exception e) {
            logger.error("❌ ERROR: Previous Employer Info save failed. Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    // ============================================================================
    // HELPER METHODS - Employee Operations
    // ============================================================================

    /**
     * Helper: Save EmpDetails entity (handles update/create logic)
     */
    private void saveEmpDetailsEntity(EmpDetails empDetails, Employee employee, Integer updatedBy) {
        Optional<EmpDetails> existingDetails = empDetailsRepository.findById(employee.getEmp_id());
        if (existingDetails.isPresent()) {
            updateEmpDetailsFields(existingDetails.get(), empDetails);
            // Set updated_by and updated_date for UPDATE mode
            if (updatedBy != null) {
                existingDetails.get().setUpdated_by(updatedBy);
                existingDetails.get().setUpdated_date(new Timestamp(System.currentTimeMillis()));
            }
            empDetailsRepository.save(existingDetails.get());
        } else {
            // Check by email
            if (empDetails.getPersonal_email() != null && !empDetails.getPersonal_email().trim().isEmpty()) {
                Optional<EmpDetails> existingByEmail = empDetailsRepository
                        .findByPersonal_email(empDetails.getPersonal_email().trim());
                if (existingByEmail.isPresent()) {
                    updateEmpDetailsFieldsExceptEmail(existingByEmail.get(), empDetails);
                    existingByEmail.get().setEmployee_id(employee);
                    empDetailsRepository.save(existingByEmail.get());
                } else {
                    empDetailsRepository.save(empDetails);
                }
            } else {
                empDetailsRepository.save(empDetails);
            }
        }
    }

    /**
     * Helper: Save EmpPfDetails entity (handles update/create logic)
     */
    private void saveEmpPfDetailsEntity(EmpPfDetails empPfDetails, Employee employee, Integer updatedBy) {
        Optional<EmpPfDetails> existingPf = empPfDetailsRepository.findByEmployeeId(employee.getEmp_id());
        if (existingPf.isPresent()) {
            EmpPfDetails existing = existingPf.get();
            existing.setPre_esi_no(empPfDetails.getPre_esi_no());
            existing.setIs_active(empPfDetails.getIs_active());
            // Set updated_by and updated_date for UPDATE mode
            if (updatedBy != null) {
                existing.setUpdated_by(updatedBy);
                existing.setUpdated_date(new Timestamp(System.currentTimeMillis()));
            }
            empPfDetailsRepository.save(existing);
        } else {
            empPfDetailsRepository.save(empPfDetails);
        }
    }

    /**
     * Helper: Prepare Employee entity WITHOUT saving
     */
    private Employee prepareEmployeeEntity(BasicInfoDTO basicInfo) {
        if (basicInfo == null) {
            throw new ResourceNotFoundException("Basic Info is required");
        }

        Employee employee = new Employee();
        employee.setFirst_name(basicInfo.getFirstName());
        employee.setLast_name(basicInfo.getLastName());
        employee.setDate_of_join(basicInfo.getDateOfJoin());
        employee.setPrimary_mobile_no(basicInfo.getPrimaryMobileNo());
        employee.setEmail(null); // Email goes to EmpDetails.personal_email only

        if (basicInfo.getTotalExperience() != null) {
            employee.setTotal_experience(basicInfo.getTotalExperience().doubleValue());
        }

        if (basicInfo.getAge() != null) {
            employee.setAge(basicInfo.getAge());
        }

        if (basicInfo.getSscNo() != null) {
            employee.setSsc_no(basicInfo.getSscNo());
        }

        employee.setIs_active(1);
        employee.setStatus("ACTIVE");

        if (basicInfo.getTempPayrollId() != null && !basicInfo.getTempPayrollId().trim().isEmpty()) {
            employee.setTempPayrollId(basicInfo.getTempPayrollId());
        }

        // Set created_by only if provided from frontend, otherwise leave as null (entity default will handle)
        if (basicInfo.getCreatedBy() != null && basicInfo.getCreatedBy() > 0) {
            employee.setCreated_by(basicInfo.getCreatedBy());
        }

        if (basicInfo.getCampusId() != null) {
            employee.setCampus_id(campusRepository.findByCampusIdAndIsActive(basicInfo.getCampusId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Campus not found with ID: " + basicInfo.getCampusId())));
        }

        // Update building_id - optional field
        if (basicInfo.getBuildingId() != null && basicInfo.getBuildingId() > 0) {
            Building building = buildingRepository.findById(basicInfo.getBuildingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Building not found with ID: " + basicInfo.getBuildingId()));
            // Validate building is active
            if (building.getIsActive() != 1) {
                throw new ResourceNotFoundException("Building with ID: " + basicInfo.getBuildingId() + " is not active");
            }
            employee.setBuilding_id(building);
        } else if (basicInfo.getBuildingId() != null && basicInfo.getBuildingId() == 0) {
            // Explicitly set to null if 0 is provided
            employee.setBuilding_id(null);
        }
        // If buildingId is null, keep existing value

        if (basicInfo.getGenderId() != null) {
            employee.setGender(genderRepository.findByIdAndIsActive(basicInfo.getGenderId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Gender not found")));
        }

        // Note: designationId and departmentId are now handled in CategoryInfoDTO only
        // Removed from BasicInfoDTO to avoid conflicts - these should be set via saveCategoryInfo API

        if (basicInfo.getCategoryId() != null) {
            employee.setCategory(categoryRepository.findByIdAndIsActive(basicInfo.getCategoryId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Category not found")));
        }

        if (basicInfo.getEmpTypeId() != null) {
            employee.setEmployee_type_id(employeeTypeRepository.findByIdAndIsActive(basicInfo.getEmpTypeId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active EmployeeType not found")));
        }

        if (basicInfo.getQualificationId() != null) {
            employee.setQualification_id(qualificationRepository.findById(basicInfo.getQualificationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Qualification not found with ID: " + basicInfo.getQualificationId())));
        }

        if (basicInfo.getEmpWorkModeId() != null) {
            employee.setWorkingMode_id(workingModeRepository.findByIdAndIsActive(basicInfo.getEmpWorkModeId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active WorkingMode not found")));
        }

        if (basicInfo.getJoinTypeId() != null) {
            employee.setJoin_type_id(joiningAsRepository.findByIdAndIsActive(basicInfo.getJoinTypeId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active JoiningAs not found")));

            if (basicInfo.getJoinTypeId() == 3) {
                if (basicInfo.getReplacedByEmpId() == null || basicInfo.getReplacedByEmpId() <= 0) {
                    throw new ResourceNotFoundException(
                            "replacedByEmpId is required when joinTypeId is 3 (Replacement). Please provide a valid replacement employee ID.");
                }
            }

            if (basicInfo.getJoinTypeId() == 4) {
                if (basicInfo.getContractStartDate() != null) {
                    employee.setContract_start_date(basicInfo.getContractStartDate());
                } else {
                    employee.setContract_start_date(basicInfo.getDateOfJoin());
                }

                if (basicInfo.getContractEndDate() != null) {
                    employee.setContract_end_date(basicInfo.getContractEndDate());
                } else {
                    java.sql.Date startDate = basicInfo.getContractStartDate() != null ?
                            basicInfo.getContractStartDate() : basicInfo.getDateOfJoin();
                    if (startDate != null) {
                        long oneYearInMillis = 365L * 24 * 60 * 60 * 1000;
                        java.util.Date endDateUtil = new java.util.Date(startDate.getTime() + oneYearInMillis);
                        employee.setContract_end_date(new java.sql.Date(endDateUtil.getTime()));
                    }
                }
            }
        }

        if (basicInfo.getModeOfHiringId() != null) {
            employee.setModeOfHiring_id(modeOfHiringRepository.findByIdAndIsActive(basicInfo.getModeOfHiringId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active ModeOfHiring not found")));
        }

        // Handle reference employees
        if (basicInfo.getReferenceEmpId() != null && basicInfo.getReferenceEmpId() > 0) {
            employee.setEmployee_reference(employeeRepository.findByIdAndIs_active(basicInfo.getReferenceEmpId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Reference Employee not found with ID: " + basicInfo.getReferenceEmpId())));
        } else {
            employee.setEmployee_reference(null);
        }

        if (basicInfo.getHiredByEmpId() != null && basicInfo.getHiredByEmpId() > 0) {
            employee.setEmployee_hired(employeeRepository.findByIdAndIs_active(basicInfo.getHiredByEmpId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Hired By Employee not found with ID: " + basicInfo.getHiredByEmpId())));
        } else {
            employee.setEmployee_hired(null);
        }

        if (basicInfo.getManagerId() != null && basicInfo.getManagerId() > 0) {
            employee.setEmployee_manager_id(employeeRepository.findByIdAndIs_active(basicInfo.getManagerId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Manager not found with ID: " + basicInfo.getManagerId())));
        } else {
            employee.setEmployee_manager_id(null);
        }

        if (basicInfo.getReportingManagerId() != null && basicInfo.getReportingManagerId() > 0) {
            employee.setEmployee_reporting_id(employeeRepository.findByIdAndIs_active(basicInfo.getReportingManagerId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Reporting Manager not found with ID: " + basicInfo.getReportingManagerId())));
        } else {
            employee.setEmployee_reporting_id(null);
        }

        if (basicInfo.getJoinTypeId() != null && basicInfo.getJoinTypeId() == 3) {
            if (basicInfo.getReplacedByEmpId() == null || basicInfo.getReplacedByEmpId() <= 0) {
                throw new ResourceNotFoundException(
                        "replacedByEmpId is required when joinTypeId is 3 (Replacement). Please provide a valid replacement employee ID.");
            }
            employee.setEmployee_replaceby_id(employeeRepository.findByIdAndIs_active(basicInfo.getReplacedByEmpId(), 0)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Inactive Replacement Employee not found with ID: " + basicInfo.getReplacedByEmpId())));
        } else if (basicInfo.getReplacedByEmpId() != null && basicInfo.getReplacedByEmpId() > 0) {
            employee.setEmployee_replaceby_id(employeeRepository.findByIdAndIs_active(basicInfo.getReplacedByEmpId(), 0)
                    .orElse(null));
        } else {
            employee.setEmployee_replaceby_id(null);
        }

        // Handle preChaitanyaId: if entered, must be an inactive employee (is_active = 0), if not entered, set to null
        if (basicInfo.getPreChaitanyaId() != null && basicInfo.getPreChaitanyaId() > 0) {
            employee.setPre_chaitanya_id(employeeRepository.findByIdAndIs_active(basicInfo.getPreChaitanyaId(), 0)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Previous Chaitanya Employee not found with ID: " + basicInfo.getPreChaitanyaId() + ". Only inactive employees (is_active = 0) can be used as previous Chaitanya employee.")));
        } else {
            employee.setPre_chaitanya_id(null);
        }

        return employee;
    }

    /**
     * Helper: Update existing employee entity with new data from BasicInfoDTO
     */
    private void updateEmployeeEntity(Employee employee, BasicInfoDTO basicInfo) {
        if (basicInfo == null) {
            return;
        }

        logger.info("🔄 Updating employee entity (emp_id: {}) with new data", employee.getEmp_id());

        if (basicInfo.getFirstName() != null && !basicInfo.getFirstName().trim().isEmpty()) {
            employee.setFirst_name(basicInfo.getFirstName());
        }

        if (basicInfo.getLastName() != null && !basicInfo.getLastName().trim().isEmpty()) {
            employee.setLast_name(basicInfo.getLastName());
        }

        if (basicInfo.getDateOfJoin() != null) {
            employee.setDate_of_join(basicInfo.getDateOfJoin());
        }

        if (basicInfo.getPrimaryMobileNo() != null && basicInfo.getPrimaryMobileNo() > 0) {
            employee.setPrimary_mobile_no(basicInfo.getPrimaryMobileNo());
        }

        employee.setEmail(null);

        if (basicInfo.getTotalExperience() != null) {
            employee.setTotal_experience(basicInfo.getTotalExperience().doubleValue());
        }

        if (basicInfo.getAge() != null) {
            employee.setAge(basicInfo.getAge());
        }

        if (basicInfo.getSscNo() != null) {
            employee.setSsc_no(basicInfo.getSscNo());
        }

        if (basicInfo.getTempPayrollId() != null && !basicInfo.getTempPayrollId().trim().isEmpty()) {
            employee.setTempPayrollId(basicInfo.getTempPayrollId());
        }

        if (basicInfo.getCreatedBy() != null && basicInfo.getCreatedBy() > 0) {
            employee.setCreated_by(basicInfo.getCreatedBy());
        }

        if (basicInfo.getCampusId() != null) {
            employee.setCampus_id(campusRepository.findByCampusIdAndIsActive(basicInfo.getCampusId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Campus not found with ID: " + basicInfo.getCampusId())));
        }

        // Update building_id - optional field
        if (basicInfo.getBuildingId() != null && basicInfo.getBuildingId() > 0) {
            Building building = buildingRepository.findById(basicInfo.getBuildingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Building not found with ID: " + basicInfo.getBuildingId()));
            // Validate building is active
            if (building.getIsActive() != 1) {
                throw new ResourceNotFoundException("Building with ID: " + basicInfo.getBuildingId() + " is not active");
            }
            employee.setBuilding_id(building);
        } else if (basicInfo.getBuildingId() != null && basicInfo.getBuildingId() == 0) {
            // Explicitly set to null if 0 is provided
            employee.setBuilding_id(null);
        }
        // If buildingId is null, keep existing value

        if (basicInfo.getGenderId() != null) {
            employee.setGender(genderRepository.findByIdAndIsActive(basicInfo.getGenderId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Gender not found")));
        }

        // Note: designationId and departmentId are now handled in CategoryInfoDTO only
        // Removed from BasicInfoDTO to avoid conflicts - these should be set via saveCategoryInfo API

        if (basicInfo.getCategoryId() != null) {
            employee.setCategory(categoryRepository.findByIdAndIsActive(basicInfo.getCategoryId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Category not found")));
        }

        if (basicInfo.getEmpTypeId() != null) {
            employee.setEmployee_type_id(employeeTypeRepository.findByIdAndIsActive(basicInfo.getEmpTypeId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active EmployeeType not found")));
        }

        if (basicInfo.getQualificationId() != null) {
            employee.setQualification_id(qualificationRepository.findById(basicInfo.getQualificationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Qualification not found with ID: " + basicInfo.getQualificationId())));
        }

        if (basicInfo.getEmpWorkModeId() != null) {
            employee.setWorkingMode_id(workingModeRepository.findByIdAndIsActive(basicInfo.getEmpWorkModeId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active WorkingMode not found")));
        }

        if (basicInfo.getJoinTypeId() != null) {
            employee.setJoin_type_id(joiningAsRepository.findByIdAndIsActive(basicInfo.getJoinTypeId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active JoiningAs not found")));

            if (basicInfo.getJoinTypeId() == 3) {
                if (basicInfo.getReplacedByEmpId() == null || basicInfo.getReplacedByEmpId() <= 0) {
                    throw new ResourceNotFoundException(
                            "replacedByEmpId is required when joinTypeId is 3 (Replacement). Please provide a valid replacement employee ID.");
                }
                employee.setEmployee_replaceby_id(employeeRepository.findByIdAndIs_active(basicInfo.getReplacedByEmpId(), 0)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Inactive Replacement Employee not found with ID: " + basicInfo.getReplacedByEmpId())));
            } else if (basicInfo.getReplacedByEmpId() != null && basicInfo.getReplacedByEmpId() > 0) {
                employee.setEmployee_replaceby_id(employeeRepository.findByIdAndIs_active(basicInfo.getReplacedByEmpId(), 0)
                        .orElse(null));
            } else {
                employee.setEmployee_replaceby_id(null);
            }

            if (basicInfo.getJoinTypeId() == 4) {
                if (basicInfo.getContractStartDate() != null) {
                    employee.setContract_start_date(basicInfo.getContractStartDate());
                } else if (basicInfo.getDateOfJoin() != null) {
                    employee.setContract_start_date(basicInfo.getDateOfJoin());
                }

                if (basicInfo.getContractEndDate() != null) {
                    employee.setContract_end_date(basicInfo.getContractEndDate());
                } else {
                    java.sql.Date startDate = basicInfo.getContractStartDate() != null ?
                            basicInfo.getContractStartDate() : basicInfo.getDateOfJoin();
                    if (startDate != null) {
                        long oneYearInMillis = 365L * 24 * 60 * 60 * 1000;
                        java.util.Date endDateUtil = new java.util.Date(startDate.getTime() + oneYearInMillis);
                        employee.setContract_end_date(new java.sql.Date(endDateUtil.getTime()));
                    }
                }
            }
        }

        if (basicInfo.getModeOfHiringId() != null) {
            employee.setModeOfHiring_id(modeOfHiringRepository.findByIdAndIsActive(basicInfo.getModeOfHiringId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active ModeOfHiring not found")));
        }

        if (basicInfo.getReferenceEmpId() != null && basicInfo.getReferenceEmpId() > 0) {
            employee.setEmployee_reference(employeeRepository.findByIdAndIs_active(basicInfo.getReferenceEmpId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Reference Employee not found with ID: " + basicInfo.getReferenceEmpId())));
        } else {
            employee.setEmployee_reference(null);
        }

        if (basicInfo.getHiredByEmpId() != null && basicInfo.getHiredByEmpId() > 0) {
            employee.setEmployee_hired(employeeRepository.findByIdAndIs_active(basicInfo.getHiredByEmpId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Hired By Employee not found with ID: " + basicInfo.getHiredByEmpId())));
        } else {
            employee.setEmployee_hired(null);
        }

        if (basicInfo.getManagerId() != null && basicInfo.getManagerId() > 0) {
            employee.setEmployee_manager_id(employeeRepository.findByIdAndIs_active(basicInfo.getManagerId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Manager not found with ID: " + basicInfo.getManagerId())));
        } else {
            employee.setEmployee_manager_id(null);
        }

        if (basicInfo.getReportingManagerId() != null && basicInfo.getReportingManagerId() > 0) {
            employee.setEmployee_reporting_id(employeeRepository.findByIdAndIs_active(basicInfo.getReportingManagerId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Reporting Manager not found with ID: " + basicInfo.getReportingManagerId())));
        } else {
            employee.setEmployee_reporting_id(null);
        }

        // Handle preChaitanyaId: if entered, must be an inactive employee (is_active = 0), if not entered, set to null
        if (basicInfo.getPreChaitanyaId() != null && basicInfo.getPreChaitanyaId() > 0) {
            employee.setPre_chaitanya_id(employeeRepository.findByIdAndIs_active(basicInfo.getPreChaitanyaId(), 0)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Previous Chaitanya Employee not found with ID: " + basicInfo.getPreChaitanyaId() + ". Only inactive employees (is_active = 0) can be used as previous Chaitanya employee.")));
        } else {
            employee.setPre_chaitanya_id(null);
        }

        logger.info("✅ Completed updating employee entity (emp_id: {})", employee.getEmp_id());
    }

    /**
     * Helper: Set employee status to "Incompleted"
     */
    private void setIncompletedStatus(Employee employee) {
        EmployeeCheckListStatus incompletedStatus = employeeCheckListStatusRepository
                .findByCheck_app_status_name("Incompleted")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EmployeeCheckListStatus with name 'Incompleted' not found"));
        employee.setEmp_check_list_status_id(incompletedStatus);
    }

    /**
     * Helper: Find Employee by tempPayrollId
     */
    private Employee findEmployeeByTempPayrollId(String tempPayrollId) {
        Employee employee = employeeRepository.findByTempPayrollId(tempPayrollId.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with tempPayrollId: " + tempPayrollId));
        logger.info("Found employee with emp_id: {} for tempPayrollId: {}", employee.getEmp_id(), tempPayrollId);
        return employee;
    }

    // ============================================================================
    // HELPER METHODS - EmpDetails Operations
    // ============================================================================


    /**
     * Helper: Prepare EmpDetails entity WITHOUT saving
     */
    private EmpDetails prepareEmpDetailsEntity(BasicInfoDTO basicInfo, Employee employee) {
        if (basicInfo == null) {
            throw new ResourceNotFoundException("Basic Info is required");
        }

        EmpDetails empDetails = new EmpDetails();
        empDetails.setEmployee_id(employee);
        empDetails.setAdhaar_name(basicInfo.getAdhaarName());
        empDetails.setDate_of_birth(basicInfo.getDateOfBirth());
        empDetails.setPersonal_email(basicInfo.getEmail());

        if (basicInfo.getEmergencyPhNo() == null || basicInfo.getEmergencyPhNo().trim().isEmpty()) {
            throw new ResourceNotFoundException("Emergency contact phone number (emergencyPhNo) is required (NOT NULL column)");
        }
        empDetails.setEmergency_ph_no(basicInfo.getEmergencyPhNo().trim());

        if (basicInfo.getEmergencyRelationId() != null && basicInfo.getEmergencyRelationId() > 0) {
            empDetails.setRelation_id(relationRepository.findById(basicInfo.getEmergencyRelationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Emergency Relation not found with ID: " + basicInfo.getEmergencyRelationId())));
        } else {
            empDetails.setRelation_id(null);
        }

        empDetails.setAdhaar_no(basicInfo.getAadharNum());
        empDetails.setPancard_no(basicInfo.getPancardNum());
        empDetails.setAdhaar_enrolment_no(basicInfo.getAadharEnrolmentNum());
        empDetails.setPassout_year(0);
        empDetails.setIs_active(1);
        empDetails.setStatus("ACTIVE");

        if (basicInfo.getBloodGroupId() == null) {
            throw new ResourceNotFoundException("BloodGroup ID is required (NOT NULL column)");
        }
        empDetails.setBloodGroup_id(bloodGroupRepository.findByIdAndIsActive(basicInfo.getBloodGroupId(), 1)
                .orElseThrow(() -> new ResourceNotFoundException("Active BloodGroup not found with ID: " + basicInfo.getBloodGroupId())));

        if (basicInfo.getCasteId() == null) {
            throw new ResourceNotFoundException("Caste ID is required (NOT NULL column)");
        }
        empDetails.setCaste_id(casteRepository.findById(basicInfo.getCasteId())
                .orElseThrow(() -> new ResourceNotFoundException("Caste not found with ID: " + basicInfo.getCasteId())));

        if (basicInfo.getReligionId() == null) {
            throw new ResourceNotFoundException("Religion ID is required (NOT NULL column)");
        }
        empDetails.setReligion_id(relegionRepository.findById(basicInfo.getReligionId())
                .orElseThrow(() -> new ResourceNotFoundException("Religion not found with ID: " + basicInfo.getReligionId())));

        if (basicInfo.getMaritalStatusId() == null) {
            throw new ResourceNotFoundException("MaritalStatus ID is required (NOT NULL column)");
        }
        empDetails.setMarital_status_id(maritalStatusRepository.findByIdAndIsActive(basicInfo.getMaritalStatusId(), 1)
                .orElseThrow(() -> new ResourceNotFoundException("Active MaritalStatus not found with ID: " + basicInfo.getMaritalStatusId())));

        return empDetails;
    }

    /**
     * Helper: Update EmpDetails fields from source to target
     */
    private void updateEmpDetailsFields(EmpDetails target, EmpDetails source) {
        target.setAdhaar_name(source.getAdhaar_name());
        target.setDate_of_birth(source.getDate_of_birth());
        target.setPersonal_email(source.getPersonal_email());
        target.setEmergency_ph_no(source.getEmergency_ph_no());
        target.setRelation_id(source.getRelation_id());
        target.setAdhaar_no(source.getAdhaar_no());
        target.setPancard_no(source.getPancard_no());
        target.setAdhaar_enrolment_no(source.getAdhaar_enrolment_no());
        target.setBloodGroup_id(source.getBloodGroup_id());
        target.setCaste_id(source.getCaste_id());
        target.setReligion_id(source.getReligion_id());
        target.setMarital_status_id(source.getMarital_status_id());
        target.setIs_active(source.getIs_active());
        target.setStatus(source.getStatus());
    }

    /**
     * Helper: Update EmpDetails fields except personal_email
     */
    private void updateEmpDetailsFieldsExceptEmail(EmpDetails target, EmpDetails source) {
        target.setAdhaar_name(source.getAdhaar_name());
        target.setDate_of_birth(source.getDate_of_birth());
        // DO NOT update personal_email
        target.setEmergency_ph_no(source.getEmergency_ph_no());
        target.setRelation_id(source.getRelation_id());
        target.setAdhaar_no(source.getAdhaar_no());
        target.setPancard_no(source.getPancard_no());
        target.setAdhaar_enrolment_no(source.getAdhaar_enrolment_no());
        target.setBloodGroup_id(source.getBloodGroup_id());
        target.setCaste_id(source.getCaste_id());
        target.setReligion_id(source.getReligion_id());
        target.setMarital_status_id(source.getMarital_status_id());
        target.setIs_active(source.getIs_active());
        target.setStatus(source.getStatus());
    }

    // ============================================================================
    // HELPER METHODS - EmpPfDetails Operations
    // ============================================================================


    /**
     * Helper: Prepare EmpPfDetails entity WITHOUT saving
     */
    private EmpPfDetails prepareEmpPfDetailsEntity(BasicInfoDTO basicInfo, Employee employee) {
        if (basicInfo == null) return null;

        if (basicInfo.getPreUanNum() == null && basicInfo.getPreEsiNum() == null) {
            return null;
        }

        EmpPfDetails empPfDetails = new EmpPfDetails();
        empPfDetails.setEmployee_id(employee);
        empPfDetails.setPre_esi_no(basicInfo.getPreEsiNum());
        empPfDetails.setIs_active(1);

        return empPfDetails;
    }

    // ============================================================================
    // HELPER METHODS - Address Operations
    // ============================================================================

    /**
     * Helper: Save Address Entities
     */
    private int saveAddressEntities(Employee employee, AddressInfoDTO addressInfo, Integer createdBy, Integer updatedBy) {
        List<EmpaddressInfo> addressEntities = prepareAddressEntities(addressInfo, employee, createdBy);
        updateOrCreateAddressEntities(addressEntities, employee, addressInfo, updatedBy);
        return addressEntities.size();
    }

    /**
     * Helper: Prepare Address entities WITHOUT saving
     */
    private List<EmpaddressInfo> prepareAddressEntities(AddressInfoDTO addressInfo, Employee employee, Integer createdBy) {
        List<EmpaddressInfo> addressList = new ArrayList<>();

        if (addressInfo == null) return addressList;

        if (addressInfo.getCurrentAddress() != null) {
            EmpaddressInfo currentAddr = createAddressEntity(addressInfo.getCurrentAddress(), employee, "CURR", createdBy);
            addressList.add(currentAddr);
        }

        if (!Boolean.TRUE.equals(addressInfo.getPermanentAddressSameAsCurrent())) {
            if (addressInfo.getPermanentAddress() != null) {
                EmpaddressInfo permanentAddr = createAddressEntity(addressInfo.getPermanentAddress(), employee, "PERM", createdBy);
                addressList.add(permanentAddr);
            }
        }

        return addressList;
    }

    /**
     * Helper: Create Address entity
     */
    private EmpaddressInfo createAddressEntity(AddressInfoDTO.AddressDTO addressDTO, Employee employee, String addressType, Integer createdBy) {
        EmpaddressInfo address = new EmpaddressInfo();
        address.setAddrs_type(addressType);
        address.setHouse_no(addressDTO.getAddressLine1());
        address.setLandmark(addressDTO.getAddressLine2() + " " + (addressDTO.getAddressLine3() != null ? addressDTO.getAddressLine3() : ""));
        address.setPostal_code(addressDTO.getPin());
        address.setIs_active(1);
        address.setEmp_id(employee);

        if (addressDTO.getCountryId() != null) {
            address.setCountry_id(countryRepository.findById(addressDTO.getCountryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Country not found")));
        } else {
            throw new ResourceNotFoundException("Country ID is required (NOT NULL column)");
        }

        if (addressDTO.getStateId() != null) {
            address.setState_id(stateRepository.findById(addressDTO.getStateId())
                    .orElseThrow(() -> new ResourceNotFoundException("State not found")));
        } else {
            throw new ResourceNotFoundException("State ID is required (NOT NULL column)");
        }

        if (addressDTO.getCityId() != null) {
            address.setCity_id(cityRepository.findById(addressDTO.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("City not found")));
        } else {
            throw new ResourceNotFoundException("City ID is required (NOT NULL column)");
        }

        // Set created_by only if provided from frontend, otherwise don't set it (entity default will be used)
        if (createdBy != null && createdBy > 0) {
            address.setCreated_by(createdBy);
            address.setCreated_date(new java.sql.Timestamp(System.currentTimeMillis()));
        }
        // If createdBy is null, don't set created_by - entity default value will be used

        return address;
    }

    /**
     * Helper: Update or create Address entities
     */
    private void updateOrCreateAddressEntities(List<EmpaddressInfo> newAddresses, Employee employee, AddressInfoDTO addressInfo, Integer updatedBy) {
        int empId = employee.getEmp_id();

        List<EmpaddressInfo> existingAddresses = empaddressInfoRepository.findAll().stream()
                .filter(addr -> addr.getEmp_id() != null && addr.getEmp_id().getEmp_id() == empId && addr.getIs_active() == 1)
                .collect(Collectors.toList());

        for (EmpaddressInfo newAddr : newAddresses) {
            newAddr.setEmp_id(employee);
            newAddr.setIs_active(1);

            Optional<EmpaddressInfo> existingByType = existingAddresses.stream()
                    .filter(addr -> addr.getAddrs_type() != null &&
                            addr.getAddrs_type().equals(newAddr.getAddrs_type()))
                    .findFirst();

            if (existingByType.isPresent()) {
                EmpaddressInfo existing = existingByType.get();
                updateAddressFields(existing, newAddr);
                // Set updated_by and updated_date on update
                if (updatedBy != null && updatedBy > 0) {
                    existing.setUpdated_by(updatedBy);
                    existing.setUpdated_date(new java.sql.Timestamp(System.currentTimeMillis()));
                }
                empaddressInfoRepository.save(existing);
                existingAddresses.remove(existing);
            } else {
                empaddressInfoRepository.save(newAddr);
            }
        }

        if (addressInfo != null && Boolean.TRUE.equals(addressInfo.getPermanentAddressSameAsCurrent())) {
            for (EmpaddressInfo existingAddr : existingAddresses) {
                if ("PERM".equals(existingAddr.getAddrs_type())) {
                    existingAddr.setIs_active(0);
                    if (updatedBy != null && updatedBy > 0) {
                        existingAddr.setUpdated_by(updatedBy);
                        existingAddr.setUpdated_date(new java.sql.Timestamp(System.currentTimeMillis()));
                    }
                    empaddressInfoRepository.save(existingAddr);
                }
            }
            existingAddresses.removeIf(addr -> "PERM".equals(addr.getAddrs_type()));
        }

        for (EmpaddressInfo remainingAddr : existingAddresses) {
            remainingAddr.setIs_active(0);
            if (updatedBy != null && updatedBy > 0) {
                remainingAddr.setUpdated_by(updatedBy);
                remainingAddr.setUpdated_date(new java.sql.Timestamp(System.currentTimeMillis()));
            }
            empaddressInfoRepository.save(remainingAddr);
        }
    }

    /**
     * Helper: Update Address fields
     */
    private void updateAddressFields(EmpaddressInfo target, EmpaddressInfo source) {
        target.setAddrs_type(source.getAddrs_type());
        target.setCountry_id(source.getCountry_id());
        target.setState_id(source.getState_id());
        target.setCity_id(source.getCity_id());
        target.setPostal_code(source.getPostal_code());
        target.setHouse_no(source.getHouse_no());
        target.setLandmark(source.getLandmark());
        target.setIs_active(source.getIs_active());
    }

    // ============================================================================
    // HELPER METHODS - Family Operations
    // ============================================================================

    /**
     * Helper: Save Family Entities
     */
    private int saveFamilyEntities(Employee employee, FamilyInfoDTO familyInfo, Integer createdBy, Integer updatedBy) {
        List<EmpFamilyDetails> familyEntities = prepareFamilyEntities(familyInfo, employee, createdBy);
        updateOrCreateFamilyEntities(familyEntities, employee, updatedBy);
        return familyEntities.size();
    }

    /**
     * Helper: Prepare Family entities WITHOUT saving
     */
    private List<EmpFamilyDetails> prepareFamilyEntities(FamilyInfoDTO familyInfo, Employee employee, Integer createdBy) {
        List<EmpFamilyDetails> familyList = new ArrayList<>();

        if (familyInfo == null || familyInfo.getFamilyMembers() == null || familyInfo.getFamilyMembers().isEmpty()) {
            return familyList;
        }

        for (FamilyInfoDTO.FamilyMemberDTO memberDTO : familyInfo.getFamilyMembers()) {
            if (memberDTO != null) {
                EmpFamilyDetails familyMember = createFamilyMemberEntity(memberDTO, employee, createdBy);
                familyList.add(familyMember);
            }
        }

        return familyList;
    }

    /**
     * Helper: Create Family Member entity
     */
    private EmpFamilyDetails createFamilyMemberEntity(FamilyInfoDTO.FamilyMemberDTO memberDTO, Employee employee, Integer createdBy) {
        EmpFamilyDetails familyMember = new EmpFamilyDetails();

        familyMember.setEmp_id(employee);
        familyMember.setFirst_name(memberDTO.getFirstName());
        familyMember.setLast_name(memberDTO.getLastName());
        familyMember.setIs_late(memberDTO.getIsLate() != null && memberDTO.getIsLate() ? "Y" : "N");
        
        // Handle occupation: If occupationId is provided, check if it exists in Occupation table
        // If exists, use occupation_name from table; otherwise use occupation string from frontend
        String occupationToStore = null;
        if (memberDTO.getOccupationId() != null && memberDTO.getOccupationId() > 0) {
            Optional<com.employee.entity.Occupation> occupationOpt = occupationRepository.findById(memberDTO.getOccupationId());
            if (occupationOpt.isPresent() && occupationOpt.get().getIsActive() != null && occupationOpt.get().getIsActive() == 1) {
                // Occupation ID exists and is active, use occupation_name from table
                occupationToStore = occupationOpt.get().getOccupation_name();
                logger.debug("Using occupation_name '{}' from Occupation table for occupationId: {}", occupationToStore, memberDTO.getOccupationId());
            } else {
                // Occupation ID provided but not found or inactive, use occupation string from frontend
                occupationToStore = memberDTO.getOccupation();
                logger.debug("Occupation ID {} not found or inactive, using occupation string from frontend: {}", memberDTO.getOccupationId(), occupationToStore);
            }
        } else {
            // No occupationId provided (Others case), use occupation string from frontend
            occupationToStore = memberDTO.getOccupation();
            logger.debug("No occupationId provided, using occupation string from frontend: {}", occupationToStore);
        }
        
        if (occupationToStore == null || occupationToStore.trim().isEmpty()) {
            throw new ResourceNotFoundException("Occupation is required (NOT NULL column). Please provide either occupationId or occupation name.");
        }
        
        familyMember.setOccupation(occupationToStore);
        familyMember.setNationality(memberDTO.getNationality());
        familyMember.setIs_active(1);
        familyMember.setDate_of_birth(memberDTO.getDateOfBirth());

        if (memberDTO.getIsDependent() != null) {
            familyMember.setIs_dependent(memberDTO.getIsDependent() ? 1 : 0);
        } else {
            familyMember.setIs_dependent(null);
        }

        if (memberDTO.getRelationId() == null) {
            throw new ResourceNotFoundException("Relation ID is required (NOT NULL column)");
        }
        familyMember.setRelation_id(relationRepository.findById(memberDTO.getRelationId())
                .orElseThrow(() -> new ResourceNotFoundException("Relation not found")));

        Integer genderIdToUse;
        if (memberDTO.getRelationId() == 1) {
            genderIdToUse = 1; // Father - Male
        } else if (memberDTO.getRelationId() == 2) {
            genderIdToUse = 2; // Mother - Female
        } else {
            genderIdToUse = memberDTO.getGenderId();
        }

        if (genderIdToUse != null) {
            familyMember.setGender_id(genderRepository.findById(genderIdToUse)
                    .orElseThrow(() -> new ResourceNotFoundException("Gender not found with ID: " + genderIdToUse)));
        } else {
            throw new ResourceNotFoundException("Gender ID is required (NOT NULL column)");
        }

        if (memberDTO.getBloodGroupId() == null) {
            throw new ResourceNotFoundException("BloodGroup ID is required (NOT NULL column)");
        }
        familyMember.setBlood_group_id(bloodGroupRepository.findByIdAndIsActive(memberDTO.getBloodGroupId(), 1)
                .orElseThrow(() -> new ResourceNotFoundException("Active BloodGroup not found with ID: " + memberDTO.getBloodGroupId())));

        Integer isSriChaitanyaEmpValue = 0;
        if (memberDTO.getIsSriChaitanyaEmp() != null) {
            isSriChaitanyaEmpValue = memberDTO.getIsSriChaitanyaEmp() ? 1 : 0;
        }
        familyMember.setIs_sri_chaitanya_emp(isSriChaitanyaEmpValue);

        if (isSriChaitanyaEmpValue == 1) {
            if (memberDTO.getParentEmpId() == null || memberDTO.getParentEmpId() <= 0) {
                throw new ResourceNotFoundException(
                        "parentEmpId is required when isSriChaitanyaEmp is true. Please provide a valid parent employee ID.");
            }
            Employee parentEmployee = employeeRepository.findById(memberDTO.getParentEmpId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent Employee not found with ID: " + memberDTO.getParentEmpId()));
            familyMember.setParent_emp_id(parentEmployee);
        } else {
            if (memberDTO.getParentEmpId() != null && memberDTO.getParentEmpId() > 0) {
                Employee parentEmployee = employeeRepository.findById(memberDTO.getParentEmpId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Parent Employee not found with ID: " + memberDTO.getParentEmpId()));
                familyMember.setParent_emp_id(parentEmployee);
            }
        }

        familyMember.setEmail(memberDTO.getEmail());

        if (memberDTO.getPhoneNumber() != null && !memberDTO.getPhoneNumber().trim().isEmpty()) {
            try {
                familyMember.setContact_no(Long.parseLong(memberDTO.getPhoneNumber()));
            } catch (NumberFormatException e) {
                throw new ResourceNotFoundException(
                        "Invalid phone number format for family member: " + memberDTO.getPhoneNumber());
            }
        }

        // Set created_by only if provided from frontend, otherwise don't set it (entity default will be used)
        if (createdBy != null && createdBy > 0) {
            familyMember.setCreated_by(createdBy);
            familyMember.setCreated_date(new java.sql.Timestamp(System.currentTimeMillis()));
        }
        // If createdBy is null, don't set created_by - entity default value will be used

        return familyMember;
    }

    /**
     * Helper: Update or create Family entities
     */
    private void updateOrCreateFamilyEntities(List<EmpFamilyDetails> newFamily, Employee employee, Integer updatedBy) {
        int empId = employee.getEmp_id();

        List<EmpFamilyDetails> existingFamily = empFamilyDetailsRepository.findAll().stream()
                .filter(fam -> fam.getEmp_id() != null && fam.getEmp_id().getEmp_id() == empId && fam.getIs_active() == 1)
                .collect(Collectors.toList());

        int maxSize = Math.max(newFamily.size(), existingFamily.size());

        for (int i = 0; i < maxSize; i++) {
            if (i < newFamily.size()) {
                EmpFamilyDetails newFam = newFamily.get(i);
                newFam.setEmp_id(employee);
                newFam.setIs_active(1);

                if (i < existingFamily.size()) {
                    EmpFamilyDetails existing = existingFamily.get(i);
                    updateFamilyFields(existing, newFam);
                    // Set updated_by and updated_date on update
                    if (updatedBy != null && updatedBy > 0) {
                        existing.setUpdated_by(updatedBy);
                        existing.setUpdated_date(new java.sql.Timestamp(System.currentTimeMillis()));
                    }
                    empFamilyDetailsRepository.save(existing);
                } else {
                    empFamilyDetailsRepository.save(newFam);
                }
            } else if (i < existingFamily.size()) {
                existingFamily.get(i).setIs_active(0);
                if (updatedBy != null && updatedBy > 0) {
                    existingFamily.get(i).setUpdated_by(updatedBy);
                    existingFamily.get(i).setUpdated_date(new java.sql.Timestamp(System.currentTimeMillis()));
                }
                empFamilyDetailsRepository.save(existingFamily.get(i));
            }
        }
    }

    /**
     * Helper: Update Family fields
     */
    private void updateFamilyFields(EmpFamilyDetails target, EmpFamilyDetails source) {
        target.setFirst_name(source.getFirst_name());
        target.setLast_name(source.getLast_name());
        target.setDate_of_birth(source.getDate_of_birth());
        target.setGender_id(source.getGender_id());
        target.setRelation_id(source.getRelation_id());
        target.setBlood_group_id(source.getBlood_group_id());
        target.setNationality(source.getNationality());
        target.setOccupation(source.getOccupation());
        target.setIs_dependent(source.getIs_dependent());
        target.setIs_late(source.getIs_late());
        target.setIs_sri_chaitanya_emp(source.getIs_sri_chaitanya_emp());
        target.setParent_emp_id(source.getParent_emp_id());
        target.setEmail(source.getEmail());
        target.setContact_no(source.getContact_no());
        target.setIs_active(source.getIs_active());
    }

    /**
     * Helper: Save Family Group Photo as document
     */
    private void saveFamilyGroupPhoto(Employee employee, FamilyInfoDTO familyInfo) {
        if (familyInfo != null && familyInfo.getFamilyGroupPhotoPath() != null
                && !familyInfo.getFamilyGroupPhotoPath().trim().isEmpty()) {
            EmpDocuments familyPhotoDoc = createFamilyGroupPhotoDocument(familyInfo.getFamilyGroupPhotoPath(), employee);
            empDocumentsRepository.save(familyPhotoDoc);
            logger.info("✅ Family Group Photo saved as document for emp_id: {} (tempPayrollId: {})", 
                    employee.getEmp_id(), employee.getTempPayrollId());
        }
    }

    /**
     * Helper: Create Family Group Photo document entity
     */
    private EmpDocuments createFamilyGroupPhotoDocument(String familyGroupPhotoPath, Employee employee) {
        EmpDocuments doc = new EmpDocuments();
        doc.setEmp_id(employee);
        doc.setDoc_path(familyGroupPhotoPath);
        doc.setIs_verified(0);
        doc.setIs_active(1);

        doc.setEmp_doc_type_id(empDocTypeRepository.findByDocNameAndIsActive("Family Group Photo", 1)
                .orElseThrow(() -> new ResourceNotFoundException("Family Group Photo document type not found or inactive")));

        return doc;
    }

    // ============================================================================
    // HELPER METHODS - Experience Operations
    // ============================================================================

    /**
     * Helper: Save Experience Entities
     */
    private int saveExperienceEntities(Employee employee, PreviousEmployerInfoDTO previousEmployerInfo, Integer createdBy, Integer updatedBy) {
        List<EmpExperienceDetails> experienceEntities = prepareExperienceEntities(previousEmployerInfo, employee, createdBy);
        updateOrCreateExperienceEntities(experienceEntities, employee, updatedBy);
        return experienceEntities.size();
    }

    /**
     * Helper: Prepare Experience entities WITHOUT saving
     */
    private List<EmpExperienceDetails> prepareExperienceEntities(PreviousEmployerInfoDTO previousEmployerInfo, Employee employee, Integer createdBy) {
        List<EmpExperienceDetails> experienceList = new ArrayList<>();

        if (previousEmployerInfo == null || previousEmployerInfo.getPreviousEmployers() == null
                || previousEmployerInfo.getPreviousEmployers().isEmpty()) {
            return experienceList;
        }

        for (PreviousEmployerInfoDTO.EmployerDetailsDTO employer : previousEmployerInfo.getPreviousEmployers()) {
            if (employer != null) {
                EmpExperienceDetails experience = createExperienceEntity(employer, employee, createdBy);
                experienceList.add(experience);
            }
        }

        return experienceList;
    }

    /**
     * Helper: Create Experience entity
     */
    private EmpExperienceDetails createExperienceEntity(PreviousEmployerInfoDTO.EmployerDetailsDTO employerDTO, Employee employee, Integer createdBy) {
        EmpExperienceDetails experience = new EmpExperienceDetails();
        experience.setEmployee_id(employee);

        if (employerDTO.getCompanyName() != null) {
            String companyName = employerDTO.getCompanyName().trim();
            if (companyName.length() > 50) {
                companyName = companyName.substring(0, 50);
            }
            experience.setPre_organigation_name(companyName);
        } else {
            throw new ResourceNotFoundException("Company Name is required (NOT NULL column)");
        }

        if (employerDTO.getFromDate() != null) {
            experience.setDate_of_join(employerDTO.getFromDate());
        } else {
            throw new ResourceNotFoundException("From Date is required (NOT NULL column)");
        }

        if (employerDTO.getToDate() != null) {
            experience.setDate_of_leave(employerDTO.getToDate());
        } else {
            throw new ResourceNotFoundException("To Date is required (NOT NULL column)");
        }

        if (employerDTO.getDesignation() != null) {
            String designation = employerDTO.getDesignation().trim();
            if (designation.length() > 50) {
                designation = designation.substring(0, 50);
            }
            experience.setDesignation(designation);
        } else {
            throw new ResourceNotFoundException("Designation is required (NOT NULL column)");
        }

        if (employerDTO.getLeavingReason() != null) {
            String leavingReason = employerDTO.getLeavingReason().trim();
            if (leavingReason.length() > 50) {
                leavingReason = leavingReason.substring(0, 50);
            }
            experience.setLeaving_reason(leavingReason);
        } else {
            throw new ResourceNotFoundException("Leaving Reason is required (NOT NULL column)");
        }

        if (employerDTO.getNatureOfDuties() != null) {
            String natureOfDuties = employerDTO.getNatureOfDuties().trim();
            if (natureOfDuties.length() > 50) {
                natureOfDuties = natureOfDuties.substring(0, 50);
            }
            experience.setNature_of_duties(natureOfDuties);
        } else {
            throw new ResourceNotFoundException("Nature of Duties is required (NOT NULL column)");
        }

        String companyAddress = employerDTO.getCompanyAddressLine1() != null ? employerDTO.getCompanyAddressLine1() : "";
        if (employerDTO.getCompanyAddressLine2() != null) {
            companyAddress += " " + employerDTO.getCompanyAddressLine2();
        }
        if (companyAddress.trim().isEmpty()) {
            throw new ResourceNotFoundException("Company Address is required (NOT NULL column)");
        }
        String trimmedAddress = companyAddress.trim();
        if (trimmedAddress.length() > 50) {
            trimmedAddress = trimmedAddress.substring(0, 50);
        }
        experience.setCompany_addr(trimmedAddress);

        experience.setGross_salary(employerDTO.getGrossSalaryPerMonth() != null ? employerDTO.getGrossSalaryPerMonth() : 0);
        experience.setIs_active(1);

        // Set created_by only if provided from frontend, otherwise don't set it (entity default will be used)
        if (createdBy != null && createdBy > 0) {
            experience.setCreated_by(createdBy);
            experience.setCreated_date(new java.sql.Timestamp(System.currentTimeMillis()));
        }
        // If createdBy is null, don't set created_by - entity default value will be used

        // Note: preChaitanyaId has been moved to BasicInfoDTO and Employee entity
        // It is no longer stored in EmpExperienceDetails (field removed from entity)

        return experience;
    }

    /**
     * Helper: Update or create Experience entities
     */
    private void updateOrCreateExperienceEntities(List<EmpExperienceDetails> newExperience, Employee employee, Integer updatedBy) {
        int empId = employee.getEmp_id();

        List<EmpExperienceDetails> existingExperience = empExperienceDetailsRepository.findAll().stream()
                .filter(exp -> exp.getEmployee_id() != null && exp.getEmployee_id().getEmp_id() == empId && exp.getIs_active() == 1)
                .collect(Collectors.toList());

        int maxSize = Math.max(newExperience.size(), existingExperience.size());

        for (int i = 0; i < maxSize; i++) {
            if (i < newExperience.size()) {
                EmpExperienceDetails newExp = newExperience.get(i);
                newExp.setEmployee_id(employee);
                newExp.setIs_active(1);

                if (i < existingExperience.size()) {
                    EmpExperienceDetails existing = existingExperience.get(i);
                    updateExperienceFields(existing, newExp);
                    // Set updated_by and updated_date on update
                    if (updatedBy != null && updatedBy > 0) {
                        existing.setUpdated_by(updatedBy);
                        existing.setUpdated_date(new java.sql.Timestamp(System.currentTimeMillis()));
                    }
                    empExperienceDetailsRepository.save(existing);
                } else {
                    empExperienceDetailsRepository.save(newExp);
                }
            } else if (i < existingExperience.size()) {
                existingExperience.get(i).setIs_active(0);
                if (updatedBy != null && updatedBy > 0) {
                    existingExperience.get(i).setUpdated_by(updatedBy);
                    existingExperience.get(i).setUpdated_date(new java.sql.Timestamp(System.currentTimeMillis()));
                }
                empExperienceDetailsRepository.save(existingExperience.get(i));
            }
        }
    }

    /**
     * Helper: Update Experience fields
     */
    private void updateExperienceFields(EmpExperienceDetails target, EmpExperienceDetails source) {
        target.setPre_organigation_name(source.getPre_organigation_name());
        target.setDate_of_join(source.getDate_of_join());
        target.setDate_of_leave(source.getDate_of_leave());
        target.setDesignation(source.getDesignation());
        target.setLeaving_reason(source.getLeaving_reason());
        target.setNature_of_duties(source.getNature_of_duties());
        target.setCompany_addr(source.getCompany_addr());
        target.setGross_salary(source.getGross_salary());
        // Note: preChaitanyaId has been moved to BasicInfoDTO and Employee entity
        // It is no longer stored in EmpExperienceDetails (field removed from entity)
        target.setIs_active(source.getIs_active());
    }

    // ============================================================================
    // HELPER METHODS - Validation Operations
    // ============================================================================

    /**
     * Helper: Validate prepared entities BEFORE saving to database
     * This is the FINAL check before employee.save() consumes a sequence number
     */
    private void validatePreparedEntities(Employee employee, EmpDetails empDetails, EmpPfDetails empPfDetails) {
        if (employee == null) {
            throw new ResourceNotFoundException("Employee entity cannot be null");
        }

        if (employee.getFirst_name() == null || employee.getFirst_name().trim().isEmpty()) {
            throw new ResourceNotFoundException("Employee first name is required");
        }

        if (employee.getLast_name() == null || employee.getLast_name().trim().isEmpty()) {
            throw new ResourceNotFoundException("Employee last name is required");
        }

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

    /**
     * Helper: Validate Basic Info DTO
     */
    private void validateBasicInfo(BasicInfoDTO basicInfo, String tempPayrollId) {
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

        // Validate Aadhaar Number format IF provided (optional field)
        // CHANGED: Validation now supports Long type (removed .trim())
        if (basicInfo.getAadharNum() != null && basicInfo.getAadharNum() > 0) {
            String aadhar = String.valueOf(basicInfo.getAadharNum());
            
            // Layer 1: Format validation - must be exactly 12 numeric digits
            if (!aadhar.matches("^[0-9]{12}$")) {
                throw new ResourceNotFoundException("Aadhaar must be exactly 12 numeric digits. Please remove any spaces, dashes, or special characters.");
            }
            
            // Layer 2: Verhoeff algorithm validation - checks mathematical validity
            if (!isValidAadhaar(aadhar)) {
                throw new ResourceNotFoundException("Invalid Aadhaar number format. Please verify the Aadhaar number and try again.");
            }
        }

        // Validate tempPayrollId against SkillTestDetl table if provided
        if (tempPayrollId != null && !tempPayrollId.trim().isEmpty()) {
            skillTestDetailsRepository.findByTempPayrollId(tempPayrollId)
                    .orElseThrow(() -> new ResourceNotFoundException("Temp Payroll ID not found in Skill Test Details: " + tempPayrollId + ". Please provide a valid temp payroll ID."));
        }
    }

    /**
     * Helper: Validate Address Info DTO
     */
    private void validateAddressInfo(AddressInfoDTO addressInfo) {
        if (addressInfo == null) {
            return; // Address info is optional
        }

        if (addressInfo.getCurrentAddress() != null) {
            if (addressInfo.getCurrentAddress().getCityId() != null) {
                cityRepository.findById(addressInfo.getCurrentAddress().getCityId())
                        .orElseThrow(() -> new ResourceNotFoundException("City not found with ID: " + addressInfo.getCurrentAddress().getCityId()));
            }
            if (addressInfo.getCurrentAddress().getStateId() != null) {
                stateRepository.findById(addressInfo.getCurrentAddress().getStateId())
                        .orElseThrow(() -> new ResourceNotFoundException("State not found with ID: " + addressInfo.getCurrentAddress().getStateId()));
            }
            if (addressInfo.getCurrentAddress().getCountryId() != null) {
                countryRepository.findById(addressInfo.getCurrentAddress().getCountryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Country not found with ID: " + addressInfo.getCurrentAddress().getCountryId()));
            }
            if (addressInfo.getCurrentAddress().getPin() != null && addressInfo.getCurrentAddress().getPin().length() > 10) {
                throw new ResourceNotFoundException("PIN code cannot exceed 10 characters");
            }
            if (addressInfo.getCurrentAddress().getName() != null && addressInfo.getCurrentAddress().getName().length() > 50) {
                throw new ResourceNotFoundException("Address name cannot exceed 50 characters");
            }
        }

        // Only validate permanent address if permanentAddressSameAsCurrent is NOT true
        // If permanentAddressSameAsCurrent = true, permanent address is ignored (can be null/empty)
        if (!Boolean.TRUE.equals(addressInfo.getPermanentAddressSameAsCurrent()) && addressInfo.getPermanentAddress() != null) {
            if (addressInfo.getPermanentAddress().getCityId() != null) {
                cityRepository.findById(addressInfo.getPermanentAddress().getCityId())
                        .orElseThrow(() -> new ResourceNotFoundException("Permanent Address City not found with ID: " + addressInfo.getPermanentAddress().getCityId()));
            }
            if (addressInfo.getPermanentAddress().getStateId() != null) {
                stateRepository.findById(addressInfo.getPermanentAddress().getStateId())
                        .orElseThrow(() -> new ResourceNotFoundException("Permanent Address State not found with ID: " + addressInfo.getPermanentAddress().getStateId()));
            }
            if (addressInfo.getPermanentAddress().getCountryId() != null) {
                countryRepository.findById(addressInfo.getPermanentAddress().getCountryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Permanent Address Country not found with ID: " + addressInfo.getPermanentAddress().getCountryId()));
            }
            if (addressInfo.getPermanentAddress().getPin() != null && addressInfo.getPermanentAddress().getPin().length() > 10) {
                throw new ResourceNotFoundException("PIN code cannot exceed 10 characters");
            }
            if (addressInfo.getPermanentAddress().getName() != null && addressInfo.getPermanentAddress().getName().length() > 50) {
                throw new ResourceNotFoundException("Address name cannot exceed 50 characters");
            }
        }
    }

    /**
     * Helper: Validate Family Info DTO
     */
    private void validateFamilyInfo(FamilyInfoDTO familyInfo) {
        if (familyInfo == null) {
            return; // Family info is optional
        }

        // Validate Family Group Photo document type if provided
        if (familyInfo.getFamilyGroupPhotoPath() != null && !familyInfo.getFamilyGroupPhotoPath().trim().isEmpty()) {
            empDocTypeRepository.findByDocNameAndIsActive("Family Group Photo", 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Family Group Photo document type not found or inactive in document type master. Please ensure the document type is configured and active."));
        }

        if (familyInfo.getFamilyMembers() != null) {
            for (FamilyInfoDTO.FamilyMemberDTO member : familyInfo.getFamilyMembers()) {
                if (member == null) continue;

                if (member.getRelationId() == null) {
                    throw new ResourceNotFoundException("Relation ID is required for family member: " + member.getFirstName());
                }

                relationRepository.findById(member.getRelationId())
                        .orElseThrow(() -> new ResourceNotFoundException("Relation not found with ID: " + member.getRelationId() + " for family member: " + member.getFirstName()));

                // Gender validation: For Father (relationId=1) and Mother (relationId=2), gender is auto-set by backend
                if (member.getRelationId() != 1 && member.getRelationId() != 2) {
                    if (member.getGenderId() == null) {
                        throw new ResourceNotFoundException("Gender ID is required for family member: " + member.getFirstName() +
                                " (relationId: " + member.getRelationId() + "). Gender is only auto-set for Father and Mother.");
                    }
                    genderRepository.findById(member.getGenderId())
                            .orElseThrow(() -> new ResourceNotFoundException("Gender not found with ID: " + member.getGenderId() + " for family member: " + member.getFirstName()));
                }

                if (member.getBloodGroupId() == null) {
                    throw new ResourceNotFoundException("Blood Group ID is required for family member: " + member.getFirstName());
                }
                bloodGroupRepository.findByIdAndIsActive(member.getBloodGroupId(), 1)
                        .orElseThrow(() -> new ResourceNotFoundException("Active Blood Group not found with ID: " + member.getBloodGroupId() + " for family member: " + member.getFirstName()));

                if (member.getNationality() == null || member.getNationality().trim().isEmpty()) {
                    throw new ResourceNotFoundException("Nationality is required for family member: " + member.getFirstName());
                }

                if (member.getOccupation() == null || member.getOccupation().trim().isEmpty()) {
                    throw new ResourceNotFoundException("Occupation is required for family member: " + member.getFirstName());
                }
            }
        }
    }

    /**
     * Helper: Validate Previous Employer Info DTO
     */
    private void validatePreviousEmployerInfo(PreviousEmployerInfoDTO previousEmployerInfo) {
        if (previousEmployerInfo == null) {
            return; // Previous employer info is optional
        }

        if (previousEmployerInfo.getPreviousEmployers() != null) {
            for (PreviousEmployerInfoDTO.EmployerDetailsDTO employer : previousEmployerInfo.getPreviousEmployers()) {
                if (employer == null) continue;

                if (employer.getCompanyName() == null || employer.getCompanyName().trim().isEmpty()) {
                    throw new ResourceNotFoundException("Company Name is required for previous employer");
                }
                if (employer.getCompanyName().length() > 50) {
                    throw new ResourceNotFoundException("Company Name cannot exceed 50 characters");
                }
                if (employer.getFromDate() == null) {
                    throw new ResourceNotFoundException("From Date is required for previous employer: " + employer.getCompanyName());
                }
                if (employer.getToDate() == null) {
                    throw new ResourceNotFoundException("To Date is required for previous employer: " + employer.getCompanyName());
                }
                if (employer.getDesignation() == null || employer.getDesignation().trim().isEmpty()) {
                    throw new ResourceNotFoundException("Designation is required for previous employer: " + employer.getCompanyName());
                }
                if (employer.getDesignation().length() > 50) {
                    throw new ResourceNotFoundException("Designation cannot exceed 50 characters");
                }
                if (employer.getLeavingReason() == null || employer.getLeavingReason().trim().isEmpty()) {
                    throw new ResourceNotFoundException("Leaving Reason is required for previous employer: " + employer.getCompanyName());
                }
                if (employer.getLeavingReason().length() > 50) {
                    throw new ResourceNotFoundException("Leaving Reason cannot exceed 50 characters");
                }
                if (employer.getNatureOfDuties() == null || employer.getNatureOfDuties().trim().isEmpty()) {
                    throw new ResourceNotFoundException("Nature of Duties is required for previous employer: " + employer.getCompanyName());
                }
                if (employer.getNatureOfDuties().length() > 50) {
                    throw new ResourceNotFoundException("Nature of Duties cannot exceed 50 characters");
                }
                if (employer.getCompanyAddressLine1() == null || employer.getCompanyAddressLine1().trim().isEmpty()) {
                    throw new ResourceNotFoundException("Company Address Line 1 is required for previous employer: " + employer.getCompanyName());
                }
                String companyAddress = employer.getCompanyAddressLine1() + 
                        (employer.getCompanyAddressLine2() != null ? " " + employer.getCompanyAddressLine2() : "");
                if (companyAddress.trim().length() > 50) {
                    throw new ResourceNotFoundException("Company Address cannot exceed 50 characters");
                }
            }
        }
    }

    /**
     * Validate Aadhaar number using Verhoeff algorithm
     * This algorithm checks the mathematical validity of the Aadhaar number structure
     * * @param aadhaar 12-digit Aadhaar number
     * @return true if Aadhaar format is valid, false otherwise
     */
    private boolean isValidAadhaar(String aadhaar) {
        // 1. Basic Regex Check: Length 12, cannot start with 0 or 1, numeric only
        if (aadhaar == null || !aadhaar.matches("^[2-9][0-9]{11}$")) {
            return false;
        }

        // Verhoeff multiplication table
        int[][] multiplicationTable = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
            {1, 2, 3, 4, 0, 6, 7, 8, 9, 5},
            {2, 3, 4, 0, 1, 7, 8, 9, 5, 6},
            {3, 4, 0, 1, 2, 8, 9, 5, 6, 7},
            {4, 0, 1, 2, 3, 9, 5, 6, 7, 8},
            {5, 9, 8, 7, 6, 0, 4, 3, 2, 1},
            {6, 5, 9, 8, 7, 1, 0, 4, 3, 2},
            {7, 6, 5, 9, 8, 2, 1, 0, 4, 3},
            {8, 7, 6, 5, 9, 3, 2, 1, 0, 4},
            {9, 8, 7, 6, 5, 4, 3, 2, 1, 0}
        };

        // Verhoeff permutation table
        int[][] permutationTable = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
            {1, 5, 7, 6, 2, 8, 3, 0, 9, 4},
            {5, 8, 0, 3, 7, 9, 6, 1, 4, 2},
            {8, 9, 1, 6, 0, 4, 3, 5, 2, 7},
            {9, 4, 5, 3, 1, 2, 6, 8, 7, 0},
            {4, 2, 8, 6, 5, 7, 3, 9, 0, 1},
            {2, 7, 9, 3, 8, 0, 6, 4, 1, 5},
            {7, 0, 4, 6, 9, 1, 3, 2, 5, 8}
        };

        int check = 0;
        int[] digits = new int[12];

        for (int i = 0; i < 12; i++) {
            digits[i] = Character.getNumericValue(aadhaar.charAt(i));
        }

        // Apply Verhoeff algorithm
        for (int i = 0; i < 12; i++) {
            // FIX: Changed ((i + 1) % 8) to (i % 8)
            // When i=0 (last digit), we must use permutation row 0.
            int c = digits[11 - i];
            int p = permutationTable[i % 8][c];
            check = multiplicationTable[check][p];
        }

        return check == 0;
    }
}