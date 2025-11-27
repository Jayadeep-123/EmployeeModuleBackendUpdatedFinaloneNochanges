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

import com.employee.dto.AddressInfoDTO;
import com.employee.dto.AgreementInfoDTO;
import com.employee.dto.BankInfoDTO;
import com.employee.dto.BasicInfoDTO;
import com.employee.dto.CategoryInfoDTO;
import com.employee.dto.DocumentDTO;
import com.employee.dto.FamilyInfoDTO;
import com.employee.dto.PreviousEmployerInfoDTO;
import com.employee.dto.QualificationDTO;
import com.employee.entity.BankDetails;
import com.employee.entity.Building;
import com.employee.entity.EmpChequeDetails;
import com.employee.entity.EmpDetails;
import com.employee.entity.EmpDocuments;
import com.employee.entity.EmpExperienceDetails;
import com.employee.entity.EmpFamilyDetails;
import com.employee.entity.EmpPaymentType;
import com.employee.entity.EmpPfDetails;
import com.employee.entity.EmpQualification;
import com.employee.entity.EmpaddressInfo;
import com.employee.entity.Employee;
import com.employee.entity.EmployeeCheckListStatus;
import com.employee.entity.OrgBank;
import com.employee.entity.OrgBankBranch;
import com.employee.exception.ResourceNotFoundException;
import com.employee.repository.BankDetailsRepository;
import com.employee.repository.BloodGroupRepository;
import com.employee.repository.BuildingRepository;
import com.employee.repository.CampusRepository;
import com.employee.repository.CasteRepository;
import com.employee.repository.CategoryRepository;
import com.employee.repository.CityRepository;
import com.employee.repository.CountryRepository;
import com.employee.repository.DepartmentRepository;
import com.employee.repository.DesignationRepository;
import com.employee.repository.EmpChequeDetailsRepository;
import com.employee.repository.EmpDocTypeRepository;
import com.employee.repository.EmpDocumentsRepository;
import com.employee.repository.EmpExperienceDetailsRepository;
import com.employee.repository.EmpFamilyDetailsRepository;
import com.employee.repository.EmpPaymentTypeRepository;
import com.employee.repository.EmpQualificationRepository;
import com.employee.repository.EmpaddressInfoRepository;
import com.employee.repository.EmployeeCheckListStatusRepository;
import com.employee.repository.EmployeeRepository;
import com.employee.repository.EmployeeTypeRepository;
import com.employee.repository.GenderRepository;
import com.employee.repository.JoiningAsRepository;
import com.employee.repository.MaritalStatusRepository;
import com.employee.repository.ModeOfHiringRepository;
import com.employee.repository.OccupationRepository;
import com.employee.repository.OrgBankBranchRepository;
import com.employee.repository.OrgBankRepository;
import com.employee.repository.QualificationDegreeRepository;
import com.employee.repository.QualificationRepository;
import com.employee.repository.RelationRepository;
import com.employee.repository.RelegionRepository;
import com.employee.repository.StateRepository;
import com.employee.repository.WorkingModeRepository;

/**
 * Service for Employee Entity Preparation operations.
 * Contains entity creation/preparation methods extracted from EmployeeService for better organization.
 */
@Service
public class EmployeeEntityPreparationService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeEntityPreparationService.class);

    @Autowired private CountryRepository countryRepository;
    @Autowired private StateRepository stateRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private RelationRepository relationRepository;
    @Autowired private GenderRepository genderRepository;
    @Autowired private BloodGroupRepository bloodGroupRepository;
    @Autowired private EmpDocTypeRepository empDocTypeRepository;
    @Autowired private QualificationRepository qualificationRepository;
    @Autowired private QualificationDegreeRepository qualificationDegreeRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private EmpaddressInfoRepository empaddressInfoRepository;
    @Autowired private EmpFamilyDetailsRepository empFamilyDetailsRepository;
    @Autowired private EmpExperienceDetailsRepository empExperienceDetailsRepository;
    @Autowired private EmpQualificationRepository empQualificationRepository;
    @Autowired private EmpDocumentsRepository empDocumentsRepository;
    @Autowired private BankDetailsRepository bankDetailsRepository;
    @Autowired private CampusRepository campusRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private DesignationRepository designationRepository;
    @Autowired private EmployeeTypeRepository employeeTypeRepository;
    @Autowired private EmployeeCheckListStatusRepository employeeCheckListStatusRepository;
    @Autowired private CasteRepository casteRepository;
    @Autowired private RelegionRepository relegionRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private MaritalStatusRepository maritalStatusRepository;
    @Autowired private WorkingModeRepository workingModeRepository;
    @Autowired private JoiningAsRepository joiningAsRepository;
    @Autowired private ModeOfHiringRepository modeOfHiringRepository;
    @Autowired private EmpPaymentTypeRepository empPaymentTypeRepository;
    @Autowired private OccupationRepository occupationRepository;
    @Autowired private OrgBankRepository orgBankRepository;
    @Autowired private OrgBankBranchRepository orgBankBranchRepository;
    @Autowired private EmpChequeDetailsRepository empChequeDetailsRepository;
    @Autowired private BuildingRepository buildingRepository;

    public EmpPfDetails prepareEmpPfDetailsEntity(BasicInfoDTO basicInfo, Employee employee, Integer createdBy) {
        if (basicInfo == null) return null;
        if (basicInfo.getPreUanNum() == null && basicInfo.getPreEsiNum() == null) {
            return null;
        }
        EmpPfDetails empPfDetails = new EmpPfDetails();
        empPfDetails.setEmployee_id(employee);
        empPfDetails.setPre_esi_no(basicInfo.getPreEsiNum());
        empPfDetails.setIs_active(1);
        if (createdBy != null && createdBy > 0) {
            empPfDetails.setCreated_by(createdBy);
            empPfDetails.setCreated_date(new Timestamp(System.currentTimeMillis()));
        }
        return empPfDetails;
    }


    public Employee prepareEmployeeEntity(BasicInfoDTO basicInfo) {
        if (basicInfo == null) throw new ResourceNotFoundException("Basic Info is required");

        Employee employee = new Employee();
        employee.setFirst_name(basicInfo.getFirstName());
        employee.setLast_name(basicInfo.getLastName());
        employee.setDate_of_join(basicInfo.getDateOfJoin());
        employee.setPrimary_mobile_no(basicInfo.getPrimaryMobileNo());
        employee.setEmail(null);
        if (basicInfo.getTotalExperience() != null) employee.setTotal_experience(basicInfo.getTotalExperience().doubleValue());
        if (basicInfo.getAge() != null) employee.setAge(basicInfo.getAge());
        if (basicInfo.getSscNo() != null) employee.setSsc_no(basicInfo.getSscNo());
        employee.setIs_active(1);
        employee.setStatus("ACTIVE");
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
        if (basicInfo.getBuildingId() != null && basicInfo.getBuildingId() > 0) {
            Building building = buildingRepository.findById(basicInfo.getBuildingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Building not found with ID: " + basicInfo.getBuildingId()));
            if (building.getIsActive() != 1) throw new ResourceNotFoundException("Building with ID: " + basicInfo.getBuildingId() + " is not active");
            employee.setBuilding_id(building);
        } else {
            employee.setBuilding_id(null);
        }
        if (basicInfo.getGenderId() != null) {
            employee.setGender(genderRepository.findByIdAndIsActive(basicInfo.getGenderId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Gender not found")));
        }
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
                    throw new ResourceNotFoundException("replacedByEmpId is required when joinTypeId is 3 (Replacement).");
                }
                employee.setEmployee_replaceby_id(employeeRepository.findByIdAndIs_active(basicInfo.getReplacedByEmpId(), 0)
                        .orElseThrow(() -> new ResourceNotFoundException("Inactive Replacement Employee not found with ID: " + basicInfo.getReplacedByEmpId())));
            } else if (basicInfo.getReplacedByEmpId() != null && basicInfo.getReplacedByEmpId() > 0) {
                employee.setEmployee_replaceby_id(employeeRepository.findByIdAndIs_active(basicInfo.getReplacedByEmpId(), 0).orElse(null));
            } else {
                employee.setEmployee_replaceby_id(null);
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
                    java.sql.Date startDate = basicInfo.getContractStartDate() != null ? basicInfo.getContractStartDate() : basicInfo.getDateOfJoin();
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
        EmployeeCheckListStatus pendingAtDOStatus = employeeCheckListStatusRepository.findByCheck_app_status_name("Pending at DO")
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeCheckListStatus with name 'Pending at DO' not found"));
        employee.setEmp_check_list_status_id(pendingAtDOStatus);
        if (basicInfo.getReferenceEmpId() != null && basicInfo.getReferenceEmpId() > 0) {
            employee.setEmployee_reference(employeeRepository.findByIdAndIs_active(basicInfo.getReferenceEmpId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Reference Employee not found")));
        } else {
            employee.setEmployee_reference(null);
        }
        if (basicInfo.getHiredByEmpId() != null && basicInfo.getHiredByEmpId() > 0) {
            employee.setEmployee_hired(employeeRepository.findByIdAndIs_active(basicInfo.getHiredByEmpId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Hired By Employee not found")));
        } else {
            employee.setEmployee_hired(null);
        }
        if (basicInfo.getManagerId() != null && basicInfo.getManagerId() > 0) {
            employee.setEmployee_manager_id(employeeRepository.findByIdAndIs_active(basicInfo.getManagerId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Manager not found")));
        } else {
            employee.setEmployee_manager_id(null);
        }
        if (basicInfo.getReportingManagerId() != null && basicInfo.getReportingManagerId() > 0) {
            employee.setEmployee_reporting_id(employeeRepository.findByIdAndIs_active(basicInfo.getReportingManagerId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Reporting Manager not found")));
        } else {
            employee.setEmployee_reporting_id(null);
        }
        if (basicInfo.getPreChaitanyaId() != null && basicInfo.getPreChaitanyaId() > 0) {
            employee.setPre_chaitanya_id(employeeRepository.findByIdAndIs_active(basicInfo.getPreChaitanyaId(), 0)
                    .orElseThrow(() -> new ResourceNotFoundException("Previous Chaitanya Employee not found or active")));
        } else {
            employee.setPre_chaitanya_id(null);
        }
        return employee;
    }

    public EmpDetails prepareEmpDetailsEntity(BasicInfoDTO basicInfo, AddressInfoDTO addressInfo, Employee employee, Integer createdBy) {
        if (basicInfo == null) throw new ResourceNotFoundException("Basic Info is required");
        EmpDetails empDetails = new EmpDetails();
        empDetails.setEmployee_id(employee);
        empDetails.setAdhaar_name(basicInfo.getAdhaarName());
        empDetails.setDate_of_birth(basicInfo.getDateOfBirth());
        empDetails.setPersonal_email(basicInfo.getEmail());
        if (basicInfo.getEmergencyPhNo() == null || basicInfo.getEmergencyPhNo().trim().isEmpty()) {
            throw new ResourceNotFoundException("Emergency contact phone number is required");
        }
        empDetails.setEmergency_ph_no(basicInfo.getEmergencyPhNo().trim());
        if (basicInfo.getEmergencyRelationId() != null && basicInfo.getEmergencyRelationId() > 0) {
            empDetails.setRelation_id(relationRepository.findById(basicInfo.getEmergencyRelationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Emergency Relation not found")));
        } else {
            empDetails.setRelation_id(null);
        }
        empDetails.setAdhaar_no(basicInfo.getAadharNum());
        empDetails.setPancard_no(basicInfo.getPancardNum());
        empDetails.setAdhaar_enrolment_no(basicInfo.getAadharEnrolmentNum());
        empDetails.setPassout_year(0);
        empDetails.setIs_active(1);
        empDetails.setStatus("ACTIVE");
        if (basicInfo.getBloodGroupId() == null) throw new ResourceNotFoundException("BloodGroup ID is required");
        empDetails.setBloodGroup_id(bloodGroupRepository.findByIdAndIsActive(basicInfo.getBloodGroupId(), 1)
                .orElseThrow(() -> new ResourceNotFoundException("Active BloodGroup not found")));
        if (basicInfo.getCasteId() == null) throw new ResourceNotFoundException("Caste ID is required");
        empDetails.setCaste_id(casteRepository.findById(basicInfo.getCasteId())
                .orElseThrow(() -> new ResourceNotFoundException("Caste not found")));
        if (basicInfo.getReligionId() == null) throw new ResourceNotFoundException("Religion ID is required");
        empDetails.setReligion_id(relegionRepository.findById(basicInfo.getReligionId())
                .orElseThrow(() -> new ResourceNotFoundException("Religion not found")));
        if (basicInfo.getMaritalStatusId() == null) throw new ResourceNotFoundException("MaritalStatus ID is required");
        empDetails.setMarital_status_id(maritalStatusRepository.findByIdAndIsActive(basicInfo.getMaritalStatusId(), 1)
                .orElseThrow(() -> new ResourceNotFoundException("Active MaritalStatus not found")));
        if (createdBy != null && createdBy > 0) {
            empDetails.setCreated_by(createdBy);
            empDetails.setCreated_date(new Timestamp(System.currentTimeMillis()));
        }
        if (basicInfo.getFatherName() != null) {
            empDetails.setFatherName(basicInfo.getFatherName().trim());
        }
        if (basicInfo.getUanNo() != null) {
            empDetails.setUanNo(basicInfo.getUanNo());
        }
        return empDetails;
    }



    public void updateEmployeeEntity(Employee employee, BasicInfoDTO basicInfo) {
        if (basicInfo == null) return;
        if (basicInfo.getFirstName() != null && !basicInfo.getFirstName().trim().isEmpty()) employee.setFirst_name(basicInfo.getFirstName());
        if (basicInfo.getLastName() != null && !basicInfo.getLastName().trim().isEmpty()) employee.setLast_name(basicInfo.getLastName());
        if (basicInfo.getDateOfJoin() != null) employee.setDate_of_join(basicInfo.getDateOfJoin());
        if (basicInfo.getPrimaryMobileNo() != null && basicInfo.getPrimaryMobileNo() > 0) employee.setPrimary_mobile_no(basicInfo.getPrimaryMobileNo());
        employee.setEmail(null);
        if (basicInfo.getTotalExperience() != null) employee.setTotal_experience(basicInfo.getTotalExperience().doubleValue());
        if (basicInfo.getAge() != null) employee.setAge(basicInfo.getAge());
        if (basicInfo.getSscNo() != null) employee.setSsc_no(basicInfo.getSscNo());
        if (basicInfo.getTempPayrollId() != null && !basicInfo.getTempPayrollId().trim().isEmpty()) employee.setTempPayrollId(basicInfo.getTempPayrollId());
        if (basicInfo.getCreatedBy() != null && basicInfo.getCreatedBy() > 0) employee.setCreated_by(basicInfo.getCreatedBy());
        if (basicInfo.getCampusId() != null) {
            employee.setCampus_id(campusRepository.findByCampusIdAndIsActive(basicInfo.getCampusId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Campus not found")));
        }
        if (basicInfo.getBuildingId() != null && basicInfo.getBuildingId() > 0) {
            Building building = buildingRepository.findById(basicInfo.getBuildingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Building not found"));
            if (building.getIsActive() != 1) throw new ResourceNotFoundException("Building is not active");
            employee.setBuilding_id(building);
        } else {
            // Clear building_id if buildingId is null or 0
            employee.setBuilding_id(null);
        }
        if (basicInfo.getGenderId() != null) {
            employee.setGender(genderRepository.findByIdAndIsActive(basicInfo.getGenderId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Gender not found")));
        }
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
                    throw new ResourceNotFoundException("replacedByEmpId is required when joinTypeId is 3.");
                }
                employee.setEmployee_replaceby_id(employeeRepository.findByIdAndIs_active(basicInfo.getReplacedByEmpId(), 0)
                        .orElseThrow(() -> new ResourceNotFoundException("Inactive Replacement Employee not found")));
            } else if (basicInfo.getReplacedByEmpId() != null && basicInfo.getReplacedByEmpId() > 0) {
                employee.setEmployee_replaceby_id(employeeRepository.findByIdAndIs_active(basicInfo.getReplacedByEmpId(), 0).orElse(null));
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
                    java.sql.Date startDate = basicInfo.getContractStartDate() != null ? basicInfo.getContractStartDate() : basicInfo.getDateOfJoin();
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
                    .orElseThrow(() -> new ResourceNotFoundException("Active Reference Employee not found")));
        } else {
            employee.setEmployee_reference(null);
        }
        if (basicInfo.getHiredByEmpId() != null && basicInfo.getHiredByEmpId() > 0) {
            employee.setEmployee_hired(employeeRepository.findByIdAndIs_active(basicInfo.getHiredByEmpId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Hired By Employee not found")));
        } else {
            employee.setEmployee_hired(null);
        }
        if (basicInfo.getManagerId() != null && basicInfo.getManagerId() > 0) {
            employee.setEmployee_manager_id(employeeRepository.findByIdAndIs_active(basicInfo.getManagerId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Manager not found")));
        } else {
            employee.setEmployee_manager_id(null);
        }
        if (basicInfo.getReportingManagerId() != null && basicInfo.getReportingManagerId() > 0) {
            employee.setEmployee_reporting_id(employeeRepository.findByIdAndIs_active(basicInfo.getReportingManagerId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Reporting Manager not found")));
        } else {
            employee.setEmployee_reporting_id(null);
        }
        if (basicInfo.getPreChaitanyaId() != null && basicInfo.getPreChaitanyaId() > 0) {
            employee.setPre_chaitanya_id(employeeRepository.findByIdAndIs_active(basicInfo.getPreChaitanyaId(), 0)
                    .orElseThrow(() -> new ResourceNotFoundException("Previous Chaitanya Employee not found or active")));
        } else {
            employee.setPre_chaitanya_id(null);
        }
    }

    public void updateEmpDetailsFields(EmpDetails target, EmpDetails source) {
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

    public void updateEmpDetailsFieldsExceptEmail(EmpDetails target, EmpDetails source) {
        target.setAdhaar_name(source.getAdhaar_name());
        target.setDate_of_birth(source.getDate_of_birth());
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

}