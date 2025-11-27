package com.employee.dto;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillTestDetailsDto {

    // Personal Info
	// 💡 Replaced @Column with standard validation annotations
    private Long aadhaarNo;         // Matches entity: aadhaar_no
    private String previousChaitanyaId; // Matches entity: previous_chaitanya_id
    private String firstName;         // Corrected from 'name'
    private String lastName;          // Corrected from 'surname'
    private LocalDate dob;            // Corrected from 'dateOfBirth'
   
    private Long contactNumber;     
    private String email;
// private String tempPayrollId;
//    
//    private String password;
    
   
    private Long totalExperience;     // Type changed from String to Long

    // --- Foreign Key IDs (These were already correct) ---
    private Integer genderId;
    private Integer qualificationId;
    private Integer joiningAsId;
    private Integer streamId;
    private Integer subjectId;
    private Integer emp_level_id;
    private Integer emp_structure_id;
    private Integer emp_grade_id;
	
}