package com.employee.dto;
 
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyDetailsResponseDTO {
    
    // Family Member Details (from EmpFamilyDetails and related entities)
    private String name; // e.g., "Name of Father"
    private String relation; // e.g., "Father", "Mother"
    private String bloodGroup; // e.g., "A-"
    private String nationality; // e.g., "Indian"
    private String occupation; // e.g., "IT Job"
    private String emailId; // e.g., "Design@varsitymgmt.com"
    private Long phoneNumber; // e.g., +919876543210
    private Long adhaarNo;
    // Address Details (derived from EmpaddressInfo)
    private String state; // e.g., "Telangana"
    private String country; // e.g., "India"
}
 