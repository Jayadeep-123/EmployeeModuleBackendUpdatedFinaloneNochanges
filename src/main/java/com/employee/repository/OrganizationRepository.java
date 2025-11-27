package com.employee.repository;
 
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import com.employee.entity.Employee;
import com.employee.entity.Organization;

import jakarta.persistence.LockModeType;

 

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
    List<Organization> findByIsActive(int isActive);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    Optional<Organization> findById(Integer orgId);
}