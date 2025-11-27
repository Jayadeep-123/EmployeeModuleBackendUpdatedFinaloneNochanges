package com.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryInfoDTO1 {
	
	  private String employeeType;
	    private String department;
	    private String designation;
	    
	    // These 2 fields will be joined from the sce_emp_subject table.
	    // They will be 'null' if the employee is not a teaching type.
	    private String subject;
	    private Integer agreedPeriodsPerWeek; 

}
