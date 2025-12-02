package com.employee.entity;

import java.time.LocalDate;
import java.time.LocalDateTime; // Import this!

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name ="sce_skill_test_detl", schema="sce_employee")
public class SkillTestDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_test_detl_id")
    private Integer skillTestDetlId;
    
    @Column(name="aadhaar_no", unique = true, length = 12)
    private Long aadhaar_no; // Kept as is, but camelCase (aadhaarNo) is better
    
    @Column(name = "previous_chaitanya_id")
    private String previous_chaitanya_id;

    @Column(name = "first_name", length = 100)
    private String firstName; // Changed to camelCase
    
    @Column(name = "last_name", length = 100)
    private String lastName; // Changed to camelCase

    @Column(name = "dob")
    private LocalDate dob;

    // === FIX 1: Renamed to camelCase so setCreatedBy() works ===
    @Column(name = "created_by", nullable = false)
    private Integer createdBy; 
    
    // === FIX 2: Added missing created_date field ===
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "contact_no", length = 10, unique = true)
    private Long contact_number;

    @Column(name = "email", length = 100, unique = true)
    private String email;

    @Column(name = "total_experience", length = 50)
    private Long totalExperience; 
     
    @Column(name ="temp_payroll_id")
    private String tempPayrollId;
    
    @Column(name ="password")
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gender_id")
    private Gender gender; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qualification_id") 
    private Qualification qualification; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "join_type_id") 
    private JoiningAs joiningAs; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id") 
    private Stream stream; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id") 
    private Subject subject; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_level_id") 
    private EmployeeLevel employeeLevel; 
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_grade_id") 
    private Grade empGrade;
   
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_structure_id") 
    private Structure empStructure;
    
    @Column(name = "is_active")
    private Integer isActive = 1;
}