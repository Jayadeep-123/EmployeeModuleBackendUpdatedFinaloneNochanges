package com.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QualificationDetailsDto {
    private String qualificationName;
    private String qualificationDegree;
    private String specialization;
    private String institute;
    private String university;
    private Integer passedoutYear;
}