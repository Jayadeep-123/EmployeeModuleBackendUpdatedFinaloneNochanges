package com.employee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Immutable // Marks this entity as read-only (best practice for views)
@Table(name = "sce_emp_basic_info", schema = "sce_employee")
public class EmployeeBasicInfoView {

    @Id
    @Column(name = "employee_id")
    private Integer employeeId;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "adhaar_no")
    private String adhaarNo;

    @Column(name = "adhaar_enrolment_no")
    private String adhaarEnrolmentNo;

    @Column(name = "pancard_no")
    private String pancardNo;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "father_name")
    private String fatherName;

    @Column(name = "mother_name")
    private String motherName;

    @Column(name = "pre_uan_no")
    private String preUanNo;

    @Column(name = "pre_esi_no")
    private String preEsiNo;

    @Column(name = "total_experience")
    private Double totalExperience; // Using Double for potential "2.5" years

    @Column(name = "qualification_id")
    private Integer qualificationId;

    @Column(name = "qualification_name")
    private String qualificationName;

    @Column(name = "pre_chaitanya_id")
    private String preChaitanyaId;
    
    @Column(name = "payroll_id")
    private String payrollId;
}