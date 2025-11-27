
package com.employee.controller;


import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.employee.dto.AddressResponseDTO;
import com.employee.dto.EducationalDocumentStatusDTO;
import com.employee.dto.EmpExperienceDetailsDTO;
import com.employee.dto.EmpFamilyDetailsDTO;
import com.employee.dto.EmployeeAgreementDetailsDto;
import com.employee.dto.EmployeeBankDetailsResponseDTO;
import com.employee.dto.EmployeeCampusInfoDTO;
import com.employee.dto.EmployeeCurrentInfoDTO;
import com.employee.dto.EmployeeRelationDTO;
import com.employee.dto.FamilyMemberInOrgDTO;
import com.employee.entity.EmployeeBasicInfoView;
//import com.employee.service.EmpDocTypeService;
import com.employee.service.HREmpDetlService;

@RestController
@RequestMapping("empDetails/HR")
public class HREmpDetailsController {
	
	@Autowired HREmpDetlService hrEmpDetlService;
	
//	@Autowired
//	private EmpDocTypeService empDocTypeService;

	
	
	@GetMapping("/FamilyDetails/by-payroll/{payrollId}")
    public List<EmpFamilyDetailsDTO> getFamilyMembers(@PathVariable String payrollId) {
        return hrEmpDetlService.getFamilyMembersByPayrollId(payrollId);
    }
	
    @GetMapping("/AddressDetl/{payrollId}")
    public ResponseEntity<Map<String, List<AddressResponseDTO>>> getAddressByPayrollId(
            @PathVariable String payrollId) {
        Map<String, List<AddressResponseDTO>> response = hrEmpDetlService.getAddressByPayrollIdGrouped(payrollId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/family-members-in-org/{payrollId}")
    public ResponseEntity<List<FamilyMemberInOrgDTO>> getFamilyMembersInOrg(@PathVariable String payrollId) {
        List<FamilyMemberInOrgDTO> response = hrEmpDetlService.getFamilyMembersInOrganization(payrollId);
        return ResponseEntity.ok(response);
    }
   
    
    @GetMapping("/manager/{payrollId}")
    public EmployeeRelationDTO getManagerDetails(@PathVariable String payrollId) {
        return hrEmpDetlService.getManagerDetails(payrollId);
    }

    @GetMapping("/reference/{payrollId}")
    public EmployeeRelationDTO getReferenceDetails(@PathVariable String payrollId) {
        return hrEmpDetlService.getReferenceDetails(payrollId);
    }

    @GetMapping("/reporting-manager/{payrollId}")
    public EmployeeRelationDTO getReportingManagerDetails(@PathVariable String payrollId) {
        return hrEmpDetlService.getReportingManagerDetails(payrollId);
    }
    
    @GetMapping("/BankDetails/{payrollId}")
    public EmployeeBankDetailsResponseDTO getBankDetails(@PathVariable String payrollId) {
        return hrEmpDetlService.getBankDetailsByPayrollId(payrollId);
    }
    
    @GetMapping("/current-info/{payrollId}")
    public EmployeeCurrentInfoDTO getEmployeeCurrentInfo(@PathVariable String payrollId) {
        return hrEmpDetlService.getCurrentInfoByPayrollId(payrollId);
    }
    
    @GetMapping("/experience/{payrollId}")
    public List<EmpExperienceDetailsDTO> getEmployeeExperienceDetails(@PathVariable String payrollId) {
        return hrEmpDetlService.getEmployeeExperienceByPayrollId(payrollId);
    }
    
    @GetMapping("/basicInfo/{payrollId}")
   public Optional<EmployeeBasicInfoView>  getBasicInfoByPayrollId(@PathVariable String payrollId)
   {
        return hrEmpDetlService.getBasicInfoByPayrollId(payrollId);
   }
    
    @GetMapping("/AgreementDetails/{payrollId}")
    public ResponseEntity<EmployeeAgreementDetailsDto> getChequeDetails(@PathVariable String payrollId) {
    	EmployeeAgreementDetailsDto response = hrEmpDetlService.getChequeDetailsByPayrollId(payrollId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/campus-info/{payrollId}")
    public ResponseEntity<EmployeeCampusInfoDTO> getEmployeeCampusInfo(
            @PathVariable String payrollId) {
        
        EmployeeCampusInfoDTO campusInfo = hrEmpDetlService.getEmployeeCampusInfo(payrollId);
        return ResponseEntity.ok(campusInfo);
    }
    
    @GetMapping("/QualificationNamesList/{payrollId}")
    public ResponseEntity<List<String>> getQualificationNames(@PathVariable String payrollId) {
        List<String> names = hrEmpDetlService.getQualificationNamesByPayrollId(payrollId);
        return ResponseEntity.ok(names);
    }


    @GetMapping("/qualifications/{payrollId}")
    public ResponseEntity<?> getEmployeeQualifications(@PathVariable String payrollId) {
        return ResponseEntity.ok(hrEmpDetlService.getQualificationsByPayrollId(payrollId));
    }
    @GetMapping("/educational-documents-status")
    public ResponseEntity<EducationalDocumentStatusDTO> getEducationalDocumentsStatus(@RequestParam String payrollId) {
        EducationalDocumentStatusDTO result = hrEmpDetlService.getEducationalDocumentsStatusByPayrollId(payrollId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @GetMapping("/uploaded-educational-documents")
    public ResponseEntity<List<EducationalDocumentStatusDTO.DocumentStatusDTO>> getUploadedEducationalDocuments(@RequestParam String payrollId) {
        List<EducationalDocumentStatusDTO.DocumentStatusDTO> result = hrEmpDetlService.getUploadedEducationalDocuments(payrollId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @GetMapping("/missing-educational-documents")
    public ResponseEntity<List<EducationalDocumentStatusDTO.DocumentStatusDTO>> getMissingEducationalDocuments(@RequestParam String payrollId) {
        List<EducationalDocumentStatusDTO.DocumentStatusDTO> result = hrEmpDetlService.getMissingEducationalDocuments(payrollId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @GetMapping("/uploaded-id-proof-documents")
    public ResponseEntity<List<EducationalDocumentStatusDTO.DocumentStatusDTO>> getUploadedIdProofDocuments(@RequestParam String payrollId) {
        List<EducationalDocumentStatusDTO.DocumentStatusDTO> result = hrEmpDetlService.getUploadedIdProofDocuments(payrollId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @GetMapping("/missing-id-proof-documents")
    public ResponseEntity<List<EducationalDocumentStatusDTO.DocumentStatusDTO>> getMissingIdProofDocuments(@RequestParam String payrollId) {
        List<EducationalDocumentStatusDTO.DocumentStatusDTO> result = hrEmpDetlService.getMissingIdProofDocuments(payrollId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @GetMapping("/uploaded-specific-documents")
    public ResponseEntity<List<EducationalDocumentStatusDTO.DocumentStatusDTO>> getUploadedSpecificDocuments(@RequestParam String payrollId) {
        List<EducationalDocumentStatusDTO.DocumentStatusDTO> result = hrEmpDetlService.getUploadedSpecificDocuments(payrollId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @GetMapping("/missing-specific-documents")
    public ResponseEntity<List<EducationalDocumentStatusDTO.DocumentStatusDTO>> getMissingSpecificDocuments(@RequestParam String payrollId) {
        List<EducationalDocumentStatusDTO.DocumentStatusDTO> result = hrEmpDetlService.getMissingSpecificDocuments(payrollId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    
    
   
    
    
}
