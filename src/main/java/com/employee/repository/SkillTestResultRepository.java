package com.employee.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.employee.dto.SkillTestResultDTO;
import com.employee.entity.SkillTestResult;

public interface SkillTestResultRepository extends JpaRepository<SkillTestResult, Integer> {

//    @Query("SELECT new com.employee.dto.SkillTestResultDTO("
//         + "d.tempPayrollId, "
//         + "CONCAT(d.first_name, ' ', d.last_name), "
//         + "s.subject_name, "
//         + "r.examDate, "
//         + "r.noOfQuestion, "
//         + "r.noOfQuesAttempt, "
//         + "r.noOfQuesUnattempt, "
//         + "r.noOfQuesWrong, "
//         + "r.totalMarks) "
//         + "FROM SkillTestResult r "
//         + "JOIN r.skillTestDetlId d "
//         // --- THIS IS THE FINAL FIX ---
//         // The field in SkillTestDetails is 'subject', not 'subject_id'
//         + "LEFT JOIN d.subject s "
//         + "WHERE d.tempPayrollId = :tempPayrollId "
//         + "AND r.isActive = 1 "
//         + "ORDER BY r.examDate DESC")
//    List<SkillTestResultDTO> findSkillTestDetailsByPayrollId(@Param("tempPayrollId") String tempPayrollId);
}