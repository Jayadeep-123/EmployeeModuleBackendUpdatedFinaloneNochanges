package com.employee.entity;

import java.time.LocalDate;

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
@Table(name ="sce_skill_test_detl",schema="sce_employee")
public class SkillTestDetails {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_test_detl_id")
    private Integer skillTestDetlId;
	
	@Column(unique = true,length = 12)
	private Long aadhaar_no;
	
	@Column(name = "previous_chaitanya_id")
    private String previous_chaitanya_id;

    @Column(name = "first_name", length = 100)
    private String first_name;
    
    @Column(name = "last_name", length = 100)
    private String last_name;

    @Column(name = "dob")
    private LocalDate dob; // Use LocalDate for date-only values
    // Audit fields - required NOT NULL columns
   	@Column(name = "created_by", nullable = false)
   	private Integer created_by ; // Default to 1 if not provided
   	
    @Column(name = "contact_no", length = 10,unique = true)
    private Long contact_number;

    @Column(name = "email",  length = 100,unique = true)
    private String email;

    @Column(name = "total_experience", length = 50)
    private Long totalExperience; 
     
    @Column(name ="temp_payroll_id")
    private String tempPayrollId;
    
    @Column(name ="password")
    private String password;
    
    
//    
//    @Column(name = "emp_grade_id", nullable = false)
//    private Integer empgrade_id;
//    
//    
//    @Column(name = "emp_structure_id", nullable = false)
//    private Integer empstructure_id;
//    

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gender_id") // This is the Foreign Key
    private Gender gender; // Links to your Gender entity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qualification_id") // Foreign Key for the first qualification
    private Qualification qualification; // Links to your Qualification entity

    // --- Professional Info (Master Table Relationships) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "join_type_id") // Foreign Key
    private JoiningAs joiningAs; // Links to your JoiningAs entity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id") // Foreign Key
    private Stream stream; // Links to your Stream entity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id") // Foreign Key
    private Subject subject; // Links to your subject entity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_level_id") // Foreign Key
    private EmployeeLevel employeeLevel; 
    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_grade_id") // Foreign Key
    private Grade empGrade;
   
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_structure_id") // Foreign Key
    private Structure empStructure;
    
    
    
    
    @Column(name = "is_active")
    private Integer isActive = 1;
  

	
}
