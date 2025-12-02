package com.employee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.employee.entity.SkillTestDetails;

@Repository
public interface SkillTestDetailsRepository extends JpaRepository<SkillTestDetails, Integer> {

    @Query("SELECT MAX(s.tempPayrollId) FROM SkillTestDetails s WHERE s.tempPayrollId LIKE :keyPrefix")
    String findMaxTempPayrollIdByKey(@Param("keyPrefix") String keyPrefix);

    @Query("SELECT std FROM SkillTestDetails std WHERE std.tempPayrollId = :tempPayrollId")
    Optional<SkillTestDetails> findByTempPayrollId(@Param("tempPayrollId") String tempPayrollId);

    // FIX: Changed aadhaarNo from String to Long
    @Query("SELECT std FROM SkillTestDetails std WHERE std.aadhaar_no = :aadhaarNo AND std.contact_number = :contactNumber")
    Optional<SkillTestDetails> findByAadhaarNoAndContactNumber(
        @Param("aadhaarNo") Long aadhaarNo, 
        @Param("contactNumber") Long contactNumber
    );

    // FIX: Changed aadhaarNo from String to Long
    @Query("SELECT std FROM SkillTestDetails std WHERE std.aadhaar_no = :aadhaarNo")
    Optional<SkillTestDetails> findByAadhaarNo(@Param("aadhaarNo") Long aadhaarNo);

    @Query("SELECT std FROM SkillTestDetails std WHERE std.contact_number = :contactNumber")
    Optional<SkillTestDetails> findByContactNumber(@Param("contactNumber") Long contactNumber);

    // FIX: Changed aadhaarNo from String to Long
    @Query("SELECT std FROM SkillTestDetails std WHERE std.aadhaar_no = :aadhaarNo AND std.isActive = 1")
    Optional<SkillTestDetails> findActiveByAadhaarNo(@Param("aadhaarNo") Long aadhaarNo);

    @Query("SELECT std FROM SkillTestDetails std WHERE std.contact_number = :contactNumber AND std.isActive = 1")
    Optional<SkillTestDetails> findActiveByContactNumber(@Param("contactNumber") Long contactNumber);

    @Query("SELECT std FROM SkillTestDetails std WHERE std.tempPayrollId = :tempPayrollId AND std.isActive = 1")
    Optional<SkillTestDetails> findActiveByTempPayrollId(@Param("tempPayrollId") String tempPayrollId);
}