package com.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Campus assignment details for multiple campuses mapping.
 * Used in campusMappings array.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampusMappingDTO {
	
	private Integer campusId;      // Required
	private Integer departmentId;  // Required
	private Integer subjectId;     // Optional
	private Integer designationId; // Required
}

