package com.employee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.employee.dto.SkillTestDetailsDto;
import com.employee.entity.SkillTestDetails;
import com.employee.service.SkillTestDetailsService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
// Base path for all methods in this controller
@RequestMapping("/api/v1/skill-details")
// Allows requests from any origin (e.g., your React frontend)
@CrossOrigin("*") 
public class SkillTestDetailsController {

    @Autowired
    private SkillTestDetailsService skillTestDetailsService;

    /**
     * Saves new skill test details.
     * The emp_id (e.g., recruiter's ID) is passed in the URL.
     * The new candidate's details are passed in the request body.
     */
    @PostMapping("/save/{emp_id}")
    public ResponseEntity<String> saveSkillTestDetails(@Valid
            @RequestBody SkillTestDetailsDto dto,
            @PathVariable("emp_id") int emp_id) {
        
        log.info("Attempting to save skill test details for emp_id: {}", emp_id);
        
        // Call the service method, passing both the DTO and the emp_id
        String savedDetails = skillTestDetailsService.saveSkillTestDetails(dto, emp_id);
        
        // Return the saved entity with a "201 Created" HTTP status
        return new ResponseEntity<>(savedDetails, HttpStatus.CREATED);
    }
}