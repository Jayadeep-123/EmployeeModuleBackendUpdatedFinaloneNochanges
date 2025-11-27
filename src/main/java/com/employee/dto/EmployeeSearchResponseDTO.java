package com.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Employee Search Response
 * Contains only the required fields: employee name, department, employee id, temp payroll id
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSearchResponseDTO {
    private Integer employeeId; // emp_id
    private String employeeName; // first_name + last_name
    private String department; // department name
    private String tempPayrollId; // temp_payroll_id
}

