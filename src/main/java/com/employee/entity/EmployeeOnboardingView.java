package com.employee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sce_emp_onboarding", schema = "sce_employee")
public class EmployeeOnboardingView {
	 @Id
	    @Column(name = "emp_id")
	    private Integer empId;

	    @Column(name = "full_name")
	    private String fullName;

	    @Column(name = "date_of_birth")
	    private String dateOfBirth;

	    @Column(name = "gender_id")
	    private Integer genderId;

	    @Column(name = "gender_name")
	    private String genderName;

	    @Column(name = "designation_id")
	    private Integer designationId;

	    @Column(name = "designation_name")
	    private String designationName;

	    @Column(name = "temp_payroll_id")
	    private String tempPayrollId;

	    @Column(name = "payroll_id")
	    private String payrollId;

	    @Column(name = "primary_mobile_no")
	    private String primaryMobileNo;

	    @Column(name = "email")
	    private String email;

	    @Column(name = "adhaar_no")
	    private String adhaarNo;

	    @Column(name = "adhaar_enrolment_no")
	    private String adhaarEnrolmentNo;

	    @Column(name = "pancard_no")
	    private String pancardNo;

	    @Column(name = "ssc_no")
	    private String sscNo;

	    @Column(name = "category_id")
	    private Integer categoryId;

	    @Column(name = "category_name")
	    private String categoryName;

	    @Column(name = "pre_esi_no")
	    private String preEsiNo;

	    @Column(name = "pre_uan_no")
	    private String preUanNo;

	    @Column(name = "blood_group_id")
	    private Integer bloodGroupId;

	    @Column(name = "blood_group_name")
	    private String bloodGroupName;

	    @Column(name = "total_experience")
	    private Double totalExperience;

	    @Column(name = "highest_qualification_id")
	    private Integer highestQualificationId;

	    @Column(name = "highest_qualification")
	    private String highestQualification;

	    @Column(name = "emp_type")
	    private String empType;
}