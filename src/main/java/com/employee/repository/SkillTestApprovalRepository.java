package com.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.employee.entity.SkillTestApprovalView;


@Repository
public interface SkillTestApprovalRepository extends JpaRepository<SkillTestApprovalView, String> {
    
    // Spring Data JPA provides the findById(String tempEmployeeId) method automatically.
    // You don't need to declare it.

}