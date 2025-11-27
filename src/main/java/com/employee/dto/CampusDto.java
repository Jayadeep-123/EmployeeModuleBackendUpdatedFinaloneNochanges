// CampusDto.java
package com.employee.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampusDto {
    private int campusId;
    private String campusName;
    private String campusType;
    private String campusCode;
    

    private int cityId;
    private String cityName;
}