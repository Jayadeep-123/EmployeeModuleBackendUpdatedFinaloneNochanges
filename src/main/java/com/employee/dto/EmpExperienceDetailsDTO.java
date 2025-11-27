package com.employee.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmpExperienceDetailsDTO {

    private String companyName;       // Mapped from preOrzanigationName
    private String designation;
    private LocalDate fromDate;       // Mapped from dateOfJoin
    private LocalDate toDate;         // Mapped from dateOfLeave
    private String leavingReason;
    private String companyAddress;    // Mapped from companyAddr
    private String natureOfDuties;
    
    // As requested: Calculated as (grossSalary / 12)
    private BigDecimal grossSalaryPerMonth; 
    
    // As requested: Mapped directly from grossSalary
    private BigDecimal ctc;
}