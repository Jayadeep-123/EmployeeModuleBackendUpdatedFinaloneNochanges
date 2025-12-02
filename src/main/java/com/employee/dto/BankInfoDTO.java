package com.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Bank Info Tab (Step 8) Maps to: BankDetails entity Note: Can have two
 * records - one for Personal Account, one for Salary Account Uses: OrgBank
 * entity for bank name dropdown
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankInfoDTO {
// General Bank Information
	private Integer paymentTypeId; // From "Payment Type" dropdown - maps to EmpPaymentType.emp_payment_type_id
	private Integer bankId; // From "Bank Name" dropdown - maps to OrgBank.org_bank_id (ONLY for Salary
							// Account)
	private Integer bankBranchId; // From "Bank Branch" dropdown - maps to OrgBankBranch.org_bank_branch_id (ONLY
									// for Salary Account) - Optional: can provide ID or name
	private String bankBranchName; // Bank Branch Name - can be provided instead of bankBranchId (ONLY for Salary
									// Account)
// Personal Account Information
	private Boolean salaryLessThan40000; // Checkbox from "Salary Less Than 40,000"
	private PersonalAccountDTO personalAccount;
// Salary Account Information
	private SalaryAccountDTO salaryAccount;
	private Integer createdBy; // User ID who created the employee record
	private Integer updatedBy; // User ID who updated the employee record

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PersonalAccountDTO {
		private String bankName; // From "Personal Account Bank Name" - user enters text (NOT from master table)
		private String accountNo; // From "Personal Account No"
		private String accountHolderName; // From "Personal Account Holder Name"
		private String ifscCode; // From "Personal Account IFSC Code"
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SalaryAccountDTO {
		private Integer bankId; // From "Select Bank" dropdown - maps to OrgBank.org_bank_id
		private String ifscCode; // From "IFSC Code"
		private String accountNo; // From "Account No"
		private String accountHolderName; // Required NOT NULL - Account Holder Name
		private String payableAt; // From "Payable At" - maps to BankDetails.payable_at
// Note: BankDetails entity has: acc_type, bank_name, bank_branch, bank_holder_name, acc_no, ifsc_code, payable_at
	}
}