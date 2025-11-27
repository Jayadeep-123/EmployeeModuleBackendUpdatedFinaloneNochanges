package com.employee.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employee.entity.Campus;
import com.employee.entity.Department;
import com.employee.entity.Designation;
import com.employee.entity.Employee;
import com.employee.entity.SharedEmployee;
import com.employee.entity.Subject;
import com.employee.exception.ResourceNotFoundException;
import com.employee.dto.ManagerMappingDTO;
import com.employee.dto.BulkManagerMappingDTO;
import com.employee.dto.UnmappingDTO;
import com.employee.dto.BulkUnmappingDTO;
import com.employee.dto.CampusMappingDTO;
import com.employee.repository.CampusRepository;
import com.employee.repository.CityRepository;
import com.employee.repository.DepartmentRepository;
import com.employee.repository.DesignationRepository;
import com.employee.repository.EmployeeRepository;
import com.employee.repository.SharedEmployeeRepository;
import com.employee.repository.SubjectRepository;

/**
 * Service for Manager Mapping functionality.
 * Handles the hierarchy: City → Campus → Department → Designation → Employees
 * and updates work starting date for selected employees.
 */
@Service
public class ManagerMappingService {
    
    @Autowired
    private CityRepository cityRepository;
    
    @Autowired
    private CampusRepository campusRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private DesignationRepository designationRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private SharedEmployeeRepository sharedEmployeeRepository;
    
    @Autowired
    private SubjectRepository subjectRepository;
    
    /**
     * Maps employee based on payrollId and updates their details.
     * 
     * Flow:
     * 1. Validate City
     * 2. Validate Campus (must belong to City)
     * 3. Validate Department exists and is active (master table - independent)
     * 4. Validate Designation exists and is active
     * 5. Find Employee by payrollId
     * 6. Validate Employee is active
     * 7. Validate Manager (managerId) exists and is active (if provided)
     * 8. Validate Reporting Manager (reportingManagerId) exists and is active (if provided)
     * 9. Update employee: campus, department, designation, manager, reporting manager, work starting date, remarks
     * 
     * @param mappingDTO The manager mapping request DTO
     * @return The same ManagerMappingDTO that was passed in
     */
    @Transactional
    public ManagerMappingDTO mapEmployeesAndUpdateWorkDate(ManagerMappingDTO mappingDTO) {
        // Validate required fields
        validateMappingDTO(mappingDTO);
        
        // Step 1: Validate City exists
        cityRepository.findById(mappingDTO.getCityId())
            .orElseThrow(() -> new ResourceNotFoundException("City not found with ID: " + mappingDTO.getCityId()));
        
        // Step 2: Check if using multiple campuses or single campus
        // Always use campusMappings array - required field
        List<CampusMappingDTO> campusMappingsList = mappingDTO.getCampusMappings();
        if (campusMappingsList == null || campusMappingsList.isEmpty()) {
            throw new IllegalArgumentException("campusMappings array is required and cannot be empty");
        }
        
        boolean useMultipleCampuses = campusMappingsList.size() > 1;
        
        // Step 4: Find Employee by payrollId
        Employee employee = findEmployeeByPayrollId(mappingDTO.getPayrollId());
        
        // Step 5: Validate Employee is active
        if (employee.getIs_active() != 1) {
            throw new ResourceNotFoundException("Employee with payrollId " + mappingDTO.getPayrollId() + " is not active");
        }
        
        // Step 6: Get employee's existing campus (do not change it)
        Campus employeeCampus = employee.getCampus_id();
        if (employeeCampus == null) {
            throw new ResourceNotFoundException("Employee with payrollId " + mappingDTO.getPayrollId() + " does not have a campus assigned. Cannot perform mapping.");
        }
        
        // Step 7: Handle single campus (no SharedEmployee table) or multiple campuses (use SharedEmployee table)
        Designation designation;
        
        // Step 3: Process campus mappings and validate
        Department department = null;
        if (useMultipleCampuses) {
            // Multiple campuses: Validate that employee's primary campus is included in the campusMappings
            boolean primaryCampusFound = false;
            for (CampusMappingDTO campusMapping : campusMappingsList) {
                if (campusMapping.getCampusId().equals(employeeCampus.getCampusId())) {
                    primaryCampusFound = true;
                    break;
                }
            }
            if (!primaryCampusFound) {
                throw new IllegalArgumentException(
                    String.format("Employee's primary campus (ID: %d) must be included in campusMappings. " +
                                "Cannot map employee to different campuses without including their primary campus.", 
                                employeeCampus.getCampusId()));
            }
            
            // Multiple campuses: Process each campus mapping and store in SharedEmployee table
            for (CampusMappingDTO campusMapping : campusMappingsList) {
                // Validate Department exists and is active
                Department campusDepartment = departmentRepository.findByIdAndIsActive(campusMapping.getDepartmentId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Department not found with ID: " + campusMapping.getDepartmentId()));
                
                // Validate Campus is active and exists in the City
                Campus campus = campusRepository.findByCampusIdAndIsActive(campusMapping.getCampusId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Campus not found with ID: " + campusMapping.getCampusId()));
                
                if (campus.getCity() == null || campus.getCity().getCityId() != mappingDTO.getCityId()) {
                    throw new ResourceNotFoundException(
                        String.format("Campus with ID %d is not assigned to City with ID %d", 
                                    campusMapping.getCampusId(), mappingDTO.getCityId()));
                }
                
                // Validate Designation exists, is active, and belongs to the Department
                Designation campusDesignation = designationRepository.findByIdAndIsActive(campusMapping.getDesignationId(), 1)
                    .orElseThrow(() -> new ResourceNotFoundException("Active Designation not found with ID: " + campusMapping.getDesignationId()));
                
                if (campusDesignation.getDepartment() == null || campusDesignation.getDepartment().getDepartment_id() != campusMapping.getDepartmentId()) {
                    throw new ResourceNotFoundException(
                        String.format("Designation with ID %d does not belong to Department with ID %d", 
                                    campusMapping.getDesignationId(), campusMapping.getDepartmentId()));
                }
                
                // Create or update SharedEmployee record for multiple campuses
                saveOrUpdateSharedEmployee(employee, campus, campusDesignation, campusMapping.getSubjectId(), mappingDTO.getUpdatedBy());
                
                // Use first department for employee table (primary department)
                if (department == null) {
                    department = campusDepartment;
                }
            }
            // Use first designation for employee table (primary designation)
            designation = designationRepository.findByIdAndIsActive(campusMappingsList.get(0).getDesignationId(), 1)
                .orElseThrow(() -> new ResourceNotFoundException("Active Designation not found with ID: " + campusMappingsList.get(0).getDesignationId()));
        } else {
            // Single campus: Use first (and only) item from campusMappings (no SharedEmployee table)
            CampusMappingDTO singleMapping = campusMappingsList.get(0);
            
            // Validate that requested campus matches employee's existing campus
            if (!singleMapping.getCampusId().equals(employeeCampus.getCampusId())) {
                throw new IllegalArgumentException(
                    String.format("Requested campus ID (%d) does not match employee's existing campus ID (%d) in Employee table. " +
                                "Cannot change employee's primary campus through mapping. Employee's campus must remain: %d", 
                                singleMapping.getCampusId(), employeeCampus.getCampusId(), employeeCampus.getCampusId()));
            }
            
            // Validate Department exists and is active
            department = departmentRepository.findByIdAndIsActive(singleMapping.getDepartmentId(), 1)
                .orElseThrow(() -> new ResourceNotFoundException("Active Department not found with ID: " + singleMapping.getDepartmentId()));
            
            // Validate Campus is active and exists in the City
            Campus campus = campusRepository.findByCampusIdAndIsActive(singleMapping.getCampusId(), 1)
                .orElseThrow(() -> new ResourceNotFoundException("Active Campus not found with ID: " + singleMapping.getCampusId()));
            
            if (campus.getCity() == null || campus.getCity().getCityId() != mappingDTO.getCityId()) {
                throw new ResourceNotFoundException(
                    String.format("Campus with ID %d is not assigned to City with ID %d", 
                                singleMapping.getCampusId(), mappingDTO.getCityId()));
            }
            
            // Validate Designation exists, is active, and belongs to the Department
            designation = designationRepository.findByIdAndIsActive(singleMapping.getDesignationId(), 1)
                .orElseThrow(() -> new ResourceNotFoundException("Active Designation not found with ID: " + singleMapping.getDesignationId()));
            
            if (designation.getDepartment() == null || designation.getDepartment().getDepartment_id() != singleMapping.getDepartmentId()) {
                throw new ResourceNotFoundException(
                    String.format("Designation with ID %d does not belong to Department with ID %d", 
                                singleMapping.getDesignationId(), singleMapping.getDepartmentId()));
            }
            // No SharedEmployee table update for single campus
        }
        
        // Step 8: Assign Manager - only if provided
        // Treat 0 as null
        Employee manager = null;
        Integer managerIdValue = mappingDTO.getManagerId();
        if (managerIdValue != null && managerIdValue != 0) {
            // Manager ID provided - validate it
            final Integer managerId = managerIdValue;
            manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with ID: " + managerId));
            
            if (manager.getIs_active() != 1) {
                throw new ResourceNotFoundException("Manager with ID " + managerId + " is not active");
            }
            
            // Validate manager is from the same campus as employee (primary campus)
            if (manager.getCampus_id() == null || manager.getCampus_id().getCampusId() != employeeCampus.getCampusId()) {
                throw new ResourceNotFoundException(
                    String.format("Manager with ID %d is not from the same campus as employee. Employee campus: %d, Manager campus: %s", 
                                managerId, employeeCampus.getCampusId(), 
                                manager.getCampus_id() != null ? String.valueOf(manager.getCampus_id().getCampusId()) : "null"));
            }
        }
        
        // Step 9: Assign Reporting Manager - only if provided
        // Treat 0 as null
        Employee reportingManager = null;
        Integer reportingManagerIdValue = mappingDTO.getReportingManagerId();
        if (reportingManagerIdValue != null && reportingManagerIdValue != 0) {
            // Reporting Manager ID provided - validate it
            final Integer reportingManagerId = reportingManagerIdValue;
            reportingManager = employeeRepository.findById(reportingManagerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporting Manager not found with ID: " + reportingManagerId));
            
            if (reportingManager.getIs_active() != 1) {
                throw new ResourceNotFoundException("Reporting Manager with ID " + reportingManagerId + " is not active");
            }
            
            // Validate reporting manager is from the same campus as employee (primary campus)
            if (reportingManager.getCampus_id() == null || reportingManager.getCampus_id().getCampusId() != employeeCampus.getCampusId()) {
                throw new ResourceNotFoundException(
                    String.format("Reporting Manager with ID %d is not from the same campus as employee. Employee campus: %d, Reporting Manager campus: %s", 
                                reportingManagerId, employeeCampus.getCampusId(), 
                                reportingManager.getCampus_id() != null ? String.valueOf(reportingManager.getCampus_id().getCampusId()) : "null"));
            }
        }
        
        // Step 8: Update employee fields (DO NOT change campus - keep existing campus)
        employee.setDepartment(department);
        employee.setDesignation(designation);
        
        // Update manager_id if provided
        if (manager != null) {
            employee.setEmployee_manager_id(manager);
        }
        
        // Update reporting_manager_id if provided
        if (reportingManager != null) {
            employee.setEmployee_reporting_id(reportingManager);
        }
        // Update contract_start_date with workStartingDate
        employee.setContract_start_date(mappingDTO.getWorkStartingDate());
        employee.setUpdated_by(mappingDTO.getUpdatedBy() != null ? mappingDTO.getUpdatedBy() : 1);
        employee.setUpdated_date(new Timestamp(System.currentTimeMillis()));
        
        // Update remarks: if value exists and is not empty, set it; otherwise set to null
        // Treat null, empty string, whitespace-only, or string "null" as null
        String remarkValue = mappingDTO.getRemark();
        if (remarkValue != null && !remarkValue.trim().isEmpty() && !remarkValue.trim().equalsIgnoreCase("null")) {
            employee.setRemarks(remarkValue.trim());
        } else {
            employee.setRemarks(null);
        }
        
        employeeRepository.save(employee);
        
        return mappingDTO; // Return the request DTO
    }
    
    /**
     * Maps multiple employees based on payrollIds and updates their details with the same values.
     * 
     * Flow:
     * 1. Validate City
     * 2. Validate Campus (must belong to City)
     * 3. Validate Department exists and is active (master table - independent)
     * 4. Validate Designation exists and is active
     * 5. Validate Manager (managerId) exists and is active (if provided)
     * 6. Validate Reporting Manager (reportingManagerId) exists and is active (if provided)
     * 7. For each payrollId:
     *    - Find Employee by payrollId
     *    - Validate Employee is active
     *    - Update employee: campus, department, designation, manager, reporting manager, work starting date, remarks
     * 
     * @param bulkMappingDTO The bulk manager mapping request DTO
     * @return The same BulkManagerMappingDTO that was passed in
     */
    @Transactional
    public BulkManagerMappingDTO mapMultipleEmployeesAndUpdateWorkDate(BulkManagerMappingDTO bulkMappingDTO) {
        // Validate required fields
        validateBulkMappingDTO(bulkMappingDTO);
        
        // Step 1: Validate City exists
        cityRepository.findById(bulkMappingDTO.getCityId())
            .orElseThrow(() -> new ResourceNotFoundException("City not found with ID: " + bulkMappingDTO.getCityId()));
        
        // Step 2: Always use campusMappings array - required field
        List<CampusMappingDTO> campusMappings = bulkMappingDTO.getCampusMappings();
        if (campusMappings == null || campusMappings.isEmpty()) {
            throw new IllegalArgumentException("campusMappings array is required and cannot be empty");
        }
        
        // Step 3: Pre-validate all campuses, departments, and designations
        Department department = null;
        for (CampusMappingDTO campusMapping : campusMappings) {
            // Validate Department exists and is active
            Department campusDepartment = departmentRepository.findByIdAndIsActive(campusMapping.getDepartmentId(), 1)
                .orElseThrow(() -> new ResourceNotFoundException("Active Department not found with ID: " + campusMapping.getDepartmentId()));
            
            // Validate Campus is active and exists in the City
            Campus campus = campusRepository.findByCampusIdAndIsActive(campusMapping.getCampusId(), 1)
                .orElseThrow(() -> new ResourceNotFoundException("Active Campus not found with ID: " + campusMapping.getCampusId()));
            
            if (campus.getCity() == null || campus.getCity().getCityId() != bulkMappingDTO.getCityId()) {
                throw new ResourceNotFoundException(
                    String.format("Campus with ID %d is not assigned to City with ID %d", 
                                campusMapping.getCampusId(), bulkMappingDTO.getCityId()));
            }
            
            // Validate Designation exists, is active, and belongs to the Department
            Designation designation = designationRepository.findByIdAndIsActive(campusMapping.getDesignationId(), 1)
                .orElseThrow(() -> new ResourceNotFoundException("Active Designation not found with ID: " + campusMapping.getDesignationId()));
            
            if (designation.getDepartment() == null || designation.getDepartment().getDepartment_id() != campusMapping.getDepartmentId()) {
                throw new ResourceNotFoundException(
                    String.format("Designation with ID %d does not belong to Department with ID %d", 
                                campusMapping.getDesignationId(), campusMapping.getDepartmentId()));
            }
            
            // Use first department for employee table (primary department)
            if (department == null) {
                department = campusDepartment;
            }
        }
        
        // Step 5: Assign Manager - only if provided (will validate campus match per employee)
        // Treat 0 as null
        Employee manager = null;
        Integer managerIdValue = bulkMappingDTO.getManagerId();
        if (managerIdValue != null && managerIdValue != 0) {
            // Manager ID provided - validate it
            final Integer managerId = managerIdValue; // Make it final for lambda
            manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with ID: " + managerId));
            
            if (manager.getIs_active() != 1) {
                throw new ResourceNotFoundException("Manager with ID " + managerId + " is not active");
            }
        }
        // If managerId is not provided (null or 0), manager remains null - no auto-assignment
        
        // Step 6: Assign Reporting Manager - only if provided (will validate campus match per employee)
        // Treat 0 as null
        Employee reportingManager = null;
        Integer reportingManagerIdValue = bulkMappingDTO.getReportingManagerId();
        if (reportingManagerIdValue != null && reportingManagerIdValue != 0) {
            // Reporting Manager ID provided - validate it
            final Integer reportingManagerId = reportingManagerIdValue; // Make it final for lambda
            reportingManager = employeeRepository.findById(reportingManagerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporting Manager not found with ID: " + reportingManagerId));
            
            if (reportingManager.getIs_active() != 1) {
                throw new ResourceNotFoundException("Reporting Manager with ID " + reportingManagerId + " is not active");
            }
        }
        // If reportingManagerId is not provided (null or 0), reportingManager remains null - no auto-assignment
        
        // Step 7: Process each payrollId
        List<String> processedPayrollIds = new ArrayList<>();
        List<String> failedPayrollIds = new ArrayList<>();
        
        for (String payrollId : bulkMappingDTO.getPayrollIds()) {
            try {
                // Find Employee by payrollId
                Employee employee = findEmployeeByPayrollId(payrollId);
                
                // Validate Employee is active
                if (employee.getIs_active() != 1) {
                    failedPayrollIds.add(payrollId + " (not active)");
                    continue;
                }
                
                // Get employee's existing campus (do not change it)
                Campus employeeCampus = employee.getCampus_id();
                if (employeeCampus == null) {
                    failedPayrollIds.add(payrollId + " (no campus assigned)");
                    continue;
                }
                
                // For bulk operations, make sure manager/reporting manager is not the current employee
                Employee employeeManager = manager;
                Employee employeeReportingManager = reportingManager;
                
                if (employeeManager != null && employeeManager.getEmp_id() == employee.getEmp_id()) {
                    employeeManager = null;
                }
                
                if (employeeReportingManager != null && employeeReportingManager.getEmp_id() == employee.getEmp_id()) {
                    employeeReportingManager = null;
                }
                
                // Validate manager is from the same campus as employee (if provided)
                if (employeeManager != null) {
                    if (employeeManager.getCampus_id() == null || employeeManager.getCampus_id().getCampusId() != employeeCampus.getCampusId()) {
                        failedPayrollIds.add(payrollId + " (manager not from same campus: employee campus=" + 
                                            employeeCampus.getCampusId() + ", manager campus=" + 
                                            (employeeManager.getCampus_id() != null ? employeeManager.getCampus_id().getCampusId() : "null") + ")");
                        continue;
                    }
                }
                
                // Validate reporting manager is from the same campus as employee (if provided)
                if (employeeReportingManager != null) {
                    if (employeeReportingManager.getCampus_id() == null || employeeReportingManager.getCampus_id().getCampusId() != employeeCampus.getCampusId()) {
                        failedPayrollIds.add(payrollId + " (reporting manager not from same campus: employee campus=" + 
                                            employeeCampus.getCampusId() + ", reporting manager campus=" + 
                                            (employeeReportingManager.getCampus_id() != null ? employeeReportingManager.getCampus_id().getCampusId() : "null") + ")");
                        continue;
                    }
                }
                
                // Process each campus mapping for this employee
                // Only store in SharedEmployee table if multiple campuses are selected
                boolean isMultipleCampuses = campusMappings.size() > 1;
                
                // Validate campus matching for bulk operations
                if (isMultipleCampuses) {
                    // Multiple campuses: Validate that employee's primary campus is included in the campusMappings
                    boolean primaryCampusFound = false;
                    for (CampusMappingDTO campusMapping : campusMappings) {
                        if (campusMapping.getCampusId().equals(employeeCampus.getCampusId())) {
                            primaryCampusFound = true;
                            break;
                        }
                    }
                    if (!primaryCampusFound) {
                        failedPayrollIds.add(payrollId + " (employee's primary campus ID: " + employeeCampus.getCampusId() + 
                                            " must be included in campusMappings)");
                        continue;
                    }
                } else {
                    // Single campus: Validate that requested campus matches employee's existing campus
                    CampusMappingDTO singleMapping = campusMappings.get(0);
                    if (!singleMapping.getCampusId().equals(employeeCampus.getCampusId())) {
                        failedPayrollIds.add(payrollId + " (requested campus ID: " + singleMapping.getCampusId() + 
                                            " does not match employee's existing campus ID: " + employeeCampus.getCampusId() + ")");
                        continue;
                    }
                }
                
                for (CampusMappingDTO campusMapping : campusMappings) {
                    Campus campus = campusRepository.findByCampusIdAndIsActive(campusMapping.getCampusId(), 1)
                        .orElseThrow(() -> new ResourceNotFoundException("Active Campus not found with ID: " + campusMapping.getCampusId()));
                    
                    Designation designation = designationRepository.findByIdAndIsActive(campusMapping.getDesignationId(), 1)
                        .orElseThrow(() -> new ResourceNotFoundException("Active Designation not found with ID: " + campusMapping.getDesignationId()));
                    
                    // Validate designation belongs to department (already validated in pre-validation step, but double-check)
                    if (designation.getDepartment() == null || designation.getDepartment().getDepartment_id() != campusMapping.getDepartmentId()) {
                        throw new ResourceNotFoundException(
                            String.format("Designation with ID %d does not belong to Department with ID %d", 
                                        campusMapping.getDesignationId(), campusMapping.getDepartmentId()));
                    }
                    
                    // Only create or update SharedEmployee record if multiple campuses are selected
                    if (isMultipleCampuses) {
                        saveOrUpdateSharedEmployee(employee, campus, designation, campusMapping.getSubjectId(), bulkMappingDTO.getUpdatedBy());
                    }
                }
                
                // Update employee fields (DO NOT change campus - keep existing campus)
                // Use first department from campusMappings (primary department)
                if (!campusMappings.isEmpty()) {
                    Department primaryDepartment = departmentRepository.findByIdAndIsActive(campusMappings.get(0).getDepartmentId(), 1)
                        .orElseThrow(() -> new ResourceNotFoundException("Active Department not found with ID: " + campusMappings.get(0).getDepartmentId()));
                    employee.setDepartment(primaryDepartment);
                }
                // Set primary designation from first campus mapping (for backward compatibility)
                if (!campusMappings.isEmpty()) {
                    Designation primaryDesignation = designationRepository.findByIdAndIsActive(campusMappings.get(0).getDesignationId(), 1)
                        .orElseThrow(() -> new ResourceNotFoundException("Active Designation not found with ID: " + campusMappings.get(0).getDesignationId()));
                    employee.setDesignation(primaryDesignation);
                }
                
                // Update manager_id if provided
                if (employeeManager != null) {
                    employee.setEmployee_manager_id(employeeManager);
                }
                
                // Update reporting_manager_id if provided
                if (employeeReportingManager != null) {
                    employee.setEmployee_reporting_id(employeeReportingManager);
                }
                // Update contract_start_date with workStartingDate
                employee.setContract_start_date(bulkMappingDTO.getWorkStartingDate());
                employee.setUpdated_by(bulkMappingDTO.getUpdatedBy() != null ? bulkMappingDTO.getUpdatedBy() : 1);
                employee.setUpdated_date(new Timestamp(System.currentTimeMillis()));
                
                // Update remarks: if value exists and is not empty, set it; otherwise set to null
                // Treat null, empty string, whitespace-only, or string "null" as null
                String remarkValue = bulkMappingDTO.getRemark();
                if (remarkValue != null && !remarkValue.trim().isEmpty() && !remarkValue.trim().equalsIgnoreCase("null")) {
                    employee.setRemarks(remarkValue.trim());
                } else {
                    employee.setRemarks(null);
                }
                
                employeeRepository.save(employee);
                processedPayrollIds.add(payrollId);
                
            } catch (ResourceNotFoundException e) {
                failedPayrollIds.add(payrollId + " (not found)");
            } catch (Exception e) {
                failedPayrollIds.add(payrollId + " (" + e.getMessage() + ")");
            }
        }
        
        return bulkMappingDTO; // Return the request DTO
    }
    
    /**
     * Finds employee by payrollId (checks both tempPayrollId and payRollId).
     * 
     * @param payrollId Payroll ID (can be tempPayrollId or payRollId)
     * @return Employee found by payrollId
     * @throws ResourceNotFoundException if employee not found
     */
    private Employee findEmployeeByPayrollId(String payrollId) {
        if (payrollId == null || payrollId.trim().isEmpty()) {
            throw new IllegalArgumentException("payrollId is required");
        }
        
        // Try to find by tempPayrollId first
        Optional<Employee> employeeByTemp = employeeRepository.findByTempPayrollId(payrollId);
        if (employeeByTemp.isPresent()) {
            return employeeByTemp.get();
        }
        
        // Try to find by payRollId
        Optional<Employee> employeeByPayroll = employeeRepository.findByPayRollId(payrollId);
        if (employeeByPayroll.isPresent()) {
            return employeeByPayroll.get();
        }
        
        throw new ResourceNotFoundException("Employee not found with payrollId: " + payrollId);
    }
    
    /**
     * Creates or updates a SharedEmployee record for an employee working in multiple campuses.
     * 
     * @param employee The employee
     * @param campus The campus
     * @param designation The designation
     * @param subjectId Optional subject ID (can be null)
     * @param updatedBy User ID performing the update
     * @return The created or updated SharedEmployee
     */
    private SharedEmployee saveOrUpdateSharedEmployee(Employee employee, Campus campus, Designation designation, Integer subjectId, Integer updatedBy) {
        // Check if SharedEmployee record already exists for this employee and campus
        Optional<SharedEmployee> existing = sharedEmployeeRepository.findByEmpIdAndCampusId(
                employee.getEmp_id(), campus.getCampusId());
        
        SharedEmployee sharedEmployee;
        if (existing.isPresent()) {
            // Update existing record
            sharedEmployee = existing.get();
            sharedEmployee.setIsActive(1); // Reactivate if it was deactivated
        } else {
            // Create new record
            sharedEmployee = new SharedEmployee();
            sharedEmployee.setEmpId(employee);
            sharedEmployee.setCmpsId(campus);
            sharedEmployee.setCreatedBy(updatedBy != null ? updatedBy : 1);
            sharedEmployee.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        }
        
        // Update designation
        sharedEmployee.setDesignationId(designation);
        
        // Update subject if provided
        if (subjectId != null && subjectId > 0) {
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + subjectId));
            sharedEmployee.setSubjectId(subject);
        } else {
            sharedEmployee.setSubjectId(null);
        }
        
        // Update audit fields
        sharedEmployee.setUpdatedBy(updatedBy != null ? updatedBy : 1);
        sharedEmployee.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
        
        return sharedEmployeeRepository.save(sharedEmployee);
    }
    
    /**
     * Validates the ManagerMappingDTO for required fields.
     * 
     * @param mappingDTO The DTO to validate
     * @throws IllegalArgumentException if any required field is null or invalid
     */
    private void validateMappingDTO(ManagerMappingDTO mappingDTO) {
        if (mappingDTO == null) {
            throw new IllegalArgumentException("ManagerMappingDTO cannot be null");
        }
        if (mappingDTO.getCityId() == null) {
            throw new IllegalArgumentException("cityId is required");
        }
        if (mappingDTO.getPayrollId() == null || mappingDTO.getPayrollId().trim().isEmpty()) {
            throw new IllegalArgumentException("payrollId is required");
        }
        if (mappingDTO.getWorkStartingDate() == null) {
            throw new IllegalArgumentException("workStartingDate is required");
        }
        
        // Validate campusMappings array - always required
        List<CampusMappingDTO> campusMappingsForValidation = mappingDTO.getCampusMappings();
        if (campusMappingsForValidation == null || campusMappingsForValidation.isEmpty()) {
            throw new IllegalArgumentException("campusMappings array is required and cannot be empty");
        }
        
        // Validate each campus mapping
        for (CampusMappingDTO campusMapping : campusMappingsForValidation) {
            if (campusMapping.getCampusId() == null || campusMapping.getCampusId() <= 0) {
                throw new IllegalArgumentException("Valid campusId (greater than 0) is required in each campusMapping object");
            }
            if (campusMapping.getDepartmentId() == null || campusMapping.getDepartmentId() <= 0) {
                throw new IllegalArgumentException("Valid departmentId (greater than 0) is required in each campusMapping object");
            }
            if (campusMapping.getDesignationId() == null || campusMapping.getDesignationId() <= 0) {
                throw new IllegalArgumentException("Valid designationId (greater than 0) is required in each campusMapping object");
            }
        }
    }
    
    /**
     * Validates the BulkManagerMappingDTO for required fields.
     * 
     * @param bulkMappingDTO The DTO to validate
     * @throws IllegalArgumentException if any required field is null or invalid
     */
    private void validateBulkMappingDTO(BulkManagerMappingDTO bulkMappingDTO) {
        if (bulkMappingDTO == null) {
            throw new IllegalArgumentException("BulkManagerMappingDTO cannot be null");
        }
        if (bulkMappingDTO.getCityId() == null) {
            throw new IllegalArgumentException("cityId is required");
        }
        
        // Validate campusMappings array - always required
        List<CampusMappingDTO> campusMappingsForValidation = bulkMappingDTO.getCampusMappings();
        if (campusMappingsForValidation == null || campusMappingsForValidation.isEmpty()) {
            throw new IllegalArgumentException("campusMappings array is required and cannot be empty");
        }
        
        // Validate each campus mapping
        for (CampusMappingDTO campusMapping : campusMappingsForValidation) {
            if (campusMapping.getCampusId() == null || campusMapping.getCampusId() <= 0) {
                throw new IllegalArgumentException("Valid campusId (greater than 0) is required in each campusMapping object");
            }
            if (campusMapping.getDepartmentId() == null || campusMapping.getDepartmentId() <= 0) {
                throw new IllegalArgumentException("Valid departmentId (greater than 0) is required in each campusMapping object");
            }
            if (campusMapping.getDesignationId() == null || campusMapping.getDesignationId() <= 0) {
                throw new IllegalArgumentException("Valid designationId (greater than 0) is required in each campusMapping object");
            }
        }
        if (bulkMappingDTO.getPayrollIds() == null || bulkMappingDTO.getPayrollIds().isEmpty()) {
            throw new IllegalArgumentException("payrollIds list is required and cannot be empty");
        }
        // Validate that all payrollIds in the list are not null or empty
        for (String payrollId : bulkMappingDTO.getPayrollIds()) {
            if (payrollId == null || payrollId.trim().isEmpty()) {
                throw new IllegalArgumentException("payrollIds list cannot contain null or empty values");
            }
        }
        if (bulkMappingDTO.getWorkStartingDate() == null) {
            throw new IllegalArgumentException("workStartingDate is required");
        }
    }
    
    /**
     * Unmaps manager and/or reporting manager from a single employee.
     * Does not require department or designation - but requires city, campus, and last date of working.
     * 
     * Flow:
     * 1. Validate City exists
     * 2. Validate Campus (must belong to City)
     * 3. Find Employee by payrollId
     * 4. Validate Employee is active
     * 5. Update campus
     * 6. Set manager_id to null if provided managerId matches current manager
     * 7. Set reporting_manager_id to null if provided reportingManagerId matches current reporting manager
     * 8. Update last date of working (contract end date)
     * 9. Update remarks (set to null if empty)
     * 10. Update employee
     * 
     * @param unmappingDTO The unmapping request DTO
     * @return The same UnmappingDTO that was passed in
     */
    @Transactional
    public UnmappingDTO unmapEmployee(UnmappingDTO unmappingDTO) {
        // Validate required fields
        if (unmappingDTO == null) {
            throw new IllegalArgumentException("UnmappingDTO cannot be null");
        }
        if (unmappingDTO.getCityId() == null) {
            throw new IllegalArgumentException("cityId is required");
        }
        if (unmappingDTO.getCampusIds() == null || unmappingDTO.getCampusIds().isEmpty()) {
            throw new IllegalArgumentException("campusIds array is required and cannot be empty");
        }
        if (unmappingDTO.getPayrollId() == null || unmappingDTO.getPayrollId().trim().isEmpty()) {
            throw new IllegalArgumentException("payrollId is required");
        }
        if (unmappingDTO.getLastDate() == null) {
            throw new IllegalArgumentException("lastDate is required");
        }
        
        // Step 1: Validate City exists
        cityRepository.findById(unmappingDTO.getCityId())
            .orElseThrow(() -> new ResourceNotFoundException("City not found with ID: " + unmappingDTO.getCityId()));
        
        // Step 2: Always use campusIds array - required field
        List<Integer> campusIdsList = unmappingDTO.getCampusIds();
        boolean useMultipleCampuses = campusIdsList.size() > 1;
        
        // Step 3: Validate all campuses exist and belong to the City
        for (Integer campusId : campusIdsList) {
            Campus campus = campusRepository.findByCampusIdAndIsActive(campusId, 1)
                .orElseThrow(() -> new ResourceNotFoundException("Active Campus not found with ID: " + campusId));
            
            if (campus.getCity() == null || campus.getCity().getCityId() != unmappingDTO.getCityId()) {
                throw new ResourceNotFoundException(
                    String.format("Campus with ID %d is not assigned to City with ID %d", 
                                campusId, unmappingDTO.getCityId()));
            }
        }
        
        // Step 4: Find Employee by payrollId
        Employee employee = findEmployeeByPayrollId(unmappingDTO.getPayrollId());
        
        // Step 5: Validate Employee is active
        if (employee.getIs_active() != 1) {
            throw new ResourceNotFoundException("Employee with payrollId " + unmappingDTO.getPayrollId() + " is not active");
        }
        
        // Step 6: Get employee's existing campus (do not change it)
        Campus employeeCampus = employee.getCampus_id();
        if (employeeCampus == null) {
            throw new ResourceNotFoundException("Employee with payrollId " + unmappingDTO.getPayrollId() + " does not have a campus assigned. Cannot perform unmapping.");
        }
        
        // Step 7: Deactivate SharedEmployee records only if multiple campuses are selected
        // Single campus unmapping doesn't use SharedEmployee table
        if (useMultipleCampuses) {
            for (Integer campusId : campusIdsList) {
                List<SharedEmployee> sharedEmployees = sharedEmployeeRepository.findAllByEmpIdAndCampusId(employee.getEmp_id(), campusId);
                for (SharedEmployee sharedEmployee : sharedEmployees) {
                    sharedEmployee.setIsActive(0);
                    sharedEmployee.setUpdatedBy(unmappingDTO.getUpdatedBy() != null ? unmappingDTO.getUpdatedBy() : 1);
                    sharedEmployee.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
                    sharedEmployeeRepository.save(sharedEmployee);
                }
            }
        }
        // If single campus, no SharedEmployee table update needed
        
        // Step 8: Unmap manager if the provided managerId matches the current manager
        // Treat 0 or null as "don't touch this field"
        Integer managerIdValue = unmappingDTO.getManagerId();
        if (managerIdValue != null && managerIdValue != 0) {
            if (employee.getEmployee_manager_id() != null && 
                employee.getEmployee_manager_id().getEmp_id() == managerIdValue) {
                // Validate manager is from the same campus as employee
                Employee currentManager = employee.getEmployee_manager_id();
                if (currentManager.getCampus_id() == null || currentManager.getCampus_id().getCampusId() != employeeCampus.getCampusId()) {
                    throw new ResourceNotFoundException(
                        String.format("Cannot unmap manager: Manager with ID %d is not from the same campus as employee. Employee campus: %d, Manager campus: %s", 
                                    managerIdValue, employeeCampus.getCampusId(), 
                                    currentManager.getCampus_id() != null ? String.valueOf(currentManager.getCampus_id().getCampusId()) : "null"));
                }
                employee.setEmployee_manager_id(null);
            } else if (employee.getEmployee_manager_id() == null) {
                throw new ResourceNotFoundException(
                    "Employee does not have a manager assigned to unmap");
            } else {
                throw new ResourceNotFoundException(
                    String.format("Employee's current manager ID (%d) does not match the provided managerId (%d)", 
                                employee.getEmployee_manager_id().getEmp_id(), managerIdValue));
            }
        }
        // If managerId is null or 0, don't touch the manager field
        
        // Step 9: Unmap reporting manager if the provided reportingManagerId matches the current reporting manager
        // Treat 0 or null as "don't touch this field"
        Integer reportingManagerIdValue = unmappingDTO.getReportingManagerId();
        if (reportingManagerIdValue != null && reportingManagerIdValue != 0) {
            if (employee.getEmployee_reporting_id() != null && 
                employee.getEmployee_reporting_id().getEmp_id() == reportingManagerIdValue) {
                // Validate reporting manager is from the same campus as employee
                Employee currentReportingManager = employee.getEmployee_reporting_id();
                if (currentReportingManager.getCampus_id() == null || currentReportingManager.getCampus_id().getCampusId() != employeeCampus.getCampusId()) {
                    throw new ResourceNotFoundException(
                        String.format("Cannot unmap reporting manager: Reporting Manager with ID %d is not from the same campus as employee. Employee campus: %d, Reporting Manager campus: %s", 
                                    reportingManagerIdValue, employeeCampus.getCampusId(), 
                                    currentReportingManager.getCampus_id() != null ? String.valueOf(currentReportingManager.getCampus_id().getCampusId()) : "null"));
                }
                employee.setEmployee_reporting_id(null);
            } else if (employee.getEmployee_reporting_id() == null) {
                throw new ResourceNotFoundException(
                    "Employee does not have a reporting manager assigned to unmap");
            } else {
                throw new ResourceNotFoundException(
                    String.format("Employee's current reporting manager ID (%d) does not match the provided reportingManagerId (%d)", 
                                employee.getEmployee_reporting_id().getEmp_id(), reportingManagerIdValue));
            }
        }
        // If reportingManagerId is null or 0, don't touch the reporting manager field
        
        // Step 10: Update last date of working (contract end date)
        employee.setContract_end_date(unmappingDTO.getLastDate());
        
        // Step 11: Update remarks: if value exists and is not empty, set it; otherwise set to null
        // Treat null, empty string, whitespace-only, or string "null" as null
        String remarkValue = unmappingDTO.getRemark();
        if (remarkValue != null && !remarkValue.trim().isEmpty() && !remarkValue.trim().equalsIgnoreCase("null")) {
            employee.setRemarks(remarkValue.trim());
        } else {
            employee.setRemarks(null);
        }
        
        // Step 12: Update audit fields
        employee.setUpdated_by(unmappingDTO.getUpdatedBy() != null ? unmappingDTO.getUpdatedBy() : 1);
        employee.setUpdated_date(new Timestamp(System.currentTimeMillis()));
        
        employeeRepository.save(employee);
        
        return unmappingDTO;
    }
    
    /**
     * Unmaps manager and/or reporting manager from multiple employees in bulk.
     * Does not require department or designation - but requires city, campus, and last date of working.
     * 
     * Flow:
     * 1. Validate City exists
     * 2. Validate Campus (must belong to City)
     * 3. For each payrollId in the list:
     *    - Find Employee by payrollId
     *    - Validate Employee is active
     *    - Update campus
     *    - Set manager_id to null if provided managerId matches current manager
     *    - Set reporting_manager_id to null if provided reportingManagerId matches current reporting manager
     *    - Update last date of working (contract end date)
     *    - Update remarks (set to null if empty)
     *    - Update employee
     * 
     * @param bulkUnmappingDTO The bulk unmapping request DTO
     * @return The same BulkUnmappingDTO that was passed in
     */
    @Transactional
    public BulkUnmappingDTO unmapMultipleEmployees(BulkUnmappingDTO bulkUnmappingDTO) {
        // Validate required fields
        if (bulkUnmappingDTO == null) {
            throw new IllegalArgumentException("BulkUnmappingDTO cannot be null");
        }
        if (bulkUnmappingDTO.getCityId() == null) {
            throw new IllegalArgumentException("cityId is required");
        }
        if (bulkUnmappingDTO.getCampusIds() == null || bulkUnmappingDTO.getCampusIds().isEmpty()) {
            throw new IllegalArgumentException("campusIds array is required and cannot be empty");
        }
        if (bulkUnmappingDTO.getPayrollIds() == null || bulkUnmappingDTO.getPayrollIds().isEmpty()) {
            throw new IllegalArgumentException("payrollIds list is required and cannot be empty");
        }
        // Validate that all payrollIds in the list are not null or empty
        for (String payrollId : bulkUnmappingDTO.getPayrollIds()) {
            if (payrollId == null || payrollId.trim().isEmpty()) {
                throw new IllegalArgumentException("payrollIds list cannot contain null or empty values");
            }
        }
        if (bulkUnmappingDTO.getLastDate() == null) {
            throw new IllegalArgumentException("lastDate is required");
        }
        
        // Step 1: Validate City exists
        cityRepository.findById(bulkUnmappingDTO.getCityId())
            .orElseThrow(() -> new ResourceNotFoundException("City not found with ID: " + bulkUnmappingDTO.getCityId()));
        
        // Step 2: Always use campusIds array - required field
        List<Integer> campusIdsToUnmap = bulkUnmappingDTO.getCampusIds();
        boolean useMultipleCampuses = campusIdsToUnmap.size() > 1;
        
        // Step 3: Validate all campuses exist and belong to the City
        for (Integer campusId : campusIdsToUnmap) {
            Campus campus = campusRepository.findByCampusIdAndIsActive(campusId, 1)
                .orElseThrow(() -> new ResourceNotFoundException("Active Campus not found with ID: " + campusId));
            
            if (campus.getCity() == null || campus.getCity().getCityId() != bulkUnmappingDTO.getCityId()) {
                throw new ResourceNotFoundException(
                    String.format("Campus with ID %d is not assigned to City with ID %d", 
                                campusId, bulkUnmappingDTO.getCityId()));
            }
        }
        
        // Step 3: Process each payrollId
        List<String> processedPayrollIds = new ArrayList<>();
        List<String> failedPayrollIds = new ArrayList<>();
        
        for (String payrollId : bulkUnmappingDTO.getPayrollIds()) {
            try {
                // Find Employee by payrollId
                Employee employee = findEmployeeByPayrollId(payrollId);
                
                // Validate Employee is active
                if (employee.getIs_active() != 1) {
                    failedPayrollIds.add(payrollId + " (not active)");
                    continue;
                }
                
                // Get employee's existing campus (do not change it)
                Campus employeeCampus = employee.getCampus_id();
                if (employeeCampus == null) {
                    failedPayrollIds.add(payrollId + " (no campus assigned)");
                    continue;
                }
                
                // Deactivate SharedEmployee records for the specified campuses
                // Only deactivate if multiple campuses are selected (single campus doesn't use SharedEmployee table)
                if (useMultipleCampuses) {
                    for (Integer campusId : campusIdsToUnmap) {
                        List<SharedEmployee> sharedEmployees = sharedEmployeeRepository.findAllByEmpIdAndCampusId(employee.getEmp_id(), campusId);
                        for (SharedEmployee sharedEmployee : sharedEmployees) {
                            sharedEmployee.setIsActive(0);
                            sharedEmployee.setUpdatedBy(bulkUnmappingDTO.getUpdatedBy() != null ? bulkUnmappingDTO.getUpdatedBy() : 1);
                            sharedEmployee.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
                            sharedEmployeeRepository.save(sharedEmployee);
                        }
                    }
                }
                
                // Unmap manager if the provided managerId matches the current manager
                // Treat 0 or null as "don't touch this field"
                Integer managerIdValue = bulkUnmappingDTO.getManagerId();
                if (managerIdValue != null && managerIdValue != 0) {
                    if (employee.getEmployee_manager_id() != null && 
                        employee.getEmployee_manager_id().getEmp_id() == managerIdValue) {
                        // Validate manager is from the same campus as employee
                        Employee currentManager = employee.getEmployee_manager_id();
                        if (currentManager.getCampus_id() == null || currentManager.getCampus_id().getCampusId() != employeeCampus.getCampusId()) {
                            failedPayrollIds.add(payrollId + " (manager not from same campus: employee campus=" + 
                                                employeeCampus.getCampusId() + ", manager campus=" + 
                                                (currentManager.getCampus_id() != null ? currentManager.getCampus_id().getCampusId() : "null") + ")");
                            continue;
                        }
                        employee.setEmployee_manager_id(null);
                    } else if (employee.getEmployee_manager_id() == null) {
                        failedPayrollIds.add(payrollId + " (no manager assigned)");
                        continue;
                    } else {
                        failedPayrollIds.add(payrollId + " (manager ID mismatch: current=" + 
                                            employee.getEmployee_manager_id().getEmp_id() + 
                                            ", provided=" + managerIdValue + ")");
                        continue;
                    }
                }
                // If managerId is null or 0, don't touch the manager field
                
                // Unmap reporting manager if the provided reportingManagerId matches the current reporting manager
                // Treat 0 or null as "don't touch this field"
                Integer reportingManagerIdValue = bulkUnmappingDTO.getReportingManagerId();
                if (reportingManagerIdValue != null && reportingManagerIdValue != 0) {
                    if (employee.getEmployee_reporting_id() != null && 
                        employee.getEmployee_reporting_id().getEmp_id() == reportingManagerIdValue) {
                        // Validate reporting manager is from the same campus as employee
                        Employee currentReportingManager = employee.getEmployee_reporting_id();
                        if (currentReportingManager.getCampus_id() == null || currentReportingManager.getCampus_id().getCampusId() != employeeCampus.getCampusId()) {
                            failedPayrollIds.add(payrollId + " (reporting manager not from same campus: employee campus=" + 
                                                employeeCampus.getCampusId() + ", reporting manager campus=" + 
                                                (currentReportingManager.getCampus_id() != null ? currentReportingManager.getCampus_id().getCampusId() : "null") + ")");
                            continue;
                        }
                        employee.setEmployee_reporting_id(null);
                    } else if (employee.getEmployee_reporting_id() == null) {
                        failedPayrollIds.add(payrollId + " (no reporting manager assigned)");
                        continue;
                    } else {
                        failedPayrollIds.add(payrollId + " (reporting manager ID mismatch: current=" + 
                                            employee.getEmployee_reporting_id().getEmp_id() + 
                                            ", provided=" + reportingManagerIdValue + ")");
                        continue;
                    }
                }
                // If reportingManagerId is null or 0, don't touch the reporting manager field
                
                // Update last date of working (contract end date)
                employee.setContract_end_date(bulkUnmappingDTO.getLastDate());
                
                // Update remarks: if value exists and is not empty, set it; otherwise set to null
                // Treat null, empty string, whitespace-only, or string "null" as null
                String remarkValue = bulkUnmappingDTO.getRemark();
                if (remarkValue != null && !remarkValue.trim().isEmpty() && !remarkValue.trim().equalsIgnoreCase("null")) {
                    employee.setRemarks(remarkValue.trim());
                } else {
                    employee.setRemarks(null);
                }
                
                // Update audit fields
                employee.setUpdated_by(bulkUnmappingDTO.getUpdatedBy() != null ? bulkUnmappingDTO.getUpdatedBy() : 1);
                employee.setUpdated_date(new Timestamp(System.currentTimeMillis()));
                
                employeeRepository.save(employee);
                processedPayrollIds.add(payrollId);
                
            } catch (ResourceNotFoundException e) {
                failedPayrollIds.add(payrollId + " (not found)");
            } catch (Exception e) {
                failedPayrollIds.add(payrollId + " (" + e.getMessage() + ")");
            }
        }
        
        return bulkUnmappingDTO;
    }
}

