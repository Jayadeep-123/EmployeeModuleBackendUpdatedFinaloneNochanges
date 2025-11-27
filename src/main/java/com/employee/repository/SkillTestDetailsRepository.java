package com.employee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// Note: SkillTestDetailsDto import is removed as it's not used here
import com.employee.entity.SkillTestDetails;


@Repository
public interface SkillTestDetailsRepository extends JpaRepository<SkillTestDetails, Integer>{

	//
	// --- THIS METHOD WAS REMOVED ---
	// Spring Data's save() method only works with entities (SkillTestDetails),
	// not DTOs (SkillTestDetailsDto).
	//
	// SkillTestDetailsDto save(SkillTestDetailsDto details);
	//

	@Query("SELECT MAX(s.tempPayrollId) FROM SkillTestDetails s WHERE s.tempPayrollId LIKE :keyPrefix")
    String findMaxTempPayrollIdByKey(@Param("keyPrefix") String keyPrefix);
	
    /**
     * Corrected Query: Uses the entity name "SkillTestDetails"
     */
	@Query("SELECT std FROM SkillTestDetails std WHERE std.tempPayrollId = :tempPayrollId")
	Optional<SkillTestDetails> findByTempPayrollId(@Param("tempPayrollId") String tempPayrollId);

	
	@Query("SELECT std FROM SkillTestDetails std WHERE std.aadhaar_no = :aadhaarNo AND std.contact_number = :contactNumber")
	Optional<SkillTestDetails> findByAadhaarNoAndContactNumber(
		@Param("aadhaarNo") String aadhaarNo, 
		@Param("contactNumber") Long contactNumber
	);
	
	/**
	 * Find SkillTestDetails by Aadhaar number only
	 * Used to check if Aadhaar number already exists in SkillTestDetails table
	 */
	@Query("SELECT std FROM SkillTestDetails std WHERE std.aadhaar_no = :aadhaarNo")
	Optional<SkillTestDetails> findByAadhaarNo(@Param("aadhaarNo") String aadhaarNo);
	
	/**
	 * Find SkillTestDetails by Contact number only
	 * Used to check if phone number already exists in SkillTestDetails table
	 */
	@Query("SELECT std FROM SkillTestDetails std WHERE std.contact_number = :contactNumber")
	Optional<SkillTestDetails> findByContactNumber(@Param("contactNumber") Long contactNumber);
	
	/**
	 * Find active SkillTestDetails by Aadhaar number only (is_active = 1)
	 * Used to check if Aadhaar number already exists in active SkillTestDetails records
	 */
	@Query("SELECT std FROM SkillTestDetails std WHERE std.aadhaar_no = :aadhaarNo AND std.isActive = 1")
	Optional<SkillTestDetails> findActiveByAadhaarNo(@Param("aadhaarNo") Long aadharNum);
	
	/**
	 * Find active SkillTestDetails by Contact number only (is_active = 1)
	 * Used to check if phone number already exists in active SkillTestDetails records
	 */
	@Query("SELECT std FROM SkillTestDetails std WHERE std.contact_number = :contactNumber AND std.isActive = 1")
	Optional<SkillTestDetails> findActiveByContactNumber(@Param("contactNumber") Long contactNumber);
	
	/**
	 * Find active SkillTestDetails by tempPayrollId (is_active = 1)
	 * Used to check if tempPayrollId exists in active SkillTestDetails records
	 */
	@Query("SELECT std FROM SkillTestDetails std WHERE std.tempPayrollId = :tempPayrollId AND std.isActive = 1")
	Optional<SkillTestDetails> findActiveByTempPayrollId(@Param("tempPayrollId") String tempPayrollId);
    /**
     * Corrected Query: Uses the entity name "SkillTestDetails"
     *
     * !! WARNING !!
     * This query will still fail because your "SkillTestDetails" entity
     * is missing the "isActive" field.
     *
     * You must either:
     * 1. Add 'private Integer isActive;' to your SkillTestDetails.java entity
     * OR
     * 2. Delete this method if you don't need it.
     */
//	@Query("SELECT std FROM SkillTestDetails std WHERE std.tempPayrollId = :tempPayrollId AND std.isActive = :isActive")
//	Optional<SkillTestDetails> findByTempPayrollIdAndIsActive(@Param("tempPayrollId") String tempPayrollId, @Param("isActive") Integer isActive);

}