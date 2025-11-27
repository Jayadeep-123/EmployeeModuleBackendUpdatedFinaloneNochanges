package com.employee.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.employee.dto.EmployeeSearchRequestDTO;
import com.employee.dto.EmployeeSearchResponseDTO;
import com.employee.service.EmployeeSearchService;

/**
 * Controller for flexible employee search
 * Supports searching by cityId, employeeTypeId, and payrollId in various combinations
 */
@RestController
@RequestMapping("/api/employee/search")
public class EmployeeSearchController {

    @Autowired
    private EmployeeSearchService employeeSearchService;

    /**
     * GET endpoint for flexible employee search
     * 
     * Supports various filter combinations:
     * - cityId + payrollId
     * - cityId + employeeTypeId + payrollId
     * - cityId only
     * - employeeTypeId + payrollId
     * - payrollId only
     * - employeeTypeId only
     * 
     * @param cityId Optional - City ID filter
     * @param employeeTypeId Optional - Employee Type ID filter
     * @param payrollId Optional - Payroll ID filter
     * @return ResponseEntity with list of EmployeeSearchResponseDTO containing:
     *         employeeId, employeeName, department, tempPayrollId
     */
    @GetMapping
    public ResponseEntity<?> searchEmployees(
            @RequestParam(required = false) Integer cityId,
            @RequestParam(required = false) Integer employeeTypeId,
            @RequestParam(required = false) String payrollId) {
        
        // Build search request DTO
        EmployeeSearchRequestDTO searchRequest = new EmployeeSearchRequestDTO();
        searchRequest.setCityId(cityId);
        searchRequest.setEmployeeTypeId(employeeTypeId);
        searchRequest.setPayrollId(payrollId);

        // Validate that at least one filter is provided
        if (cityId == null && employeeTypeId == null && payrollId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("At least one search parameter is required (cityId, employeeTypeId, or payrollId)");
        }

        // Perform search
        List<EmployeeSearchResponseDTO> results = employeeSearchService.searchEmployees(searchRequest);

        // Return results
        if (results == null || results.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No employees found matching the search criteria");
        }

        return ResponseEntity.ok(results);
    }
}

