package com.employee.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sce_emp_subject", schema = "sce_employee")
public class EmpSubject {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "emp_subject_id")
	private Integer emp_subject_id;
	
	@ManyToOne
	@JoinColumn(name = "emp_id", nullable = false)
	private Employee emp_id;
	
	@Column(name = "orientation_id")
	private Integer orientation_id; // FK to sce_course.sce_orientation (nullable, different schema, using Integer instead of @ManyToOne)
	
	@ManyToOne
	@JoinColumn(name = "subject_id", nullable = false)
	private Subject subject_id; // Required NOT NULL
	
	@Column(name = "agree_no_period", nullable = false)
	private Integer agree_no_period;
	
	@Column(name = "is_active", nullable = false)
	private Integer is_active = 1;
	
	// Audit fields - required NOT NULL columns
	@Column(name = "created_by", nullable = false)
	private Integer created_by; // Will be set from frontend or use entity default
	
	@Column(name = "created_date", nullable = false)
	private Timestamp created_date;
	
	@Column(name = "updated_by")
	private Integer updated_by;
	
	@Column(name = "updated_date")
	private Timestamp updated_date;
	
}

