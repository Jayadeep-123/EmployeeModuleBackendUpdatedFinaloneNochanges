package com.employee.repository;
 
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import com.employee.entity.EmpQualification;
import com.employee.entity.Employee;
 
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
 
    @Query("SELECT e FROM Employee e WHERE e.emp_id = :id AND e.is_active = :is_active")
    Optional<Employee> findByIdAndIs_active(@Param("id") Integer id, @Param("is_active") int is_active);
    
    Optional<Employee> findByTempPayrollId(String tempPayrollId);
    
    /**
     * Finds an employee by the 'payRollId' field.
     * Note: This method name relies on Spring Data naming conventions.
     * If your field is 'payRollId', this works automatically.
     */
    Optional<Employee> findByPayRollId(String payrollId);

    /**
     * Finds all employees by their active status.
     */
    @Query("SELECT e FROM Employee e WHERE e.is_active = :status")
    List<Employee> findByIsActive(@Param("status") int status);
 
    @Query("SELECT e FROM Employee e "
            + "LEFT JOIN FETCH e.campus_id c "
            + "LEFT JOIN FETCH c.city " 
            + "LEFT JOIN FETCH e.employee_manager_id m " 
            + "LEFT JOIN FETCH e.employee_replaceby_id r " 
            + "LEFT JOIN FETCH e.employee_hired h " 
            + "LEFT JOIN FETCH e.modeOfHiring_id moh " 
            + "LEFT JOIN FETCH e.workingMode_id wm " 
            + "LEFT JOIN FETCH e.join_type_id jat " 
            + "WHERE e.tempPayrollId = :tempPayrollId")
       Optional<Employee> findWorkingInfoByTempPayrollId(@Param("tempPayrollId") String tempPayrollId);
    
    
    @Query("SELECT e FROM Employee e "
            + "LEFT JOIN FETCH e.qualification_id q " 
            + "WHERE e.tempPayrollId = :tempPayrollId")
       Optional<Employee> findHighestQualificationDetailsByTempPayrollId(@Param("tempPayrollId") String tempPayrollId);
       
       @Query("SELECT eq FROM EmpQualification eq "
       	 + "JOIN eq.emp_id e "
       	 + "LEFT JOIN FETCH eq.qualification_degree_id qd "
       	 + "WHERE e.tempPayrollId = :tempPayrollId AND eq.qualification_id.qualification_id = e.qualification_id.qualification_id")
       Optional<EmpQualification> findHighestEmpQualificationRecord(@Param("tempPayrollId") String tempPayrollId);

       
       @Query("SELECT e FROM Employee e JOIN FETCH e.designation d "
               + "WHERE e.department.department_id = :departmentId "
               + "AND e.campus_id.campusId = :campusId " 
               + "AND e.is_active = 1")
          List<Employee> findActiveEmployeesByDepartmentAndCampus(
                  @Param("departmentId") int departmentId,
                  @Param("campusId") int campusId);
       
       @Query("SELECT e FROM Employee e "
               + "JOIN FETCH e.designation d "
               + "JOIN FETCH e.gender g "
               + "WHERE e.campus_id.campusId = :campusId AND e.is_active = 1")
          List<Employee> findActiveEmployeesByCampusId(@Param("campusId") int campusId);

       @Query("SELECT e FROM Employee e WHERE e.primary_mobile_no = :mobileNo AND e.emp_id != :empId")
       Optional<Employee> findByPrimary_mobile_noExcludingEmpId(@Param("mobileNo") Long mobileNo, @Param("empId") Integer empId);
       
       @Query("SELECT MAX(e.tempPayrollId) FROM Employee e WHERE e.tempPayrollId LIKE :keyPrefix")
       String findMaxTempPayrollIdByKey(@Param("keyPrefix") String keyPrefix);
       
       @Query("SELECT e FROM Employee e WHERE e.primary_mobile_no = :mobileNo")
       Optional<Employee> findByPrimary_mobile_no(@Param("mobileNo") Long mobileNo);
       
       // --- THE FIX IS HERE ---
       // 1. Changed 'e.payroll_id' (DB Column) to 'e.payRollId' (Java Variable)
       // 2. Ensure this matches your Employee.java field exactly.
       @Query("SELECT e FROM Employee e WHERE e.payRollId = :payrollId")
       Optional<Employee> findByPayrollId(@Param("payrollId") String payrollId);

       // Employee Search Methods - Flexible search by cityId, employeeTypeId, payrollId
       @Query("SELECT e FROM Employee e "
               + "JOIN FETCH e.department d "
               + "JOIN FETCH e.campus_id c "
               + "JOIN FETCH c.city city "
               + "WHERE city.cityId = :cityId AND e.payRollId = :payrollId AND e.is_active = 1")
       List<Employee> findByCityIdAndPayrollId(@Param("cityId") Integer cityId, @Param("payrollId") String payrollId);

       @Query("SELECT e FROM Employee e "
               + "JOIN FETCH e.department d "
               + "JOIN FETCH e.campus_id c "
               + "JOIN FETCH c.city city "
               + "JOIN FETCH e.employee_type_id et "
               + "WHERE city.cityId = :cityId AND et.emp_type_id = :employeeTypeId AND e.payRollId = :payrollId AND e.is_active = 1")
       List<Employee> findByCityIdAndEmployeeTypeIdAndPayrollId(
               @Param("cityId") Integer cityId, 
               @Param("employeeTypeId") Integer employeeTypeId, 
               @Param("payrollId") String payrollId);

       @Query("SELECT e FROM Employee e "
               + "JOIN FETCH e.department d "
               + "JOIN FETCH e.campus_id c "
               + "JOIN FETCH c.city city "
               + "WHERE city.cityId = :cityId AND e.is_active = 1")
       List<Employee> findByCityId(@Param("cityId") Integer cityId);

       @Query("SELECT e FROM Employee e "
               + "JOIN FETCH e.department d "
               + "JOIN FETCH e.campus_id c "
               + "JOIN FETCH c.city city "
               + "JOIN FETCH e.employee_type_id et "
               + "WHERE city.cityId = :cityId AND et.emp_type_id = :employeeTypeId AND e.is_active = 1")
       List<Employee> findByCityIdAndEmployeeTypeId(
               @Param("cityId") Integer cityId, 
               @Param("employeeTypeId") Integer employeeTypeId);

       @Query("SELECT e FROM Employee e "
               + "JOIN FETCH e.department d "
               + "JOIN FETCH e.employee_type_id et "
               + "WHERE et.emp_type_id = :employeeTypeId AND e.payRollId = :payrollId AND e.is_active = 1")
       List<Employee> findByEmployeeTypeIdAndPayrollId(
               @Param("employeeTypeId") Integer employeeTypeId, 
               @Param("payrollId") String payrollId);

       @Query("SELECT e FROM Employee e "
               + "JOIN FETCH e.department d "
               + "JOIN FETCH e.employee_type_id et "
               + "WHERE et.emp_type_id = :employeeTypeId AND e.is_active = 1")
       List<Employee> findByEmployeeTypeId(@Param("employeeTypeId") Integer employeeTypeId);

}