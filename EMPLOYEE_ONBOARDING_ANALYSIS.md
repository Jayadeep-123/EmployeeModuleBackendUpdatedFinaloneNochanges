# Employee Onboarding - Complete Analysis (All 8 Tabs)

## Overview
The onboarding process has **8 tabs**, and all data from all tabs will be submitted via **ONE POST method** using `EmployeeOnboardingDTO`.

---

## Complete Tab-by-Tab Analysis

### **Tab 1: Basic Info**
**DTO:** `BasicInfoDTO` ✅ (Already Created)

**Entities Used:**
1. **Employee** - Main employee record
   - Fields: `first_name`, `last_name`, `date_of_join`, `primary_mobile_no`, `email`, `passout_year`
   - Foreign Keys: `gender_id`, `designation_id`, `department_id`, `manager_id`, `category_id`, `emp_type_id`, `qualification_id`, `emp_work_mode_id`, `join_type_id`, `mode_of_hiring_id`, `reference_emp_id`, `hired_by_emp_id`, `replaced_by_emp_id`, `reporting_manager_id`, `check_list_status_id`

2. **EmpDetails** - Additional personal details
   - Fields: `adhaar_name`, `date_of_birth`, `emergency_ph_no`, `email`, `aadhar_num`, `pancard_num`, `specialization`, `adhaar_enrolment_num`, `passout_year`
   - Foreign Keys: `blood_group_id`, `caste_id`, `religion_id`, `marital_status_id`, `emp_id`, `emp_pf_esi_uan_info_id`

3. **EmpPfDetails** - PF/ESI/UAN information
   - Fields: `pf_no`, `pf_join_date`, `pre_uan_no`, `uan_no`
   - Foreign Key: `emp_id`

**Form Fields Mapping:**
- First Name, Surname → `Employee.first_name`, `Employee.last_name`
- Gender → `Employee.gender_id`
- Aadhaar No, Aadhaar Enrollment No → `EmpDetails.aadhar_num`, `EmpDetails.adhaar_enrolment_num`
- PAN Number → `EmpDetails.pancard_num`
- Date of Birth → `EmpDetails.date_of_birth`
- Blood Group → `EmpDetails.blood_group_id`
- SSC No → Store in `EmpDetails` or `Employee` (may need to add field)
- Category → `Employee.category_id`
- Email → `Employee.email` AND `EmpDetails.email`
- Phone Number → `Employee.primary_mobile_no`
- Father Name → May need to add to `EmpDetails` or handle separately
- Marital Status → `EmpDetails.marital_status_id`
- Previous ESI No → `EmpPfDetails.pre_uan_no` (or create separate ESI field)
- Previous UAN No → `EmpPfDetails.pre_uan_no`
- Highest Qualification → `Employee.qualification_id`
- Total Experience → May need to add to `Employee` or `EmpDetails`
- Campus, Campus Code, Campus Type, Location, Building, Manager → `Employee` foreign keys
- Profile Picture → Store as file path/URL (may need to add `profile_picture` field to `Employee`)

---

### **Tab 2: Address Info**
**DTO:** `AddressInfoDTO` ✅ (Already Created)

**Entities Used:**
1. **EmpaddressInfo** - Address records (TWO records needed)
   - Fields: `addrs_type`, `house_no`, `landmark`, `postal_code`, `emrg_contact_no`, `is_active`
   - Foreign Keys: `country_id`, `state_id`, `city_id`, `district_id`, `emp_id`

**Form Fields Mapping:**
- Current Address → Create `EmpaddressInfo` record with `addrs_type = "CURRENT"`
- Permanent Address → Create `EmpaddressInfo` record with `addrs_type = "PERMANENT"`
- If checkbox "Permanent Address Same as Current" is checked → Copy current address data to permanent

**Fields in AddressDTO:**
- name, addressLine1, addressLine2, addressLine3, pin, cityId, stateId, countryId, districtId, phoneNumber

---

### **Tab 3: Family Info**
**DTO:** `FamilyInfoDTO` ✅ (Already Created)

**Entities Used:**
1. **EmpFamilyDetails** - Family member records (MULTIPLE records needed)
   - Fields: `first_name`, `last_name`, `occupation`, `nationality`, `is_late`
   - Foreign Keys: `relation_id`, `blood_group_id`, `emp_id`

**Form Fields Mapping:**
- Father Information → Create `EmpFamilyDetails` with `relation_id = "Father"`
- Mother Information → Create `EmpFamilyDetails` with `relation_id = "Mother"`
- Additional Family Members → Create multiple `EmpFamilyDetails` records with respective `relation_id`

**Note:** You need a `Relation` entity/table with values like "Father", "Mother", "Spouse", "Son", "Daughter", etc.

**Fields in FamilyMemberDTO:**
- firstName, lastName, isLate, occupation, bloodGroupId, email, nationality, phoneNumber, relationId

---

### **Tab 4: Previous Employer Info**
**DTO:** `PreviousEmployerInfoDTO` ✅ (Already Created)

**Entities Used:**
1. **EmpExperienceDetails** - Previous employer records (MULTIPLE records needed)
   - Fields: `pre_organigation_name`, `date_of_join`, `date_of_leave`, `designation`, `leaving_reason`, `nature_of_duties`, `company_addr`, `gross_salary`, `is_active`, `pre_chaitanya_id`
   - Foreign Key: `emp_id`

**Form Fields Mapping:**
- Each previous employer → Create one `EmpExperienceDetails` record
- Company Name → `pre_organigation_name`
- Designation → `designation`
- From/To Dates → `date_of_join`, `date_of_leave`
- Leaving Reason → `leaving_reason`
- Company Address → `company_addr` (combine Address Line 1 & 2)
- Nature of Duties → `nature_of_duties`
- Gross Salary → `gross_salary`
- CTC → May need to add field or store in `gross_salary`

---

### **Tab 5: Qualification**
**DTO:** `QualificationDTO` ✅ (Already Created)

**Entities Used:**
1. **Employee** - `qualification_id` (highest qualification)
2. **EmpDetails** - `specialization`, `passout_year`
3. **Qualification** - Master data entity (for qualification types)
4. **EmpDocuments** - For storing qualification certificates (if needed)

**Form Fields Mapping:**
- Qualification → `Qualification.qualification_id` (dropdown reference)
- Degree → May need to add to `Qualification` entity or store separately
- Specialization → `EmpDetails.specialization`
- University → `Qualification.university`
- Institute → `Qualification.institute`
- Passed out Year → `EmpDetails.passout_year` or `Employee.passout_year`
- Upload Certificate → Store in `EmpDocuments` with appropriate `doc_type_id`

**Note:** If multiple qualifications are needed, you might need a separate `EmployeeQualification` join entity.

---

### **Tab 6: Upload Documents**
**DTO:** `DocumentDTO` ✅ (Just Created)

**Entities Used:**
1. **EmpDocuments** - Document records (MULTIPLE records needed)
   - Fields: `doc_path`, `ssc_no`, `is_verified`, `is_active`
   - Foreign Keys: `emp_id`, `doc_type_id`

2. **EmpDocType** - Document type master data
   - Fields: `doc_type_id`, `doc_name`, `doc_type`, `doc_short_name`, `description`, `is_active`

**Form Fields Mapping:**
- Each uploaded document → Create one `EmpDocuments` record
- Document Type → `EmpDocType.doc_type_id` (e.g., "Personal Detail Form", "Resume", "Passport", "Aadhaar Card", etc.)
- Document Path → `EmpDocuments.doc_path` (file path/URL after upload)
- SSC No (if applicable) → `EmpDocuments.ssc_no`
- Verification Status → `EmpDocuments.is_verified` (default: false, updated by HR later)

**Document Types from Form:**
- Personal Detail Form, Hiring Approval Form, Background Verification Form, ESI Declaration Form, PF Form, Resume, Previous Company Payslips, Gratuity Form, Others
- Passport, Pan Card, Voter Identity Card, Driving License, Aadhaar Card

---

### **Tab 7: Category Info**
**DTO:** `CategoryInfoDTO` ✅ (Just Created)

**Entities Used:**
1. **Employee** - Already has these foreign keys:
   - `emp_type_id` → EmployeeType
   - `department_id` → Department
   - `designation_id` → Designation
   - `category_id` → Category (already in BasicInfo)

2. **Subject** - `subject` entity
   - Fields: `subject_id`, `subject_name`, `is_active`

**Form Fields Mapping:**
- Employee Type → `Employee.emp_type_id` (already exists)
- Subject → Need to check if `Employee` has `subject_id` field, or may need to add
- Department → `Employee.department_id` (already exists)
- Designation → `Employee.designation_id` (already exists)
- Agreed Periods per week → May need to add field to `Employee` entity if not exists

**Note:** Some fields might already be in `BasicInfoDTO`. This tab serves as a confirmation/emphasis tab.

---

### **Tab 8: Bank Info**
**DTO:** `BankInfoDTO` ✅ (Just Created)

**Entities Used:**
1. **BankDetails** - Bank account records (can have TWO records: Personal + Salary)
   - Fields: `acc_type`, `bank_name`, `bank_branch`, `bank_holder_name`, `acc_no`, `ifsc_code`, `net_payable`, `bank_statement_cheque_path`, `is_active`
   - Foreign Key: `emp_id`

2. **OrgBank** - Master data for bank names (for dropdowns)
   - Fields: `org_bank_id`, `bank_name`, `ifsc_code`

**Form Fields Mapping:**
- Payment Type → `BankDetails.acc_type` (e.g., "Bank Transfer")
- Salary Less Than 40,000 → Store as boolean (may need to add field)
- Personal Account → Create `BankDetails` record with `acc_type = "PERSONAL"`
  - Bank Name → `bank_name`
  - Account No → `acc_no`
  - Account Holder Name → `bank_holder_name`
  - IFSC Code → `ifsc_code`
- Salary Account → Create `BankDetails` record with `acc_type = "SALARY"`
  - Bank (from dropdown) → `bank_name` (derived from `OrgBank`)
  - IFSC Code → `ifsc_code`
  - Account No → `acc_no`
  - Payable At → `bank_branch` or add separate field

---

## Complete DTO Structure

### Main DTO: `EmployeeOnboardingDTO`
```java
@Data
public class EmployeeOnboardingDTO {
    private BasicInfoDTO basicInfo;              // Tab 1
    private AddressInfoDTO addressInfo;          // Tab 2
    private FamilyInfoDTO familyInfo;             // Tab 3
    private PreviousEmployerInfoDTO previousEmployerInfo; // Tab 4
    private QualificationDTO qualification;       // Tab 5
    private DocumentDTO documents;                // Tab 6
    private CategoryInfoDTO categoryInfo;         // Tab 7
    private BankInfoDTO bankInfo;                 // Tab 8
}
```

---

## Entity Summary - Which Entities Store What Data

| Entity | Purpose | Records per Employee |
|--------|---------|---------------------|
| **Employee** | Main employee record | 1 |
| **EmpDetails** | Personal details | 1 |
| **EmpPfDetails** | PF/ESI/UAN info | 1 |
| **EmpaddressInfo** | Addresses | 2 (Current + Permanent) |
| **EmpFamilyDetails** | Family members | Multiple (Father, Mother, Others) |
| **EmpExperienceDetails** | Previous employers | Multiple |
| **EmpDocuments** | Uploaded documents | Multiple |
| **BankDetails** | Bank accounts | 1-2 (Personal + Salary) |

---

## POST Endpoint Structure

```java
@PostMapping("/employee/onboard")
public ResponseEntity<?> onboardEmployee(@RequestBody EmployeeOnboardingDTO onboardingDTO) {
    // Service will process all tabs and save to respective entities
    // 1. Save Employee (from basicInfo)
    // 2. Save EmpDetails (from basicInfo)
    // 3. Save EmpPfDetails (from basicInfo)
    // 4. Save Addresses - 2 records (from addressInfo)
    // 5. Save Family Members - Multiple (from familyInfo)
    // 6. Save Previous Employers - Multiple (from previousEmployerInfo)
    // 7. Handle Qualifications (from qualification)
    // 8. Save Documents - Multiple (from documents)
    // 9. Update Category Info (from categoryInfo) - mostly updates existing Employee fields
    // 10. Save Bank Details - 1-2 records (from bankInfo)
    
    return ResponseEntity.ok("Employee onboarded successfully");
}
```

---

## Important Notes

1. **Multiple Records:** Several entities can have multiple records per employee (Address, Family, Experience, Documents, Bank)
2. **Foreign Keys:** Most fields use IDs that reference master data entities (Gender, BloodGroup, Category, etc.)
3. **File Uploads:** Documents and profile pictures should be uploaded first, then file paths/URLs stored in database
4. **Verification:** Documents have `is_verified` flag (default: false, updated by HR later)
5. **Overlap:** Some fields appear in multiple tabs (like Department, Designation in both Basic Info and Category Info)

---

## All DTOs Created ✅

1. ✅ `BasicInfoDTO` - Tab 1
2. ✅ `AddressInfoDTO` - Tab 2
3. ✅ `FamilyInfoDTO` - Tab 3
4. ✅ `PreviousEmployerInfoDTO` - Tab 4
5. ✅ `QualificationDTO` - Tab 5
6. ✅ `DocumentDTO` - Tab 6
7. ✅ `CategoryInfoDTO` - Tab 7
8. ✅ `BankInfoDTO` - Tab 8
9. ✅ `EmployeeOnboardingDTO` - Main DTO combining all tabs

All DTOs are ready for your single POST method!

