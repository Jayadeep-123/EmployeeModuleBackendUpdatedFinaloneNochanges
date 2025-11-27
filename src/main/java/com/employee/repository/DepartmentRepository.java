package com.employee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.employee.entity.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {

	// Find by ID and is_active = 1
	@Query("SELECT d FROM Department d WHERE d.department_id = :id AND d.isActive = :isActive")
	Optional<Department> findByIdAndIsActive(@Param("id") Integer id, @Param("isActive") Integer isActive);

	
    /**
     * Finds all active departments that are associated with a specific employee type.
     * CORRECTED: This now uses a manual @Query because Spring cannot
     * auto-generate this query from the method name.
     */
    @Query("SELECT DISTINCT e.department FROM Employee e " +
           "WHERE e.employee_type_id.emp_type_id = :empTypeId " +
           "AND e.department.isActive = :isActive")
	List<Department> findByEmpTypeId_EmpTypeIdAndIsActive(
        @Param("empTypeId") int empTypeId, 
        @Param("isActive") Integer isActive
    );

}