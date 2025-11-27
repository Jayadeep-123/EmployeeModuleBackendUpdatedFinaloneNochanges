package com.employee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.employee.dto.AddressInfoDTO;
import com.employee.dto.AgreementInfoDTO;
import com.employee.dto.BankInfoDTO;
import com.employee.dto.BasicInfoDTO;
import com.employee.dto.CategoryInfoDTO;
import com.employee.dto.DocumentDTO;
import com.employee.dto.FamilyInfoDTO;
import com.employee.dto.PreviousEmployerInfoDTO;
import com.employee.dto.QualificationDTO;
import com.employee.service.EmployeeBasicInfoTabService;
import com.employee.service.EmployeeRemainingTabService;

/**
 * Controller for individual employee onboarding tabs.
 * Provides separate APIs for each tab, allowing step-by-step onboarding.
 * 
 * This controller calls two services:
 * - EmployeeBasicInfoTabService (4 APIs: Basic, Address, Family, Previous Employer)
 * - EmployeeRemainingTabService (5 APIs: Qualification, Documents, Category, Bank, Agreement)
 * 
 * NOTE: EmployeeController remains unchanged - this is an additional controller.
 */
@RestController
@RequestMapping("/api/employee/tab")
public class EmployeeTabController {

    @Autowired
    private EmployeeBasicInfoTabService employeeBasicInfoTabService;

    @Autowired
    private EmployeeRemainingTabService employeeRemainingTabService;

    /**
     * API 1: Save Basic Info (Tab 1)
     * POST /api/employee/tab/basic-info
     * 
     * @param basicInfo Basic Info DTO (contains empId and tempPayrollId)
     * @return Saved BasicInfoDTO with empId
     */
    @PostMapping("/basic-info")
    public ResponseEntity<BasicInfoDTO> saveBasicInfo(
            @RequestBody BasicInfoDTO basicInfo) {
        BasicInfoDTO response = employeeBasicInfoTabService.saveBasicInfo(basicInfo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * API 2: Save Address Info (Tab 2)
     * POST /api/employee/tab/address-info
     * 
     * @param tempPayrollId Temp Payroll ID
     * @param addressInfo Address Info DTO
     * @return Saved AddressInfoDTO object
     */
    @PostMapping("/address-info")
    public ResponseEntity<AddressInfoDTO> saveAddressInfo(
            @RequestParam String tempPayrollId,
            @RequestBody AddressInfoDTO addressInfo) {
        AddressInfoDTO response = employeeBasicInfoTabService.saveAddressInfo(tempPayrollId, addressInfo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * API 3: Save Family Info (Tab 3)
     * POST /api/employee/tab/family-info
     * 
     * @param tempPayrollId Temp Payroll ID
     * @param familyInfo Family Info DTO
     * @return Saved FamilyInfoDTO object
     */
    @PostMapping("/family-info")
    public ResponseEntity<FamilyInfoDTO> saveFamilyInfo(
            @RequestParam String tempPayrollId,
            @RequestBody FamilyInfoDTO familyInfo) {
        FamilyInfoDTO response = employeeBasicInfoTabService.saveFamilyInfo(tempPayrollId, familyInfo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * API 4: Save Previous Employer Info (Tab 4)
     * POST /api/employee/tab/previous-employer
     * 
     * @param tempPayrollId Temp Payroll ID
     * @param previousEmployerInfo Previous Employer Info DTO
     * @return Saved PreviousEmployerInfoDTO object
     */
    @PostMapping("/previous-employer")
    public ResponseEntity<PreviousEmployerInfoDTO> savePreviousEmployerInfo(
            @RequestParam String tempPayrollId,
            @RequestBody PreviousEmployerInfoDTO previousEmployerInfo) {
        PreviousEmployerInfoDTO response = employeeBasicInfoTabService.savePreviousEmployerInfo(tempPayrollId, previousEmployerInfo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * API 5: Save Qualification (Tab 5)
     * POST /api/employee/tab/qualification
     * 
     * @param tempPayrollId Temp Payroll ID
     * @param qualification Qualification DTO
     * @return Saved QualificationDTO object
     */
    @PostMapping("/qualification")
    public ResponseEntity<QualificationDTO> saveQualification(
            @RequestParam String tempPayrollId,
            @RequestBody QualificationDTO qualification) {
        QualificationDTO response = employeeRemainingTabService.saveQualification(tempPayrollId, qualification);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * API 6: Save Documents (Tab 6)
     * POST /api/employee/tab/documents
     * 
     * @param tempPayrollId Temp Payroll ID
     * @param documents Document DTO
     * @return Saved DocumentDTO object
     */
    @PostMapping("/documents")
    public ResponseEntity<DocumentDTO> saveDocuments(
            @RequestParam String tempPayrollId,
            @RequestBody DocumentDTO documents) {
        DocumentDTO response = employeeRemainingTabService.saveDocuments(tempPayrollId, documents);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * API 7: Save Category Info (Tab 7)
     * POST /api/employee/tab/category-info
     * 
     * @param tempPayrollId Temp Payroll ID
     * @param categoryInfo Category Info DTO
     * @return Saved CategoryInfoDTO object
     */
    @PostMapping("/category-info")
    public ResponseEntity<CategoryInfoDTO> saveCategoryInfo(
            @RequestParam String tempPayrollId,
            @RequestBody CategoryInfoDTO categoryInfo) {
        CategoryInfoDTO response = employeeRemainingTabService.saveCategoryInfo(tempPayrollId, categoryInfo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * API 8: Save Bank Info (Tab 8)
     * POST /api/employee/tab/bank-info
     * 
     * @param tempPayrollId Temp Payroll ID
     * @param bankInfo Bank Info DTO
     * @return Saved BankInfoDTO object
     */
    @PostMapping("/bank-info")
    public ResponseEntity<BankInfoDTO> saveBankInfo(
            @RequestParam String tempPayrollId,
            @RequestBody BankInfoDTO bankInfo) {
        BankInfoDTO response = employeeRemainingTabService.saveBankInfo(tempPayrollId, bankInfo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * API 9: Save Agreement Info (Tab 9)
     * POST /api/employee/tab/agreement-info
     * 
     * @param tempPayrollId Temp Payroll ID
     * @param agreementInfo Agreement Info DTO
     * @return Saved AgreementInfoDTO object
     */
    @PostMapping("/agreement-info")
    public ResponseEntity<AgreementInfoDTO> saveAgreementInfo(
            @RequestParam String tempPayrollId,
            @RequestBody AgreementInfoDTO agreementInfo) {
        AgreementInfoDTO response = employeeRemainingTabService.saveAgreementInfo(tempPayrollId, agreementInfo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

