package com.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponseDTO {
    private String addressType;
    private String houseNo;
    private String landmark;
    private String postalCode;
    private String cityName;
    private String stateName;
    private String countryName;
}