package com.employee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.employee.dto.GenericDropdownDTO;
import com.employee.entity.Building;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Integer> {
	
	 @Query("SELECT new com.employee.dto.GenericDropdownDTO(b.buildingId, b.buildingName) " +
	           "FROM Building b " +
	           "WHERE b.campusId.campusId = :campusId AND b.isActive = 1")
	    List<GenericDropdownDTO> findBuildingsByCampusId(@Param("campusId") int campusId);
}

