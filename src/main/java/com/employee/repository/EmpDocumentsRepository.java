package com.employee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.employee.entity.EmpDocuments;

@Repository
public interface EmpDocumentsRepository extends JpaRepository<EmpDocuments, Integer> {
	
	@Query("SELECT ed FROM EmpDocuments ed WHERE ed.emp_id.emp_id = :empId AND ed.is_active = 1")
    List<EmpDocuments> findByEmpIdAndIsActive(@Param("empId") Integer empId);
   
    // Find documents by emp_id and doc_type_id
    @Query("SELECT ed FROM EmpDocuments ed WHERE ed.emp_id.emp_id = :empId AND ed.emp_doc_type_id.doc_type_id = :docTypeId AND ed.is_active = 1")
    List<EmpDocuments> findByEmpIdAndDocTypeId(@Param("empId") Integer empId, @Param("docTypeId") Integer docTypeId);
   
    // Find documents by emp_id and list of doc_type_ids
    @Query("SELECT ed FROM EmpDocuments ed WHERE ed.emp_id.emp_id = :empId AND ed.emp_doc_type_id.doc_type_id IN :docTypeIds AND ed.is_active = 1")
    List<EmpDocuments> findByEmpIdAndDocTypeIds(@Param("empId") Integer empId, @Param("docTypeIds") List<Integer> docTypeIds);

}

