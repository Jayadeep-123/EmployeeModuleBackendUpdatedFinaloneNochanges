package com.employee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.employee.dto.EmpQualificationDTO;
import com.employee.entity.EmpQualification;
import com.employee.entity.Employee;

@Repository
public interface EmpQualificationRepository extends JpaRepository<EmpQualification, Integer> {

    // --- DTO PROJECTION (For UI/Read-Only) ---
    
    @Query("SELECT NEW com.employee.dto.EmpQualificationDTO(" +
            "qual.qualification_name, " +
            "deg.degree_name, " +
            "q.specialization, " +
            "q.university, " +
            "q.institute, " +
            "q.passedout_year" +
            ") " +
            "FROM EmpQualification q " + 
            "LEFT JOIN q.qualification_id qual " +
            "LEFT JOIN q.qualification_degree_id deg " +
            "WHERE q.emp_id.tempPayrollId = :payrollId AND q.is_active = 1")
    List<EmpQualificationDTO> findQualificationsByPayrollId(@Param("payrollId") String payrollId);

    // --- ENTITY QUERIES (For Logic/Updates) ---

    /**
     * Find by entire Employee object and specific active status
     */
    @Query("SELECT eq FROM EmpQualification eq WHERE eq.emp_id = :employee AND eq.is_active = :isActive")
    List<EmpQualification> findByEmployeeAndActiveStatus(@Param("employee") Employee employee, @Param("isActive") int isActive);
    
    /**
     * Find active qualifications fetching the qualification details.
     * Note: Changed parameter from Optional<Employee> to Employee to prevent JPA binding errors.
     */
    @Query("SELECT eq FROM EmpQualification eq JOIN FETCH eq.qualification_id q WHERE eq.emp_id = :employee AND eq.is_active = 1")
    List<EmpQualification> findActiveQualificationsByEmployee(@Param("employee") Optional<Employee> employee);
 
    /**
     * Find by Permanent PayRollId (matches 'payRollId' in Employee entity)
     */
    @Query("SELECT eq FROM EmpQualification eq " + 
           "WHERE eq.emp_id.payRollId = :payRollId AND eq.is_active = :isActive")
    List<EmpQualification> findByEmp_id_PayRollIdAndIsActive(
        @Param("payRollId") String payRollId, 
        @Param("isActive") int isActive
    );

    /**
     * Find by Temporary Payroll Id
     */
    @Query("SELECT eq FROM EmpQualification eq " + 
            "WHERE eq.emp_id.tempPayrollId = :tempPayrollId AND eq.is_active = :isActive")
     List<EmpQualification> findByEmp_id_TempPayrollIdAndIsActive(
         @Param("tempPayrollId") String tempPayrollId, 
         @Param("isActive") int isActive
     );
    
    /**
     * Find active qualifications by PayrollId (Simple wrapper)
     */
    @Query("SELECT eq FROM EmpQualification eq " +
            "WHERE eq.emp_id.payRollId = :payrollId AND eq.is_active = 1")
     List<EmpQualification> findByEmployeePayrollId(@Param("payrollId") String payrollId);

    // --- THE FIX FOR YOUR ERROR ---
    
    /**
     * Finds active qualifications using just the Employee ID (Integer).
     * Solves: empQualificationRepository.findByEmpIdAndIsActive(empId);
     */
    @Query("SELECT eq FROM EmpQualification eq " +
           "WHERE eq.emp_id.emp_id = :empId AND eq.is_active = 1")
    List<EmpQualification> findByEmpIdAndIsActive(@Param("empId") Integer empId);

}