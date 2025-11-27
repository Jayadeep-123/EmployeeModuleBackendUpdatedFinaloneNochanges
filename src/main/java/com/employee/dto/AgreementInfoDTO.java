package com.employee.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Agreements Tab (Step 9)
 * Maps to: Employee entity (agreement_org_id, agreement_type, provided_cheque)
 *          EmpChequeDetails entity (multiple records - one per cheque)
 * 
 * Business Logic:
 * - Agreement info (agreement_org_id, agreement_type, provided_cheque) is stored in Employee table
 * - Cheque details are stored in EmpChequeDetails table ONLY if provided_cheque = true
 * - If provided_cheque = false or null, no cheque details are saved
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgreementInfoDTO {
	
	// Agreement Information (stored in Employee table)
	private Integer agreementOrgId; // Agreement Company - FK to sce_campus.sce_organization
	private String agreementType; // Agreement Type - stored in Employee.agreement_type
	
	// Provided Cheque flag (stored in Employee table)
	// If true: cheque details will be saved in EmpChequeDetails table
	// If false or null: no cheque details are saved
	private Boolean providedCheque; // Checkbox "Provided Cheque"
	
	// Is Check Submit flag (stored in Employee.is_check_submit)
	// Passed from frontend checkbox - not derived from providedCheque
	private Integer isCheckSubmit; // Checkbox value: 1 = checked, 0 = unchecked, null = not provided
	
	// Cheque Details (stored in EmpChequeDetails table - only if providedCheque = true)
	// Can have multiple cheques (1st Cheque, 2nd Cheque, etc.)
	private List<ChequeDetailDTO> chequeDetails;
	
	private Integer createdBy; // User ID who created the employee record
	private Integer updatedBy; // User ID who updated the employee record
	
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ChequeDetailDTO {
		private Long chequeNo; // Cheque Number - int8 (bigint)
		private String chequeBankName; // Bank Name - varchar(50)
		private String chequeBankIfscCode; // IFSC Code - varchar(20)
	}
}