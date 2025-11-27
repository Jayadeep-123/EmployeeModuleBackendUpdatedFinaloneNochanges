# Employee Onboarding - DTO to Entity Mapping

This document maps each section of the `EmployeeOnboardingDTO` POST object to the corresponding entities that are saved in the database.

---

## POST Object Structure → Entity Mapping

### 1. **basicInfo** → Multiple Entities

**Entities Used:**
- ✅ **Employee** (Main employee record)
  - Fields: `emp_id`, `first_name`, `last_name`, `date_of_join`, `primary_mobile_no`, `email`, `user_name`, `password`, `is_active`, `status`, `contract_start_date`, `contract_end_date`
  - Foreign Keys: `gender_id`, `reference_emp_id`, `hired_by_emp_id`, `designation_id`, `department_id`, `manager_id`, `category_id`, `reporting_manager_id`, `emp_type_id`, `qualification_id`, `emp_work_mode_id`, `replaced_by_emp_id`, `join_type_id`, `mode_of_hiring_id`, `emp_check_list_status_id`
  - Table: `sce_employee.sce_emp`

- ✅ **EmpDetails** (Personal details)
  - Fields: `emp_detl_id`, `adhaar_name`, `date_of_birth`, `emergency_ph_no`, `personal_email`, `adhaar_no`, `pancard_no`, `adhaar_enrolment_no`, `is_active`, `status`
  - Foreign Keys: `emp_id`, `blood_group_id`, `caste_id`, `religion_id`, `marital_status_id`
  - Table: `sce_employee.sce_emp_detl`

- ✅ **EmpPfDetails** (PF/ESI/UAN information)
  - Fields: `emp_pf_esi_uan_info_id`, `pf_no`, `pf_join_date`, `pre_uan_num`, `uan_no`, `pre_esi_num`, `esi_no`, `is_active`
  - Foreign Keys: `emp_id`
  - Table: `sce_employee.sce_emp_pf_esi_uan_info`

**Field Mapping:**
- `firstName` → `Employee.first_name`
- `lastName` → `Employee.last_name`
- `dateOfJoin` → `Employee.date_of_join`
- `primaryMobileNo` → `Employee.primary_mobile_no`
- `email` → `Employee.email` and `EmpDetails.personal_email`
- `genderId` → `Employee.gender_id`
- `referenceEmpId` → `Employee.reference_emp_id`
- `hiredByEmpId` → `Employee.hired_by_emp_id`
- `designationId` → `Employee.designation_id`
- `departmentId` → `Employee.department_id`
- `managerId` → `Employee.manager_id`
- `categoryId` → `Employee.category_id`
- `reportingManagerId` → `Employee.reporting_manager_id`
- `empTypeId` → `Employee.emp_type_id`
- `qualificationId` → `Employee.qualification_id`
- `empWorkModeId` → `Employee.emp_work_mode_id`
- `replacedByEmpId` → `Employee.replaced_by_emp_id`
- `joinTypeId` → `Employee.join_type_id`
- `modeOfHiringId` → `Employee.mode_of_hiring_id`
- `adhaarName` → `EmpDetails.adhaar_name`
- `dateOfBirth` → `EmpDetails.date_of_birth`
- `aadharNum` → `EmpDetails.adhaar_no`
- `aadharEnrolmentNum` → `EmpDetails.adhaar_enrolment_no`
- `pancardNum` → `EmpDetails.pancard_no`
- `bloodGroupId` → `EmpDetails.blood_group_id`
- `casteId` → `EmpDetails.caste_id`
- `religionId` → `EmpDetails.religion_id`
- `maritalStatusId` → `EmpDetails.marital_status_id`
- `pfNo` → `EmpPfDetails.pf_no`
- `pfJoinDate` → `EmpPfDetails.pf_join_date`
- `preUanNum` → `EmpPfDetails.pre_uan_num`
- `uanNo` → `EmpPfDetails.uan_no`
- `preEsiNum` → `EmpPfDetails.pre_esi_num`
- `esiNo` → `EmpPfDetails.esi_no`

**Note:** Fields like `campusId`, `campusCode`, `campusTypeId`, `locationId`, `buildingId`, `fatherName`, `totalExperience`, `profilePicture`, `sscNo`, `sscNotAvailable` are currently not mapped to any entity (may need to be added to entities or stored separately).

---

### 2. **addressInfo** → Entity

**Entity Used:**
- ✅ **EmpaddressInfo** (Address records - 2 records created)
  - Fields: `emp_addrs_id`, `house_no`, `landmark`, `postal_code`, `emrg_contact_no`, `addrs_type`, `is_active`
  - Foreign Keys: `emp_id`, `country_id`, `state_id`, `city_id`
  - Table: `sce_employee.sce_emp_addrs`
  - **Records Created:** 
    - 1 record for `currentAddress` with `addrs_type = "CURR"`
    - 1 record for `permanentAddress` with `addrs_type = "PERM"` (uses `currentAddress` data if `permanentAddressSameAsCurrent = true`)

**Field Mapping:**
- `currentAddress.name` → Not directly mapped (may be stored in `house_no` or `landmark`)
- `currentAddress.addressLine1` → `EmpaddressInfo.house_no`
- `currentAddress.addressLine2` → `EmpaddressInfo.landmark`
- `currentAddress.addressLine3` → Not directly mapped
- `currentAddress.pin` → `EmpaddressInfo.postal_code`
- `currentAddress.cityId` → `EmpaddressInfo.city_id`
- `currentAddress.stateId` → `EmpaddressInfo.state_id`
- `currentAddress.countryId` → `EmpaddressInfo.country_id`
- `currentAddress.phoneNumber` → `EmpaddressInfo.emrg_contact_no`
- `permanentAddress` → Same mapping as `currentAddress` (if `permanentAddressSameAsCurrent = false`)

---

### 3. **familyInfo** → Entity

**Entity Used:**
- ✅ **EmpFamilyDetails** (Family member records - Multiple records created)
  - Fields: `emp_family_detl_id`, `first_name`, `last_name`, `occupation`, `nationality`, `is_late`, `is_dependent`, `date_of_birth`, `is_active`
  - Foreign Keys: `emp_id`, `gender_id`, `blood_group_id`, `relation_id`
  - Table: `sce_employee.sce_emp_family_detl`
  - **Records Created:** One record per item in `familyMembers` array

**Field Mapping:**
- `familyMembers[].firstName` → `EmpFamilyDetails.first_name`
- `familyMembers[].lastName` → `EmpFamilyDetails.last_name`
- `familyMembers[].isLate` → `EmpFamilyDetails.is_late` ("Y" or "N")
- `familyMembers[].occupation` → `EmpFamilyDetails.occupation`
- `familyMembers[].genderId` → `EmpFamilyDetails.gender_id`
- `familyMembers[].bloodGroupId` → `EmpFamilyDetails.blood_group_id`
- `familyMembers[].nationality` → `EmpFamilyDetails.nationality`
- `familyMembers[].relationId` → `EmpFamilyDetails.relation_id`

**Note:** Fields like `email` and `phoneNumber` from `familyMembers` are currently not mapped to any entity field.

---

### 4. **previousEmployerInfo** → Entity

**Entity Used:**
- ✅ **EmpExperienceDetails** (Previous employer records - Multiple records created)
  - Fields: `emp_exp_detl_id`, `designation`, `nature_of_duties`, `company_name`, `company_addr`, `from_date`, `to_date`, `leaving_reason`, `gross_salary_per_month`, `ctc`, `pre_chaitanya_id`, `is_active`
  - Foreign Keys: `emp_id`
  - Table: `sce_employee.sce_emp_exp_detl`
  - **Records Created:** One record per item in `previousEmployers` array

**Field Mapping:**
- `previousEmployers[].companyName` → `EmpExperienceDetails.company_name`
- `previousEmployers[].designation` → `EmpExperienceDetails.designation`
- `previousEmployers[].fromDate` → `EmpExperienceDetails.from_date`
- `previousEmployers[].toDate` → `EmpExperienceDetails.to_date`
- `previousEmployers[].leavingReason` → `EmpExperienceDetails.leaving_reason`
- `previousEmployers[].companyAddressLine1` → `EmpExperienceDetails.company_addr` (combined with addressLine2)
- `previousEmployers[].companyAddressLine2` → Combined with `company_addr`
- `previousEmployers[].natureOfDuties` → `EmpExperienceDetails.nature_of_duties`
- `previousEmployers[].grossSalaryPerMonth` → `EmpExperienceDetails.gross_salary_per_month`
- `previousEmployers[].ctc` → `EmpExperienceDetails.ctc`
- `previousEmployers[].preChaitanyaId` → `EmpExperienceDetails.pre_chaitanya_id`

---

### 5. **qualification** → Entity

**Entity Used:**
- ✅ **EmpQualification** (Qualification records - Multiple records created)
  - Fields: `emp_qualification_id`, `specialization`, `university`, `institute`, `passed_out_year`, `certificate_file`, `is_highest`, `is_active`
  - Foreign Keys: `emp_id`, `qualification_id`, `qualification_degree_id`
  - Table: `sce_employee.sce_emp_qualification`
  - **Records Created:** One record per item in `qualifications` array

**Field Mapping:**
- `qualifications[].qualificationId` → `EmpQualification.qualification_id`
- `qualifications[].qualificationDegreeId` → `EmpQualification.qualification_degree_id`
- `qualifications[].specialization` → `EmpQualification.specialization`
- `qualifications[].university` → `EmpQualification.university`
- `qualifications[].institute` → `EmpQualification.institute`
- `qualifications[].passedOutYear` → `EmpQualification.passed_out_year`
- `qualifications[].certificateFile` → `EmpQualification.certificate_file`
- `qualifications[].isHighest` → `EmpQualification.is_highest`

**Note:** If `isHighest = true`, the `qualification_id` from this record is also updated in the `Employee` entity.

---

### 6. **documents** → Entity

**Entity Used:**
- ✅ **EmpDocuments** (Document records - Multiple records created)
  - Fields: `emp_doc_id`, `doc_path`, `ssc_no`, `is_verified`, `is_active`
  - Foreign Keys: `emp_id`, `emp_doc_type_id`
  - Table: `sce_employee.sce_emp_documents`
  - **Records Created:** One record per item in `documents` array

**Field Mapping:**
- `documents[].docTypeId` → `EmpDocuments.emp_doc_type_id`
- `documents[].docPath` → `EmpDocuments.doc_path`
- `documents[].sscNo` → `EmpDocuments.ssc_no`
- `documents[].isVerified` → `EmpDocuments.is_verified` (1 or 0)

---

### 7. **categoryInfo** → Entity (Update)

**Entity Used:**
- ✅ **Employee** (Updates existing Employee record)
  - Fields Updated: `emp_type_id`, `department_id`, `designation_id`
  - Table: `sce_employee.sce_emp`

**Field Mapping:**
- `categoryInfo.employeeTypeId` → `Employee.emp_type_id`
- `categoryInfo.departmentId` → `Employee.department_id`
- `categoryInfo.designationId` → `Employee.designation_id`

**Note:** Fields like `subjectId` and `agreedPeriodsPerWeek` are currently not mapped to any entity field.

---

### 8. **bankInfo** → Entity

**Entity Used:**
- ✅ **BankDetails** (Bank account records - 1-2 records created)
  - Fields: `emp_sal_bank_detl_id`, `acc_type`, `bank_name`, `bank_branch`, `bank_holder_name`, `acc_no`, `ifsc_code`, `net_payable`, `bank_statement_cheque_path`, `is_active`
  - Foreign Keys: `emp_id`
  - Table: `sce_employee.sce_emp_bank_detl`
  - **Records Created:** 
    - 1 record for `personalAccount` with `acc_type = "PERSONAL"` (if provided)
    - 1 record for `salaryAccount` with `acc_type = "SALARY"` (if provided)

**Field Mapping:**
- `bankInfo.personalAccount.bankName` → `BankDetails.bank_name`
- `bankInfo.personalAccount.accountNo` → `BankDetails.acc_no`
- `bankInfo.personalAccount.accountHolderName` → `BankDetails.bank_holder_name`
- `bankInfo.personalAccount.ifscCode` → `BankDetails.ifsc_code`
- `bankInfo.salaryAccount.bankId` → Not directly mapped (may need to fetch `OrgBank` entity)
- `bankInfo.salaryAccount.accountNo` → `BankDetails.acc_no`
- `bankInfo.salaryAccount.accountHolderName` → `BankDetails.bank_holder_name` (falls back to employee name if not provided)
- `bankInfo.salaryAccount.ifscCode` → `BankDetails.ifsc_code`
- `bankInfo.salaryAccount.payableAt` → `BankDetails.bank_branch`

**Note:** Fields like `paymentType` and `salaryLessThan40000` are currently not mapped to any entity field.

---

## Summary Table

| POST Section | Entity/Entities | Records Created | Table Name |
|--------------|----------------|-----------------|------------|
| `basicInfo` | `Employee`, `EmpDetails`, `EmpPfDetails` | 3 records | `sce_emp`, `sce_emp_detl`, `sce_emp_pf_esi_uan_info` |
| `addressInfo` | `EmpaddressInfo` | 2 records | `sce_emp_addrs` |
| `familyInfo` | `EmpFamilyDetails` | N records (one per family member) | `sce_emp_family_detl` |
| `previousEmployerInfo` | `EmpExperienceDetails` | N records (one per employer) | `sce_emp_exp_detl` |
| `qualification` | `EmpQualification` | N records (one per qualification) | `sce_emp_qualification` |
| `documents` | `EmpDocuments` | N records (one per document) | `sce_emp_documents` |
| `categoryInfo` | `Employee` (update) | 0 records (updates existing) | `sce_emp` |
| `bankInfo` | `BankDetails` | 1-2 records (personal + salary) | `sce_emp_bank_detl` |

---

## Notes

1. **Audit Fields:** All entities (except `Employee`) have audit fields: `created_by`, `created_date`, `updated_by`, `updated_date`
2. **Foreign Keys:** All entities have `emp_id` as foreign key referencing `sce_emp.emp_id`
3. **Active Status:** All entities have `is_active` field (default: 1)
4. **Unmapped Fields:** Some fields from the POST object are not currently mapped to any entity (e.g., `campusId`, `campusCode`, `fatherName`, `totalExperience`, `profilePicture`, `subjectId`, `agreedPeriodsPerWeek`, `paymentType`, `salaryLessThan40000`)

