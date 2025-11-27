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
@Table(name = "sce_emp_sal_info", schema = "sce_employee")
public class EmpSalaryInfo {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "emp_sal_info_id")
	private Integer empSalInfoId;
	
	@ManyToOne
	@JoinColumn(name = "emp_id", nullable = false)
	private Employee empId; // Required NOT NULL - Foreign key to Employee
	
	@Column(name = "payroll_id")
	private String payrollId; // Optional - can be null (will be updated later)
	
	@ManyToOne
	@JoinColumn(name = "emp_payment_type_id", nullable = true)
	private EmpPaymentType empPaymentType; // Optional - can be null
	
	@Column(name = "monthly_ctc", nullable = false)
	private Double monthlyCtc; // Required NOT NULL - float8 (double)
	
	@Column(name = "ctc_words", length = 250)
	private String ctcWords; // Optional - nullable
	
	@Column(name = "yearly_ctc", nullable = false)
	private Double yearlyCtc; // Required NOT NULL - float8 (double)
	
	@ManyToOne
	@JoinColumn(name = "emp_structure_id", nullable = false)
	private EmpStructure empStructure; // Required NOT NULL - Foreign key to sce_emp_structure
	
	@ManyToOne
	@JoinColumn(name = "grade_id", nullable = true)
	private EmpGrade grade; // Optional - nullable (FK to sce_emp_grade)
	
	@Column(name = "is_active", nullable = false)
	private Integer isActive = 1; // Default 1
	
	@Column(name = "temp_payroll_id", length = 50)
	private String tempPayrollId; // Optional - nullable
	
	@ManyToOne
	@JoinColumn(name = "cost_center_id", nullable = true)
	private CostCenter costCenter; // Optional - nullable (FK to sce_emp_costcenter)
	
	@Column(name = "is_pf_eligible", nullable = false)
	private Integer isPfEligible; // Required NOT NULL - Default 0 (int2)
	
	@Column(name = "is_esi_eligible", nullable = false)
	private Integer isEsiEligible; // Required NOT NULL - Default 0 (int2)
	
	// Audit fields - required NOT NULL columns
	@Column(name = "created_by", nullable = false)
	private Integer createdBy = 1; // Default to 1 if not provided
	
	@Column(name = "created_date", nullable = false)
	private Timestamp createdDate = new Timestamp(System.currentTimeMillis());
	
	@Column(name = "updated_by")
	private Integer updatedBy;
	
	@Column(name = "updated_date")
	private Timestamp updatedDate;
}
 
 
 