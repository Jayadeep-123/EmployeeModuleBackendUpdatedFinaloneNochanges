package com.employee.repository;
 
import java.util.Optional;
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import com.employee.entity.EmpSalaryInfo;
 
@Repository
public interface EmpSalaryInfoRepository extends JpaRepository<EmpSalaryInfo, Integer> {
 
	// Find by ID and is_active = 1
	@Query("SELECT esi FROM EmpSalaryInfo esi WHERE esi.empSalInfoId = :id AND esi.isActive = :isActive")
	Optional<EmpSalaryInfo> findByIdAndIsActive(@Param("id") Integer id, @Param("isActive") Integer isActive);
	
	// Find by employee ID and is_active = 1
	@Query("SELECT esi FROM EmpSalaryInfo esi WHERE esi.empId.emp_id = :empId AND esi.isActive = :isActive")
	Optional<EmpSalaryInfo> findByEmpIdAndIsActive(@Param("empId") Integer empId, @Param("isActive") Integer isActive);
	
	// Alternative: Find by employee entity and is_active = 1
	@Query("SELECT esi FROM EmpSalaryInfo esi JOIN esi.empId e WHERE e.emp_id = :empId AND esi.isActive = :isActive")
	Optional<EmpSalaryInfo> findByEmployeeIdAndIsActive(@Param("empId") Integer empId, @Param("isActive") Integer isActive);
	
	// Find by payroll ID
	@Query("SELECT esi FROM EmpSalaryInfo esi WHERE esi.payrollId = :payrollId")
	Optional<EmpSalaryInfo> findByPayrollId(@Param("payrollId") String payrollId);
 
}
 