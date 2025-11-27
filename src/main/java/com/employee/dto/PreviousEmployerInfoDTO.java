package com.employee.dto;

import java.sql.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Previous Employer Info Tab (Step 4)
 * Maps to: EmpExperienceDetails entity (multiple records)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreviousEmployerInfoDTO {
	
	private List<EmployerDetailsDTO> previousEmployers; // Can have multiple employers
	
	// Audit fields - passed from frontend
	private Integer createdBy; // User ID who created the employee record
	private Integer updatedBy; // User ID who updated the employee record
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class EmployerDetailsDTO {
		private String companyName; // pre_organigation_name
		private String designation;
		private Date fromDate; // date_of_join
		private Date toDate; // date_of_leave
		private String leavingReason;
		private String companyAddressLine1;
		private String companyAddressLine2; // Will be combined into company_addr
		private String natureOfDuties;
		private Integer grossSalaryPerMonth; // gross_salary
		private Integer ctc; // Cost to Company - Note: This field might not be in EmpExperienceDetails entity
		// Note: preChaitanyaId has been moved to BasicInfoDTO
	}
}

