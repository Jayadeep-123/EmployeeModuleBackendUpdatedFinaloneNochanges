package com.employee.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.employee.dto.ManagerMappingDTO;
import com.employee.dto.BulkManagerMappingDTO;
import com.employee.dto.UnmappingDTO;
import com.employee.dto.BulkUnmappingDTO;
import com.employee.service.ManagerMappingService;

/**
 * Controller for Manager Mapping functionality.
 * Handles employee mapping based on City → Campus → Department → Designation hierarchy
 * and updates work starting dates.
 */
@RestController
@RequestMapping("/api/manager-mapping")
public class ManagerMappingController {
    
    private static final Logger logger = LoggerFactory.getLogger(ManagerMappingController.class);
    
    @Autowired
    private ManagerMappingService managerMappingService;
    
    /**
     * POST endpoint to map employee and update their details.
     * 
     * Flow:
     * 1. Validates City
     * 2. Validates Campus (must belong to City)
     * 3. Validates Department exists and is active (master table - independent)
     * 4. Validates Designation exists and is active
     * 5. Finds Employee by payrollId
     * 6. Validates Employee is active
     * 7. Validates Manager (managerId) exists and is active (if provided)
     * 8. Validates Reporting Manager (reportingManagerId) exists and is active (if provided)
     * 9. Updates employee: campus, department, designation, manager, reporting manager, work starting date, remarks
     * 
     * Note: All exceptions are handled by GlobalExceptionHandler
     * 
     * @param mappingDTO Request body containing cityId, campusId, departmentId, designationId, 
     *                   payrollId (required), managerId (optional), reportingManagerId (optional), 
     *                   workStartingDate, remark (optional), and updatedBy
     * @return The same ManagerMappingDTO that was passed in
     */
    @PostMapping("/map-employees")
    public ResponseEntity<ManagerMappingDTO> mapEmployeesAndUpdateWorkDate(@RequestBody ManagerMappingDTO mappingDTO) {
        logger.info("Received manager mapping request: {}", mappingDTO);
        
        // Perform the mapping and update (validation and exception handling done in service and GlobalExceptionHandler)
        ManagerMappingDTO response = managerMappingService.mapEmployeesAndUpdateWorkDate(mappingDTO);
        
        logger.info("Successfully updated employee with payrollId: {}", response.getPayrollId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * POST endpoint to map multiple employees and update their details in bulk.
     * 
     * Flow:
     * 1. Validates City
     * 2. Validates Campus (must belong to City)
     * 3. Validates Department exists and is active (master table - independent)
     * 4. Validates Designation exists and is active
     * 5. Validates Manager (managerId) exists and is active (if provided)
     * 6. Validates Reporting Manager (reportingManagerId) exists and is active (if provided)
     * 7. For each payrollId in the list:
     *    - Finds Employee by payrollId
     *    - Validates Employee is active
     *    - Updates employee: campus, department, designation, manager, reporting manager, work starting date, remarks
     * 
     * Note: All exceptions are handled by GlobalExceptionHandler
     * 
     * @param bulkMappingDTO Request body containing cityId, campusId, departmentId, designationId, 
     *                       payrollIds (list of payroll IDs, required), managerId (optional), reportingManagerId (optional), 
     *                       workStartingDate, remark (optional), and updatedBy
     * @return The same BulkManagerMappingDTO that was passed in
     */
    @PostMapping("/map-multiple-employees")
    public ResponseEntity<BulkManagerMappingDTO> mapMultipleEmployeesAndUpdateWorkDate(@RequestBody BulkManagerMappingDTO bulkMappingDTO) {
        logger.info("Received bulk manager mapping request for {} payrollIds", 
                   bulkMappingDTO.getPayrollIds() != null ? bulkMappingDTO.getPayrollIds().size() : 0);
        
        // Perform the bulk mapping and update (validation and exception handling done in service and GlobalExceptionHandler)
        BulkManagerMappingDTO response = managerMappingService.mapMultipleEmployeesAndUpdateWorkDate(bulkMappingDTO);
        
        logger.info("Successfully processed bulk update for {} payrollIds", 
                   response.getPayrollIds() != null ? response.getPayrollIds().size() : 0);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * POST endpoint to unmap (remove) manager and/or reporting manager from an employee.
     * Does not require department or designation - but requires city, campus, and last date of working.
     * 
     * Flow:
     * 1. Validates City exists
     * 2. Validates Campus (must belong to City)
     * 3. Finds Employee by payrollId
     * 4. Validates Employee is active
     * 5. Updates campus
     * 6. Sets manager_id to null if provided managerId matches current manager
     * 7. Sets reporting_manager_id to null if provided reportingManagerId matches current reporting manager
     * 8. Updates last date of working (contract end date)
     * 9. Updates remarks (set to null if empty)
     * 10. Updates employee
     * 
     * Note: All exceptions are handled by GlobalExceptionHandler
     * 
     * @param unmappingDTO Request body containing cityId (required), campusId (required), 
     *                     payrollId (required), managerId (optional, employee ID to unmap; pass 0 or null to skip), 
     *                     reportingManagerId (optional, employee ID to unmap; pass 0 or null to skip), 
     *                     lastDate (required), remark (optional), and updatedBy
     * @return The same UnmappingDTO that was passed in
     */
    @PostMapping("/unmap-employee")
    public ResponseEntity<UnmappingDTO> unmapEmployee(@RequestBody UnmappingDTO unmappingDTO) {
        logger.info("Received unmapping request for payrollId: {}", unmappingDTO.getPayrollId());
        
        // Perform the unmapping (validation and exception handling done in service and GlobalExceptionHandler)
        UnmappingDTO response = managerMappingService.unmapEmployee(unmappingDTO);
        
        logger.info("Successfully unmapped employee with payrollId: {}", response.getPayrollId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * POST endpoint to unmap (remove) manager and/or reporting manager from multiple employees in bulk.
     * Does not require department or designation - but requires city, campus, and last date of working.
     * 
     * Flow:
     * 1. Validates City exists
     * 2. Validates Campus (must belong to City)
     * 3. For each payrollId in the list:
     *    - Finds Employee by payrollId
     *    - Validates Employee is active
     *    - Updates campus
     *    - Sets manager_id to null if provided managerId matches current manager
     *    - Sets reporting_manager_id to null if provided reportingManagerId matches current reporting manager
     *    - Updates last date of working (contract end date)
     *    - Updates remarks (set to null if empty)
     *    - Updates employee
     * 
     * Note: All exceptions are handled by GlobalExceptionHandler
     * 
     * @param bulkUnmappingDTO Request body containing cityId (required), campusId (required), 
     *                          payrollIds (list of payroll IDs, required), 
     *                          managerId (optional, employee ID to unmap; pass 0 or null to skip), 
     *                          reportingManagerId (optional, employee ID to unmap; pass 0 or null to skip), 
     *                          lastDate (required), remark (optional), and updatedBy
     * @return The same BulkUnmappingDTO that was passed in
     */
    @PostMapping("/unmap-multiple-employees")
    public ResponseEntity<BulkUnmappingDTO> unmapMultipleEmployees(@RequestBody BulkUnmappingDTO bulkUnmappingDTO) {
        logger.info("Received bulk unmapping request for {} payrollIds", 
                   bulkUnmappingDTO.getPayrollIds() != null ? bulkUnmappingDTO.getPayrollIds().size() : 0);
        
        // Perform the bulk unmapping (validation and exception handling done in service and GlobalExceptionHandler)
        BulkUnmappingDTO response = managerMappingService.unmapMultipleEmployees(bulkUnmappingDTO);
        
        logger.info("Successfully processed bulk unmapping for {} payrollIds", 
                   response.getPayrollIds() != null ? response.getPayrollIds().size() : 0);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

