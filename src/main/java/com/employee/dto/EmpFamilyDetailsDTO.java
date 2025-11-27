
package com.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmpFamilyDetailsDTO {
    private int empFamilyDetlId;
    private String firstName;
    private String lastName;
    private String occupation;
    private String gender;
    private String bloodGroup;
    private String nationality;
    private String relation;
    private Integer isDependent;
    private String isLate;
    private String email;
    private long contactNumber;
}
