package com.employee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // 1. Add this import
import org.springframework.data.repository.query.Param; // 2. Add this import
import org.springframework.stereotype.Repository;

import com.employee.entity.EmpaddressInfo;
import com.employee.entity.Employee;

@Repository
public interface EmpaddressInfoRepository extends JpaRepository<EmpaddressInfo, Integer> {
	
    /**
     * Finds addresses for an employee based on their payroll ID.
     * CORRECTED: Added @Query because Spring was incorrectly looking for 'empId'
     * instead of the correct 'emp_id' field.
     */
    @Query("SELECT e FROM EmpaddressInfo e WHERE e.emp_id.payRollId = :payrollId") // 3. Add this @Query
	List<EmpaddressInfo> findByEmpId_PayrollId(@Param("payrollId") String payrollId); // 4. Add @Param
    
    @Query("SELECT a FROM EmpaddressInfo a WHERE a.emp_id = :employee")
    List<EmpaddressInfo> findByEmployeeEntity(@Param("employee") Employee employee);
}