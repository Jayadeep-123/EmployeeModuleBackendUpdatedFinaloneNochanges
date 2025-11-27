package com.employee.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employee.dto.EmployeeSearchRequestDTO;
import com.employee.dto.EmployeeSearchResponseDTO;
import com.employee.entity.Employee;
import com.employee.repository.EmployeeRepository;

/**
 * Service for flexible employee search
 * Supports searching by cityId, employeeTypeId, and payrollId in various combinations
 */
@Service
@Transactional(readOnly = true)
public class EmployeeSearchService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeSearchService.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Search employees with flexible filters
     * 
     * @param searchRequest Search request with optional filters (cityId, employeeTypeId, payrollId)
     * @return List of EmployeeSearchResponseDTO containing employee name, department, employee id, temp payroll id
     */
    public List<EmployeeSearchResponseDTO> searchEmployees(EmployeeSearchRequestDTO searchRequest) {
        logger.info("Searching employees with filters - cityId: {}, employeeTypeId: {}, payrollId: {}", 
                searchRequest.getCityId(), searchRequest.getEmployeeTypeId(), searchRequest.getPayrollId());

        List<Employee> employees;

        // Build query based on provided filters
        if (searchRequest.getCityId() != null && searchRequest.getEmployeeTypeId() != null && searchRequest.getPayrollId() != null) {
            // Search by cityId + employeeTypeId + payrollId
            employees = employeeRepository.findByCityIdAndEmployeeTypeIdAndPayrollId(
                    searchRequest.getCityId(), 
                    searchRequest.getEmployeeTypeId(), 
                    searchRequest.getPayrollId());
        } else if (searchRequest.getCityId() != null && searchRequest.getPayrollId() != null) {
            // Search by cityId + payrollId
            employees = employeeRepository.findByCityIdAndPayrollId(
                    searchRequest.getCityId(), 
                    searchRequest.getPayrollId());
        } else if (searchRequest.getCityId() != null && searchRequest.getEmployeeTypeId() != null) {
            // Search by cityId + employeeTypeId
            employees = employeeRepository.findByCityIdAndEmployeeTypeId(
                    searchRequest.getCityId(), 
                    searchRequest.getEmployeeTypeId());
        } else if (searchRequest.getCityId() != null) {
            // Search by cityId only
            employees = employeeRepository.findByCityId(searchRequest.getCityId());
        } else if (searchRequest.getEmployeeTypeId() != null && searchRequest.getPayrollId() != null) {
            // Search by employeeTypeId + payrollId
            employees = employeeRepository.findByEmployeeTypeIdAndPayrollId(
                    searchRequest.getEmployeeTypeId(), 
                    searchRequest.getPayrollId());
        } else if (searchRequest.getPayrollId() != null) {
            // Search by payrollId only
            Optional<Employee> empOpt = employeeRepository.findByPayrollId(searchRequest.getPayrollId());
            employees = empOpt.map(List::of).orElse(new ArrayList<>());
        } else if (searchRequest.getEmployeeTypeId() != null) {
            // Search by employeeTypeId only
            employees = employeeRepository.findByEmployeeTypeId(searchRequest.getEmployeeTypeId());
        } else {
            // No filters provided - return empty list
            logger.warn("No search filters provided");
            return new ArrayList<>();
        }

        // Filter only active employees and map to response DTO
        return employees.stream()
                .filter(emp -> emp.getIs_active() == 1) // Only active employees
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map Employee entity to EmployeeSearchResponseDTO
     */
    private EmployeeSearchResponseDTO mapToResponseDTO(Employee employee) {
        EmployeeSearchResponseDTO dto = new EmployeeSearchResponseDTO();
        dto.setEmployeeId(employee.getEmp_id());
        
        // Combine first_name and last_name
        String firstName = employee.getFirst_name() != null ? employee.getFirst_name() : "";
        String lastName = employee.getLast_name() != null ? employee.getLast_name() : "";
        dto.setEmployeeName((firstName + " " + lastName).trim());
        
        // Get department name
        if (employee.getDepartment() != null && employee.getDepartment().getDepartment_name() != null) {
            dto.setDepartment(employee.getDepartment().getDepartment_name());
        } else {
            dto.setDepartment("N/A");
        }
        
        dto.setTempPayrollId(employee.getTempPayrollId());
        
        return dto;
    }
}

