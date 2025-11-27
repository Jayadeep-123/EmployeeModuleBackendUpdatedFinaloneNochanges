package com.employee.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.employee.dto.CampusContactDTO;
import com.employee.dto.CampusDto;
import com.employee.dto.GenericDropdownDTO;
import com.employee.dto.OrganizationDTO;
import com.employee.entity.CampusContact;
import com.employee.service.DropDownService;

@RestController
@RequestMapping("/api/employeeModule")
public class DropDownsController {
	
	@Autowired DropDownService empDropdownService;
	
	
	 @GetMapping("/marital-status")
	    public ResponseEntity<?> getMaritalStatusTypes() {
	        List<GenericDropdownDTO> maritalStatuses = empDropdownService.getMaritalStatusTypes();
	        
	        if (maritalStatuses == null || maritalStatuses.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No active marital status types found");
	        }
	        
	        return ResponseEntity.ok(maritalStatuses);
	    }
	 @GetMapping("/qualifications")
	    public ResponseEntity<?> getQualificationTypes() {
	        List<GenericDropdownDTO> qualifications = empDropdownService.getQualificationTypes();
	        
	        if (qualifications == null || qualifications.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No active qualifications found");
	        }
	        
	        return ResponseEntity.ok(qualifications);
	    }
	 
	 @GetMapping("/work-mode")
	    public ResponseEntity<?> getWorkModeTypes() {
	        List<GenericDropdownDTO> workModes = empDropdownService.getWorkModeTypes();
	        
	        if (workModes == null || workModes.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No active work modes found");
	        }
	        
	        return ResponseEntity.ok(workModes);
	    }
	 
	 @GetMapping("/joining-as")
	    public ResponseEntity<?> getJoinAsTypes() {
	        List<GenericDropdownDTO> joinAsTypes = empDropdownService.getJoinAsTypes();
	        
	        if (joinAsTypes == null || joinAsTypes.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No active joining types found");
	        }
	        
	        return ResponseEntity.ok(joinAsTypes);
	    }
	 @GetMapping("/mode-of-hiring")
	    public ResponseEntity<?> getModeOfHiringTypes() {
	        List<GenericDropdownDTO> hiringModes = empDropdownService.getModeOfHiringTypes();
	        
	        if (hiringModes == null || hiringModes.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No active hiring modes found");
	        }
	        
	        return ResponseEntity.ok(hiringModes);
	    }
	 
	 @GetMapping("/employee-type")
	    public ResponseEntity<?> getEmployeeTypes() {
	        List<GenericDropdownDTO> employeeTypes = empDropdownService.getEmployeeTypes();
	        
	        if (employeeTypes == null || employeeTypes.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No active employee types found");
	        }
	        
	        return ResponseEntity.ok(employeeTypes);
	    }
	 @GetMapping("/countries")
	    public ResponseEntity<?> getCountries() {
	        List<GenericDropdownDTO> countries = empDropdownService.getCountries();
	        
	        if (countries == null || countries.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No countries found");
	        }
	        
	        return ResponseEntity.ok(countries);
	    }
	 @GetMapping("/employee-payment-type")
	    public ResponseEntity<?> getEmployeePaymentTypes() {
	        List<GenericDropdownDTO> paymentTypes = empDropdownService.getEmployeePaymentTypes();
	        
	        if (paymentTypes == null || paymentTypes.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No active payment types found");
	        }
	        
	        return ResponseEntity.ok(paymentTypes);
	    }
	 @GetMapping("/department/{empTypeId}")
	    public ResponseEntity<?> getDepartments(@PathVariable int empTypeId) {
	        List<GenericDropdownDTO> departments = empDropdownService.getActiveDepartmentsByEmpTypeId(empTypeId);
	        
	        if (departments == null || departments.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No active departments found for employee type ID: " + empTypeId);
	        }
	        
	        return ResponseEntity.ok(departments);
	    }
	 @GetMapping("/designation/{departmentId}")
	    public ResponseEntity<?> getDesignations(@PathVariable int departmentId) {
	        List<GenericDropdownDTO> designations = empDropdownService.getDesignations(departmentId);
	        
	        if (designations == null || designations.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No active designations found for department ID: " + departmentId);
	        }
	        
	        return ResponseEntity.ok(designations);
	    }
	 @GetMapping("/degree/{qualificationId}")
	    public ResponseEntity<?> getDegrees(@PathVariable int qualificationId ) {
	        List<GenericDropdownDTO> degrees = empDropdownService.getDegrees(qualificationId);
	        
	        if (degrees == null || degrees.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No degrees found for qualification ID: " + qualificationId);
	        }
	        
	        return ResponseEntity.ok(degrees);
	    }
	 @GetMapping("/subjects")
	 public ResponseEntity<?> getSubjects() {
	     List<GenericDropdownDTO> subjects = empDropdownService.getSubjects();
	     
	     if (subjects == null || subjects.isEmpty()) {
	         return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                 .body("No subjects found");
	     }
	     
	     return ResponseEntity.ok(subjects);
	 }
	 @GetMapping("/campusDetl/{campusId}")
	    public ResponseEntity<?> getCampusById(@PathVariable int campusId) {
	        CampusDto campus = empDropdownService.getActiveCampusById(campusId);
	        
	        if (campus == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No active campus found for campus ID: " + campusId);
	        }
	        
	        return ResponseEntity.ok(campus);
	    }
	 
	 @GetMapping("/grade")
	 public ResponseEntity<?> getGrades() {
	     List<GenericDropdownDTO> grades = empDropdownService.getActiveGrades();
	     
	     if (grades == null || grades.isEmpty()) {
	         return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                 .body("No active grades found");
	     }
	     
	     return ResponseEntity.ok(grades);
	 }
	 
	  @GetMapping("/structures")
	    public ResponseEntity<?> getActiveStructures() {
	        List<GenericDropdownDTO> structures = empDropdownService.getActiveStructures();
	        
	        if (structures == null || structures.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No active structures found");
	        }
	        
	        return ResponseEntity.ok(structures);
	    }
	  @GetMapping("/costcenters")
	    public ResponseEntity<?> getActiveCostCenters() {
	        List<GenericDropdownDTO> costCenters = empDropdownService.getCostCenters();
	        
	        if (costCenters == null || costCenters.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No active cost centers found");
	        }
	        
	        return ResponseEntity.ok(costCenters);
	    }
	  @GetMapping("/organizations/active")
	    public ResponseEntity<?> getActiveOrganizations() {
	        List<OrganizationDTO> organizations = empDropdownService.getAllActiveOrganizations();
	        
	        if (organizations == null || organizations.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No active organizations found");
	        }
	        
	        return ResponseEntity.ok(organizations);
	    }
	  
	 
	  @GetMapping("/{campusId}/organizations")
	    public ResponseEntity<?> getOrganizations(@PathVariable int campusId) {
	        List<GenericDropdownDTO> organizations = empDropdownService.getOrganizationsByCampusId(campusId);
	        
	        if (organizations == null || organizations.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No organizations found for campus ID: " + campusId);
	        }
	        
	        return ResponseEntity.ok(organizations);
	    }
	  
	  @GetMapping("/{campusId}/building")
	    public ResponseEntity<?> getBuildingsByCampusId(@PathVariable int campusId) {
	        List<GenericDropdownDTO> buildings = empDropdownService.getBuildingsByCampusId(campusId);
	        
	        if (buildings == null || buildings.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No buildings found for campus ID: " + campusId);
	        }
	        
	        return ResponseEntity.ok(buildings);
	    }
	    @GetMapping("/streams")
	    public ResponseEntity<?> getAllStreams() {
	        List<GenericDropdownDTO> streams = empDropdownService.getAllActiveStreams();
	        
	        if (streams == null || streams.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No active streams found");
	        }
	        
	        return ResponseEntity.ok(streams);
	    }
	    
	    @GetMapping("/employeeLevels")
	    public ResponseEntity<?> getEmployeeLevels() {
	        List<GenericDropdownDTO> employeeLevels = empDropdownService.getAllActiveEmpLevels();
	        
	        if (employeeLevels == null || employeeLevels.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No active employee levels found");
	        }
	        
	        return ResponseEntity.ok(employeeLevels);
	    }
	    
    @GetMapping("/active/Banks")
    public ResponseEntity<?> getAllActiveBanks() {
        List<GenericDropdownDTO> banks = empDropdownService.getAllActiveBanks();
        
        if (banks == null || banks.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No active banks found");
        }
        
        return ResponseEntity.ok(banks);
    }
    
    @GetMapping("/active/BankBranches")
    public ResponseEntity<?> getAllActiveBankBranches() {
        List<GenericDropdownDTO> bankBranches = empDropdownService.getAllActiveBankBranches();
        
        if (bankBranches == null || bankBranches.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No active bank branches found");
        }
        
        return ResponseEntity.ok(bankBranches);
    }
    
    @GetMapping("/active/BankBranches/bank/{bankId}")
    public ResponseEntity<?> getActiveBankBranchesByBankId(@PathVariable Integer bankId) {
        List<GenericDropdownDTO> bankBranches = empDropdownService.getActiveBankBranchesByBankId(bankId);
        
        if (bankBranches == null || bankBranches.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No active bank branches found for bank ID: " + bankId);
        }
        
        return ResponseEntity.ok(bankBranches);
    }
    
    @GetMapping("/active/ChecklistDetails")
    public ResponseEntity<?> getActiveChecklistDetails() {
        List<GenericDropdownDTO> checklistDetails = empDropdownService.getActiveChecklistDetails();
        
        if (checklistDetails == null || checklistDetails.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No active checklist details found");
        }
        
        return ResponseEntity.ok(checklistDetails);
    }
    
    @GetMapping("/active/DocumentTypes")
    public ResponseEntity<?> getActiveDocumentTypes() {
        List<GenericDropdownDTO> documentTypes = empDropdownService.getActiveDocumentTypes();
        
        if (documentTypes == null || documentTypes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No active document types found");
        }
        
        return ResponseEntity.ok(documentTypes);
    }
	    @GetMapping("/employees/active")
	    public ResponseEntity<?> getActiveEmployees() {
	        List<GenericDropdownDTO> employees = empDropdownService.getActiveEmployees();
	        
	        if (employees == null || employees.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No active employees found");
	        }
	        
	        return ResponseEntity.ok(employees);
	    }

	    @GetMapping("/employees/inactive")
	    public ResponseEntity<?> getInactiveEmployees() {
	        List<GenericDropdownDTO> employees = empDropdownService.getInactiveEmployees();
	        
	        if (employees == null || employees.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No inactive employees found");
	        }
	        
	        return ResponseEntity.ok(employees);
	    }
	    
	    @GetMapping("/campsEmployess/{campusId}")
	    public ResponseEntity<?> getContactsByCampus(@PathVariable Integer campusId) {
	        List<CampusContactDTO> contacts = empDropdownService.getActiveContactsByCampusId(campusId);
	        
	        if (contacts == null || contacts.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("No active contacts found for campus ID: " + campusId);
	        }
	        
	        return ResponseEntity.ok(contacts);
	    }
	    
	    
	    @GetMapping("/organizations")
		public ResponseEntity<?> getAllOrganizations() {
			List<GenericDropdownDTO> organizations = empDropdownService.getAllOrganizations();
			
			if (organizations == null || organizations.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body("No organizations found");
			}
			
			return ResponseEntity.ok(organizations);
		}
		
		@GetMapping("/all_employees")
		public ResponseEntity<?> getAllEmployees() {
			List<GenericDropdownDTO> employees = empDropdownService.getAllEmployees();
			
			if (employees == null || employees.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body("No active employees found");
			}
			
			return ResponseEntity.ok(employees);
		}
		
		@GetMapping("/categories/active")
		public ResponseEntity<?> getActiveCategories() {
			List<GenericDropdownDTO> categories = empDropdownService.getActiveCategories();
			
			if (categories == null || categories.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body("No active categories found");
			}
			
			return ResponseEntity.ok(categories);
		}
		
		@GetMapping("/campuses/active")
		public ResponseEntity<?> getActiveCampuses() {
			List<GenericDropdownDTO> campuses = empDropdownService.getActiveCampuses();
			
			if (campuses == null || campuses.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body("No active campuses found");
			}
			
			return ResponseEntity.ok(campuses);
		}
		
		@GetMapping("/employees/campus/{campusId}")
		public ResponseEntity<?> getActiveEmployeesByCampusId(@PathVariable Integer campusId) {
			List<GenericDropdownDTO> employees = empDropdownService.getActiveEmployeesByCampusId(campusId);
			
			if (employees == null || employees.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body("No active employees found for campus ID: " + campusId);
			}
			
			return ResponseEntity.ok(employees);
		}
		
	@GetMapping("/cities/district/{districtId}")
	public ResponseEntity<?> getCitiesByDistrictId(@PathVariable Integer districtId) {
		List<GenericDropdownDTO> cities = empDropdownService.getCitiesByDistrictId(districtId);
		
		if (cities == null || cities.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("No cities found for district ID: " + districtId);
		}
		
		return ResponseEntity.ok(cities);
	}
	
	@GetMapping("/occupations/active")
	public ResponseEntity<?> getActiveOccupations() {
		List<GenericDropdownDTO> occupations = empDropdownService.getActiveOccupations();
		
		if (occupations == null || occupations.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("No active occupations found");
		}
		
		return ResponseEntity.ok(occupations);
	}
	
	@GetMapping("/orientations/active")
	public ResponseEntity<?> getActiveOrientations() {
		List<GenericDropdownDTO> orientations = empDropdownService.getActiveOrientations();
		
		if (orientations == null || orientations.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("No active orientations found");
		}
		
		return ResponseEntity.ok(orientations);
	}
}