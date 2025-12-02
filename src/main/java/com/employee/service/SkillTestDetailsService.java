package com.employee.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.employee.dto.SkillTestDetailsDto;
import com.employee.entity.Campus;
import com.employee.entity.Employee;
import com.employee.entity.EmployeeLevel;
import com.employee.entity.Gender;
import com.employee.entity.Grade;
import com.employee.entity.JoiningAs;
import com.employee.entity.Qualification;
import com.employee.entity.SkillTestDetails;
import com.employee.entity.Stream;
import com.employee.entity.Structure;
import com.employee.entity.Subject;
import com.employee.exception.ResourceNotFoundException;
import com.employee.repository.CampusRepository;
import com.employee.repository.EmployeeLevelRepository;
import com.employee.repository.EmployeeRepository;
import com.employee.repository.GenderRepository;
import com.employee.repository.GradeRepository;
import com.employee.repository.JoiningAsRepository;
import com.employee.repository.QualificationRepository;
import com.employee.repository.SkillTestDetailsRepository;
import com.employee.repository.StreamRepository;
import com.employee.repository.StructureRepository;
import com.employee.repository.SubjectRepository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SkillTestDetailsService {

    @Autowired
    private SkillTestDetailsRepository skillTestDetailsRepository;
    @Autowired
    CampusRepository campusrepository;
    
    // ... (All other autowired repositories remain the same) ...
    @Autowired private GenderRepository genderRepository;
    @Autowired private QualificationRepository qualificationRepository;
    @Autowired private JoiningAsRepository joiningAsRepository;
    @Autowired private StreamRepository streamRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private EmployeeLevelRepository employeeLevelRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired GradeRepository graderepository;
    @Autowired StructureRepository structurerepository;

    private Map<String, AtomicInteger> campusCounters = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeCounters() {
        // ... (Counter logic remains exactly the same as before) ...
        log.info("Initializing campus ID counters...");
        List<Campus> allCampuses = campusrepository.findAllWithCodeNotNull();
        for (Campus campus : allCampuses) {
            String baseKey = "TEMP" + campus.getCode();
            int lastValue = 0;
            try {
                String lastIdInSkillTest = skillTestDetailsRepository.findMaxTempPayrollIdByKey(baseKey + "%");
                String lastIdInEmployee = employeeRepository.findMaxTempPayrollIdByKey(baseKey + "%");
                if (lastIdInSkillTest != null) {
                    try {
                        int val = Integer.parseInt(lastIdInSkillTest.substring(baseKey.length()));
                        lastValue = Math.max(lastValue, val);
                    } catch (Exception e) {}
                }
                if (lastIdInEmployee != null) {
                    try {
                        int val = Integer.parseInt(lastIdInEmployee.substring(baseKey.length()));
                        lastValue = Math.max(lastValue, val);
                    } catch (Exception e) {}
                }
            } catch (Exception e) {
                log.error("Error for key {}: {}", baseKey, e.getMessage());
            }
            campusCounters.put(baseKey, new AtomicInteger(lastValue));
        }
    }

    // ======================================
    // SAVE METHOD - UPDATED RETURN TYPE
    // ======================================
    @Transactional
    public SkillTestDetails saveSkillTestDetails(SkillTestDetailsDto dto, int emp_id) { // <--- Changed return type

        if (emp_id <= 0) {
            throw new IllegalArgumentException("Employee ID must be > 0");
        }

        Employee employee = employeeRepository.findById(emp_id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Campus campus = employee.getCampus_id();
        if (campus == null) {
            throw new ResourceNotFoundException("Employee has no campus assigned");
        }

        // === Generate TempPayrollId ===
        String baseKey = "TEMP" + campus.getCode();
        
        // ... (Logic to calculate maxValue remains the same) ...
        String max1 = skillTestDetailsRepository.findMaxTempPayrollIdByKey(baseKey + "%");
        String max2 = employeeRepository.findMaxTempPayrollIdByKey(baseKey + "%");
        int maxValue = 0;
        try { if (max1 != null) maxValue = Math.max(maxValue, Integer.parseInt(max1.substring(baseKey.length()))); } catch (Exception e) {}
        try { if (max2 != null) maxValue = Math.max(maxValue, Integer.parseInt(max2.substring(baseKey.length()))); } catch (Exception e) {}

        int nextValue = maxValue + 1;
        String generatedTempPayrollId = baseKey + String.format("%04d", nextValue);
        campusCounters.computeIfAbsent(baseKey, k -> new AtomicInteger(0)).set(nextValue);

        // === Aadhaar Validation ===
        if (dto.getAadhaarNo() != null && dto.getAadhaarNo() > 0) {
            String aadhaar = String.valueOf(dto.getAadhaarNo());
            if (!aadhaar.matches("^[0-9]{12}$")) throw new ResourceNotFoundException("Aadhaar must be exactly 12 digits");
            if (!isValidAadhaar(aadhaar)) throw new ResourceNotFoundException("Invalid Aadhaar (Verhoeff failed)");
        }

        // === Fetch Relations ===
        Gender gender = null;
        if (dto.getGenderId() != null && dto.getGenderId() > 0)
            gender = genderRepository.findById(dto.getGenderId()).orElseThrow(() -> new ResourceNotFoundException("Gender not found"));

        Qualification qualification = null;
        if (dto.getQualificationId() != null && dto.getQualificationId() > 0)
            qualification = qualificationRepository.findById(dto.getQualificationId()).orElseThrow(() -> new ResourceNotFoundException("Qualification not found"));

        JoiningAs joiningAs = null;
        if (dto.getJoiningAsId() != null && dto.getJoiningAsId() > 0)
            joiningAs = joiningAsRepository.findById(dto.getJoiningAsId()).orElseThrow(() -> new ResourceNotFoundException("JoiningAs not found"));

        Stream stream = null;
        if (dto.getStreamId() != null && dto.getStreamId() > 0)
            stream = streamRepository.findById(dto.getStreamId()).orElseThrow(() -> new ResourceNotFoundException("Stream not found"));

        Subject subject = null;
        if (dto.getSubjectId() != null && dto.getSubjectId() > 0)
            subject = subjectRepository.findById(dto.getSubjectId()).orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        EmployeeLevel employeeLevel = null;
        if (dto.getEmp_level_id() != null && dto.getEmp_level_id() > 0)
            employeeLevel = employeeLevelRepository.findById(dto.getEmp_level_id()).orElseThrow(() -> new ResourceNotFoundException("EmployeeLevel not found"));

        Grade grade = null;
        if (dto.getEmp_grade_id() != null && dto.getEmp_grade_id() > 0)
            grade = graderepository.findById(dto.getEmp_grade_id()).orElseThrow(() -> new ResourceNotFoundException("Grade not found"));

        Structure structure = null;
        if (dto.getEmp_structure_id() != null && dto.getEmp_structure_id() > 0)
            structure = structurerepository.findById(dto.getEmp_structure_id()).orElseThrow(() -> new ResourceNotFoundException("Structure not found"));

        // === Create Entity ===
        SkillTestDetails newDetails = new SkillTestDetails();

        newDetails.setAadhaar_no(dto.getAadhaarNo());
        newDetails.setPrevious_chaitanya_id(dto.getPreviousChaitanyaId());
        newDetails.setFirstName(dto.getFirstName());
        newDetails.setLastName(dto.getLastName());
        newDetails.setDob(dto.getDob());
        newDetails.setEmail(dto.getEmail());
        newDetails.setTotalExperience(dto.getTotalExperience());
        newDetails.setContact_number(dto.getContactNumber());
        newDetails.setTempPayrollId(generatedTempPayrollId);
        
        // === Password Logic ===
        String firstName = dto.getFirstName();
        String namePart = (firstName == null || firstName.length() < 3) ? "emp" : firstName.substring(0, 3);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        String dobPart = dto.getDob().format(formatter);
        newDetails.setPassword(namePart + dobPart);

        // === Set Relations ===
        newDetails.setGender(gender);
        newDetails.setQualification(qualification);
        newDetails.setJoiningAs(joiningAs);
        newDetails.setStream(stream);
        newDetails.setSubject(subject);
        newDetails.setEmployeeLevel(employeeLevel);
        newDetails.setEmpGrade(grade);
        newDetails.setEmpStructure(structure);
        
        // === Audit Fields ===
        newDetails.setCreatedBy(emp_id);
        newDetails.setCreatedDate(LocalDateTime.now());  
        newDetails.setIsActive(1);                       

        // === RETURN THE SAVED ENTITY ===
        return skillTestDetailsRepository.save(newDetails); 
    }

    // ... (isValidAadhaar remains the same) ...
    private boolean isValidAadhaar(String aadhaar) {
        // ... Verhoeff logic ...
        int[][] D = {{0,1,2,3,4,5,6,7,8,9},{1,2,3,4,0,6,7,8,9,5},{2,3,4,0,1,7,8,9,5,6},{3,4,0,1,2,8,9,5,6,7},{4,0,1,2,3,9,5,6,7,8},{5,9,8,7,6,0,4,3,2,1},{6,5,9,8,7,1,0,4,3,2},{7,6,5,9,8,2,1,0,4,3},{8,7,6,5,9,3,2,1,0,4},{9,8,7,6,5,4,3,2,1,0}};
        int[][] P = {{0,1,2,3,4,5,6,7,8,9},{1,5,7,6,2,8,3,0,9,4},{5,8,0,3,7,9,6,1,4,2},{8,9,1,6,0,4,3,5,2,7},{9,4,5,3,1,2,6,8,7,0},{4,2,8,6,5,7,3,9,0,1},{2,7,9,3,8,0,6,4,1,5},{7,0,4,6,9,1,3,2,5,8}};
        int checksum = 0;
        for (int i = 0; i < aadhaar.length(); i++) {
            int digit = aadhaar.charAt(aadhaar.length() - 1 - i) - '0';
            checksum = D[checksum][P[i % 8][digit]];
        }
        return checksum == 0;
    }
}