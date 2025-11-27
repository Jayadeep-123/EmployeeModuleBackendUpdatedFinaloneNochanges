package com.employee.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.employee.dto.CampusContactDTO;
import com.employee.dto.CampusDto;
import com.employee.dto.GenericDropdownDTO;
import com.employee.dto.OrganizationDTO;
import com.employee.entity.Department;
import com.employee.entity.Employee;
import com.employee.entity.EmployeeType;
import com.employee.entity.OrgBank;
import com.employee.entity.Subject;
import com.employee.repository.BuildingRepository;
import com.employee.repository.CampusContactRepository;
import com.employee.repository.CampusRepository;
import com.employee.repository.CategoryRepository;
import com.employee.repository.CityRepository;
import com.employee.repository.CmpsOrgRepository;
import com.employee.repository.CostCenterRepository;
import com.employee.repository.CountryRepository;
import com.employee.repository.DepartmentRepository;
import com.employee.repository.DesignationRepository;
import com.employee.repository.EmpAppCheckListDetlRepository;
import com.employee.repository.EmpDocTypeRepository;
import com.employee.repository.EmpPaymentTypeRepository;
import com.employee.repository.EmployeeLevelRepository;
import com.employee.repository.EmployeeRepository;
import com.employee.repository.EmployeeTypeRepository;
import com.employee.repository.GradeRepository;
import com.employee.repository.JoiningAsRepository;
import com.employee.repository.MaritalStatusRepository;
import com.employee.repository.ModeOfHiringRepository;
import com.employee.repository.OccupationRepository;
import com.employee.repository.OrientationRepository;
import com.employee.repository.OrgBankBranchRepository;
import com.employee.repository.OrgBankRepository;
import com.employee.repository.OrganizationRepository;
import com.employee.repository.QualificationDegreeRepository;
import com.employee.repository.QualificationRepository;
import com.employee.repository.StreamRepository;
import com.employee.repository.StructureRepository;
import com.employee.repository.SubjectRepository;
import com.employee.repository.WorkingModeRepository;

@Service
public class DropDownService {
	
	@Autowired MaritalStatusRepository  maritalStatusRepo;
	@Autowired QualificationRepository qualificationRepo;
	@Autowired WorkingModeRepository  workingModeRepo;
	@Autowired JoiningAsRepository joiningAsRepo;
	@Autowired ModeOfHiringRepository modeOfHiringRepo;
	@Autowired EmployeeTypeRepository employeeTypeRepo;
	@Autowired EmpPaymentTypeRepository employeePaymentTypeRepo;
	@Autowired DepartmentRepository departmentRepo;
	@Autowired DesignationRepository designationRepo;
	@Autowired CountryRepository countryRepo;
	@Autowired QualificationDegreeRepository qualificationDegreeRepo;
	@Autowired SubjectRepository subjectRepo;
	@Autowired GradeRepository gradeRepo;
	@Autowired CampusRepository campusRepository;
	@Autowired  CostCenterRepository costCenterRepo;
	@Autowired StructureRepository structureRepo;
	@Autowired OrganizationRepository organizationRepo;
	@Autowired CmpsOrgRepository cmpsOrgRepository;
	@Autowired BuildingRepository buildingRepository;
	@Autowired StreamRepository  streamRepository;
	@Autowired EmployeeLevelRepository empLevelRepository;
	@Autowired OrgBankRepository orgBankRepository;
	@Autowired OrgBankBranchRepository orgBankBranchRepository;
	@Autowired EmployeeRepository employeeRepository;
	@Autowired CampusContactRepository campusContactRepository;
	@Autowired CategoryRepository categoryRepository;
	@Autowired CityRepository cityRepository;
	@Autowired OccupationRepository occupationRepository;
	@Autowired OrientationRepository orientationRepository;
	@Autowired EmpAppCheckListDetlRepository empAppCheckListDetlRepository;
	@Autowired EmpDocTypeRepository empDocTypeRepository;
	
	
	private static final int ACTIVE_STATUS = 1;
	
	public List<GenericDropdownDTO> getMaritalStatusTypes() {
	    return maritalStatusRepo.findByIsActive(ACTIVE_STATUS).stream()
	            // Use the getters that match your entity's field names
	            .map(t -> new GenericDropdownDTO(t.getMarital_status_id(), t.getMarital_status_type()))
	            .collect(Collectors.toList());
	}
	public List<GenericDropdownDTO> getQualificationTypes() {
	    return qualificationRepo.findByIsActive(ACTIVE_STATUS).stream()
	            // Change the getter calls to match your entity's field names
	            .map(q -> new GenericDropdownDTO(q.getQualification_id(), q.getQualification_name()))
	            .collect(Collectors.toList());
	}

	public List<GenericDropdownDTO> getWorkModeTypes() {
	    return workingModeRepo.findByIsActive(ACTIVE_STATUS).stream()
	            // Change the getter calls to match your entity's field names
	            .map(m -> new GenericDropdownDTO(m.getEmp_work_mode_id(), m.getWork_mode_type()))
	            .collect(Collectors.toList());
	}

	public List<GenericDropdownDTO> getJoinAsTypes() {
	    return joiningAsRepo.findByIsActive(ACTIVE_STATUS).stream()
	            // Change the getter calls to match your entity's field names
	            .map(j -> new GenericDropdownDTO(j.getJoin_type_id(), j.getJoin_type()))
	            .collect(Collectors.toList());
	}

	public List<GenericDropdownDTO> getModeOfHiringTypes() {
	    return modeOfHiringRepo.findByIsActive(ACTIVE_STATUS).stream()
	            // Change the getter calls to match your entity's field names
	            .map(m -> new GenericDropdownDTO(m.getMode_of_hiring_id(), m.getMode_of_hiring_name()))
	            .collect(Collectors.toList());
	}

	public List<GenericDropdownDTO> getEmployeeTypes() {
	    List<EmployeeType> activeEmpTypes = employeeTypeRepo.findByIsActive(ACTIVE_STATUS);
	    return activeEmpTypes.stream()
	            // Change the getter calls to match your entity's field names
	            .map(type -> new GenericDropdownDTO(type.getEmp_type_id(), type.getEmp_type()))
	            .collect(Collectors.toList());
	}

	public List<GenericDropdownDTO> getEmployeePaymentTypes() {
	    return employeePaymentTypeRepo.findByIsActive(ACTIVE_STATUS).stream()
	            // Change the getter calls to match your entity's field names
	            .map(p -> new GenericDropdownDTO(p.getEmp_payment_type_id(), p.getPayment_type()))
	            .collect(Collectors.toList());
	}
	
	public List<GenericDropdownDTO> getActiveDepartmentsByEmpTypeId(int empTypeId) {
	    List<Department> departments = departmentRepo.findByEmpTypeId_EmpTypeIdAndIsActive(empTypeId, 1);

	    return departments.stream()
	            // Change the getter calls to match your entity's field names
	            .map(dept -> new GenericDropdownDTO(dept.getDepartment_id(), dept.getDepartment_name()))
	            .collect(Collectors.toList());
	}

	public List<GenericDropdownDTO> getDesignations(int departmentId) {
	    // TODO Auto-generated method stub
	    final int ACTIVE_STATUS = 1;
	    return designationRepo.findByDepartmentId_DepartmentIdAndIsActive(departmentId, ACTIVE_STATUS)
	            .stream()
	            // Change the getter calls to match your entity's field names
	            .map(d -> new GenericDropdownDTO(d.getDesignation_id(), d.getDesignation_name()))
	            .collect(Collectors.toList());
	}
	  public List<GenericDropdownDTO> getCountries() {
		// TODO Auto-generated method stub
		return countryRepo.findAll().stream().map(c -> new GenericDropdownDTO(c.getCountryId(), c.getCountryName()))
				.collect(Collectors.toList());
	  }

	  public List<GenericDropdownDTO> getDegrees(int qualificationId) {
		    final int ACTIVE_STATUS = 1;
		    
		    return qualificationDegreeRepo.findByQualification_QualificationIdAndIsActive(qualificationId, ACTIVE_STATUS)
		            .stream()
		            // Change the getter calls to match your entity's field names
		            .map(d -> new GenericDropdownDTO(d.getQualification_degree_id(), d.getDegree_name()))
		            .collect(Collectors.toList());
		}
	  
	  public List<GenericDropdownDTO> getSubjects() {
		    final int ACTIVE_STATUS = 1;
		    
		    // 1. Fetch all active subjects
		    List<Subject> activeSubjects = subjectRepo.findByIsActive(ACTIVE_STATUS);

		    // 2. Map to DTO (using the correct underscore getters)
		    return activeSubjects.stream()
		            .map(subject -> new GenericDropdownDTO(
		                subject.getSubject_id(), 
		                subject.getSubject_name()
		            ))
		            .collect(Collectors.toList());
		}
	  
	  public CampusDto getActiveCampusById(int campusId) {
	        return campusRepository.findActiveCampusById(campusId)
	                .orElseThrow(() -> new RuntimeException("Active campus not found for ID: " + campusId));
	    }

	    public List<GenericDropdownDTO> getActiveGrades() {
	        return gradeRepo.findByIsActive(1)
	                .stream()
	                .map(g -> new GenericDropdownDTO(g.getEmpGradeId(), g.getGradeName()))
	                .collect(Collectors.toList());
	    }

	    public List<GenericDropdownDTO> getActiveStructures() {
	        return structureRepo.findByIsActive(1)
	                .stream()
	                .map(s -> new GenericDropdownDTO(s.getEmpStructureId(), s.getStructureName()))
	                .collect(Collectors.toList());
	    }
	    


	    public List<GenericDropdownDTO> getCostCenters() {
	        return costCenterRepo.findAll()
	                .stream()
	                .map(c -> new GenericDropdownDTO(
	                        c.getCostCenterId(),
	                        c.getCostCenterName()
	                ))
	                .collect(Collectors.toList());
	    }


	    public List<OrganizationDTO> getAllActiveOrganizations() {
	        return organizationRepo.findByIsActive(1)
	                .stream()
	                .map(org -> new OrganizationDTO(org.getOrganizationId(), org.getOrganizationName(), org.getOrganizationType(), org.getOrganizationAddress(), org.getOrganizationCode(), org.getOrganizationHead()
	                ))
	                .collect(Collectors.toList());
	    }

	    public List<GenericDropdownDTO> getOrganizationsByCampusId(int campusId) {
	        return cmpsOrgRepository.findOrganizationsByCampusId(campusId);
	    }
	    
	    public List<GenericDropdownDTO> getBuildingsByCampusId(int campusId) {
	        return buildingRepository.findBuildingsByCampusId(campusId);
	    }
	    
	    public List<GenericDropdownDTO> getAllActiveStreams() {
	        return streamRepository.findAllActiveStreams();
	    }
	    
	    public List<GenericDropdownDTO> getAllActiveEmpLevels() {
	        return empLevelRepository.findAllActiveEmpLevels();
	    }
	    
    public List<GenericDropdownDTO> getAllActiveBanks() {
        List<OrgBank> activeBanks = orgBankRepository.findByIsActive(ACTIVE_STATUS);

        // Convert to GenericDto, using the correct underscore getters
        return activeBanks.stream()
                .map(bank -> new GenericDropdownDTO(bank.getOrg_bank_id(), bank.getBank_name()))
                .collect(Collectors.toList());
    }
    
    public List<GenericDropdownDTO> getAllActiveBankBranches() {
        return orgBankBranchRepository.findByIsActive(ACTIVE_STATUS).stream()
                .map(branch -> new GenericDropdownDTO(branch.getOrg_bank_branch_id(), branch.getBranch_name()))
                .collect(Collectors.toList());
    }
    
    public List<GenericDropdownDTO> getActiveBankBranchesByBankId(Integer bankId) {
        return orgBankBranchRepository.findActiveBranchesByBankId(bankId).stream()
                .map(branch -> new GenericDropdownDTO(branch.getOrg_bank_branch_id(), branch.getBranch_name()))
                .collect(Collectors.toList());
    }
    
    public List<GenericDropdownDTO> getActiveChecklistDetails() {
        return empAppCheckListDetlRepository.findByIsActive(ACTIVE_STATUS).stream()
                .map(checklist -> new GenericDropdownDTO(checklist.getEmpAppCheckListDetlId(), checklist.getCheckListDetlName()))
                .collect(Collectors.toList());
    }
    
    public List<GenericDropdownDTO> getActiveDocumentTypes() {
        return empDocTypeRepository.findByIsActive(ACTIVE_STATUS).stream()
                .map(docType -> new GenericDropdownDTO(docType.getDoc_type_id(), docType.getDoc_name()))
                .collect(Collectors.toList());
    }
	    public List<GenericDropdownDTO> getActiveEmployees() {
	        return employeeRepository.findByIsActive(1)
	                .stream()
	                .map(emp -> new GenericDropdownDTO(
	                        emp.getEmp_id(), // Changed from getEmpId()
	                        emp.getFirst_name() + " " + emp.getLast_name()
	                ))
	                .collect(Collectors.toList());
	    }

	    // ✅ 2. Get Inactive Employees (isActive = 0)
	    public List<GenericDropdownDTO> getInactiveEmployees() {
	        return employeeRepository.findByIsActive(0)
	                .stream()
	                .map(emp -> new GenericDropdownDTO(
	                        emp.getEmp_id(),
	                        emp.getFirst_name() + " " + emp.getLast_name()
	                ))
	                .collect(Collectors.toList());
	    }
	    
	    public List<CampusContactDTO> getActiveContactsByCampusId(Integer campusId) {
	        // Now the service calls the repository
	        return campusContactRepository.findActiveContactsByCampusId(campusId);
	    }
	    
	    
	    
	    public List<GenericDropdownDTO> getAllEmployees() {
			List<Employee> activeEmployees = employeeRepository.findByIsActive(1);
	 
			return activeEmployees.stream().map(employee -> new GenericDropdownDTO(employee.getEmp_id(),
					employee.getFirst_name() + " " + employee.getLast_name())).collect(Collectors.toList());
		}
		
		public List<GenericDropdownDTO> getAllOrganizations() {
			return organizationRepo.findAll().stream().map(org -> new GenericDropdownDTO(org.getOrganizationId(), org.getOrganizationName()))
					.collect(Collectors.toList());
		}
		
		public List<GenericDropdownDTO> getActiveCategories() {
			return categoryRepository.findByIsActive(ACTIVE_STATUS).stream()
					.map(c -> new GenericDropdownDTO(c.getCategory_id(), c.getCategory_name()))
					.collect(Collectors.toList());
		}
		
		public List<GenericDropdownDTO> getActiveCampuses() {
			return campusRepository.findByIsActive(ACTIVE_STATUS).stream()
					.map(c -> new GenericDropdownDTO(c.getCampusId(), c.getCampusName()))
					.collect(Collectors.toList());
		}
		
		public List<GenericDropdownDTO> getActiveEmployeesByCampusId(Integer campusId) {
			return employeeRepository.findActiveEmployeesByCampusId(campusId).stream()
					.map(emp -> new GenericDropdownDTO(
							emp.getEmp_id(),
							emp.getFirst_name() + " " + emp.getLast_name()
					))
					.collect(Collectors.toList());
		}
		
	public List<GenericDropdownDTO> getCitiesByDistrictId(Integer districtId) {
		return cityRepository.findByDistrictId(districtId).stream()
				.map(city -> new GenericDropdownDTO(city.getCityId(), city.getCityName()))
				.collect(Collectors.toList());
	}
	
	public List<GenericDropdownDTO> getActiveOccupations() {
		return occupationRepository.findByIsActive(ACTIVE_STATUS).stream()
				.map(o -> new GenericDropdownDTO(o.getOccupation_id(), o.getOccupation_name()))
				.collect(Collectors.toList());
	}
	
	public List<GenericDropdownDTO> getActiveOrientations() {
		return orientationRepository.findByIsActive(ACTIVE_STATUS).stream()
				.map(o -> new GenericDropdownDTO(o.getOrientationId(), o.getOrientationName()))
				.collect(Collectors.toList());
	}

}