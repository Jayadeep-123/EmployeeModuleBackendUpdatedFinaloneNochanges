package com.employee.entity;
 
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="sce_emp_bank_detl",schema="sce_employee")
public class BankDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "emp_sal_bank_detl_id")
	private Integer empSalBankDetlId;
	
	@Column(name = "acc_type", nullable = false)
	private String accType;
	
	@Column(name = "bank_name")
	private String bankName;
	
	@Column(name = "bank_branch")
	private String bankBranch;
	
	@Column(name = "bank_holder_name", nullable = false)
	private String bankHolderName;
	
	@Column(name = "acc_no", nullable = false)
	private Long accNo;
	
	@Column(name = "ifsc_code", nullable = false)
	private String ifscCode;
	
	@Column(name = "payable_at")
	private String payableAt; // Payable At - moved from net_payable
	
	@Column(name = "bank_statement_cheque_path")
	private String bankStatementChequePath;
	
	@Column(name = "is_active")
	private Integer isActive;
	
	@ManyToOne
	@JoinColumn(name = "emp_id", nullable = false)
	private Employee empId;
	
	@ManyToOne
	@JoinColumn(name = "emp_payment_type_id")
	private EmpPaymentType empPaymentType;
	
	@Column(name = "created_by", nullable = false)
	private Integer createdBy = 1;
	
	@Column(name = "created_date", nullable = false)
	private Timestamp createdDate = new Timestamp(System.currentTimeMillis());
	
	@Column(name = "updated_by")
	private Integer updatedBy;
	
	@Column(name = "updated_date")
	private Timestamp updatedDate;

	

	
}
 
 