# Entity-DDL Alignment Documentation

## Overview
This document details the process of aligning JPA entities with database DDL schemas, creating repositories with custom query methods, and fixing data persistence issues.

---

## 1. Repository Creation Strategy

### Pattern Used
Each entity has a corresponding repository interface extending `JpaRepository`. For entities where we need to filter by `is_active`, custom query methods were created.

### Repository Structure

#### Standard Repository Pattern
```java
@Repository
public interface EntityRepository extends JpaRepository<Entity, Integer> {
    // Basic CRUD operations available from JpaRepository
}
```

#### Custom Query Methods (for is_active filtering)
Since entity ID fields are named with suffix `_id` (e.g., `emp_id`, `department_id`), Spring Data JPA's method naming convention fails. Custom `@Query` methods were created:

**Example: DepartmentRepository**
```java
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    
    @Query("SELECT d FROM Department d WHERE d.department_id = :id AND d.isActive = :isActive")
    Optional<Department> findByIdAndIsActive(@Param("id") Integer id, @Param("isActive") Integer isActive);
}
```

**Repositories Created with Custom Queries:**
1. `EmployeeRepository` - `findByIdAndIs_active(Integer id, int is_active)`
2. `DepartmentRepository` - `findByIdAndIsActive(Integer id, Integer isActive)`
3. `DesignationRepository` - `findByIdAndIsActive(Integer id, Integer isActive)`
4. `CategoryRepository` - `findByIdAndIsActive(Integer id, Integer isActive)`
5. `GenderRepository` - `findByIdAndIsActive(Integer id, int isActive)`
6. `EmployeeTypeRepository` - `findByIdAndIsActive(Integer id, Integer isActive)`
7. `WorkingModeRepository` - `findByIdAndIsActive(Integer id, Integer isActive)`
8. `JoiningAsRepository` - `findByIdAndIsActive(Integer id, Integer isActive)`
9. `ModeOfHiringRepository` - `findByIdAndIsActive(Integer id, Integer isActive)`
10. `MaritalStatusRepository` - `findByIdAndIsActive(Integer id, Integer isActive)` (uses native query)

**Note:** `MaritalStatusRepository` uses a native query because the schema name was different initially:
```java
@Query(value = "SELECT * FROM sce_employee.sce_marital_status WHERE marital_status_id = :id AND is_active = :isActive", nativeQuery = true)
Optional<MaritalStatus> findByIdAndIsActive(@Param("id") Integer id, @Param("isActive") Integer isActive);
```

---

## 2. Entity-DDL Alignment Process

### Methodology
1. Compare entity field names with DDL column names
2. Check data types match (e.g., `int4` → `Integer`, `varchar` → `String`)
3. Identify NOT NULL constraints and add validation
4. Add missing audit fields (`created_by`, `created_date`, `updated_by`, `updated_date`)
5. Mark non-existent columns as `@Transient`

### Key Issues Fixed

#### A. Column Name Mismatches

| Entity | Field Name (Before) | Database Column | Fixed To |
|--------|-------------------|-----------------|----------|
| `EmpDetails` | `aadhar_num` | `adhaar_no` | `adhaar_no` |
| `EmpDetails` | `email` | `personal_email` | `personal_email` |
| `EmpDetails` | `pancard_num` | `pancard_no` | `pancard_no` |
| `EmpDetails` | `adhaar_enrolment_num` | `adhaar_enrolment_no` | `adhaar_enrolment_no` |
| `EmpDocType` | `descciption` | `description` | `description` |

#### B. Schema Name Corrections

| Entity | Schema (Before) | Schema (After) |
|--------|----------------|----------------|
| `MaritalStatus` | `sc_employee` | `sce_employee` |

#### C. Table Name Corrections

| Entity | Table Name (Before) | Table Name (After) |
|--------|-------------------|-------------------|
| `EmpExperienceDetails` | `sce_exp_detl` | `sce_emp_exp_detl` |

#### D. Non-Existent Columns (Marked as @Transient)

| Entity | Field | Reason |
|--------|-------|--------|
| `Employee` | `passout_year` | Column does not exist in `sce_emp` table |
| `EmpDetails` | `passout_year` | Column does not exist in `sce_emp_detl` table |
| `EmpDetails` | `specialization` | Column does not exist in `sce_emp_detl` table |
| `EmpPfDetails` | `pre_uan_no` | Column does not exist in `sce_emp_pf_esi_uan_info` table |

#### E. Removed Relationships (Columns Don't Exist)

| Entity | Relationship Field | Database Column | Action |
|--------|-------------------|-----------------|--------|
| `EmpDetails` | `emp_pf_esi_uan_info_id` | `emp_pf_esi_uan_info_id` | Commented out - column doesn't exist |
| `EmpaddressInfo` | `district_id` | `district_id` | Commented out - column doesn't exist |

---

## 3. Required NOT NULL Fields Handling

### Strategy
1. Add field to entity with `nullable = false`
2. Set default values in entity (e.g., `created_by = 1`)
3. Validate in service layer before saving
4. Throw `ResourceNotFoundException` with clear message if missing

### Audit Fields Pattern
All entities with audit columns follow this pattern:
```java
// Audit fields - required NOT NULL columns
@Column(name = "created_by", nullable = false)
private Integer created_by = 1; // Default to 1 if not provided

@Column(name = "created_date", nullable = false)
private Timestamp created_date = new Timestamp(System.currentTimeMillis());

@Column(name = "updated_by")
private Integer updated_by;

@Column(name = "updated_date")
private Timestamp updated_date;
```

### Entities with Audit Fields Added:
1. `EmpPfDetails`
2. `EmpDetails`
3. `EmpaddressInfo`
4. `EmpFamilyDetails`
5. `EmpExperienceDetails`
6. `EmpQualification`
7. `EmpDocuments`
8. `BankDetails`

### Required NOT NULL Fields by Entity

#### Employee
- `user_name` - Generated from `firstName.lastName` (limited to 50 chars)
- `password` - Default: "Temp@123"
- `emp_check_list_status_id` - Default: ID 2

#### EmpDetails
- `caste_id` - Validated in service
- `religion_id` - Validated in service
- `blood_group_id` - Validated in service
- `marital_status_id` - Validated in service
- `emergency_ph_no` - Set to empty string if not provided

#### EmpaddressInfo
- `emp_id` - Foreign key to `sce_emp`
- `country_id` - Validated in service
- `state_id` - Validated in service
- `city_id` - Validated in service
- `addrs_type` - Limited to 5 chars: "CURR" or "PERM" (not "CURRENT" or "PERMANENT")

#### EmpFamilyDetails
- `gender_id` - Validated in service
- `blood_group_id` - Validated in service
- `relation_id` - Validated in service
- `nationality` - Validated in service

#### EmpExperienceDetails
- `designation` - Validated in service
- `nature_of_duties` - Validated in service
- `company_addr` - Combined from addressLine1 and addressLine2

#### EmpDocuments
- `doc_type_id` - Validated in service

#### BankDetails
- `bank_holder_name` - For salary account, falls back to employee name if not provided
- `acc_no` - **LIMITATION**: Database column is `int4`, max value 2,147,483,647
- `ifsc_code` - Validated in service
- `net_payable` - Default: 0.0f

---

## 4. Data Type Corrections

### Account Number Handling (BankDetails)
**Issue:** Database column `acc_no` is `int4` (max: 2,147,483,647), but account numbers can be 14+ digits.

**Solution:**
- Entity field: `Integer acc_no`
- Service validates: If account number > `Integer.MAX_VALUE`, throw error with clear message
- **Recommendation:** Update database column to `bigint` (int8):
  ```sql
  ALTER TABLE sce_employee.sce_emp_bank_detl 
  ALTER COLUMN acc_no TYPE bigint;
  ```

### Address Type Length
**Issue:** `addrs_type` column is `varchar(5)`, but code was using "CURRENT" (6 chars) and "PERMANENT" (8 chars).

**Solution:**
- Changed to "CURR" (4 chars) and "PERM" (4 chars)

---

## 6. Service Layer Validation Pattern

### Standard Validation Pattern
```java
// Required NOT NULL field validation
if (dto.getFieldId() != null) {
    entity.setField_id(repository.findById(dto.getFieldId())
        .orElseThrow(() -> new ResourceNotFoundException("Field not found")));
} else {
    throw new ResourceNotFoundException("Field ID is required (NOT NULL column)");
}
```

### Active Record Filtering
Always use `findByIdAndIsActive()` methods to ensure only active records are used:
```java
entity.setMarital_status_id(maritalStatusRepository.findByIdAndIsActive(dto.getMaritalStatusId(), 1)
    .orElseThrow(() -> new ResourceNotFoundException("Active MaritalStatus not found")));
```

---

## 7. Employee Reference ID Handling

### Issue
Employee reference IDs can be either:
- Integer IDs (e.g., `4532`)
- String codes (e.g., `"HYD11001111452"`)

### Solution
Created `parseEmployeeId()` helper method:
```java
private Integer parseEmployeeId(String empIdOrCode, String fieldName) {
    if (empIdOrCode == null || empIdOrCode.trim().isEmpty()) {
        return null;
    }
    try {
        return Integer.parseInt(empIdOrCode);
    } catch (NumberFormatException e) {
        logger.warn("⚠️ Skipping {} lookup: '{}' is not a numeric ID. Employee code lookup not yet implemented.", 
                   fieldName, empIdOrCode);
        return null; // Skip lookup for non-numeric codes
    }
}
```

**Note:** Employee code lookup (converting codes to IDs) is not yet implemented. Non-numeric codes are logged and skipped.

---

## 8. Complete Entity Audit Checklist

For each entity, ensure:
- [x] All fields match DDL column names (using `@Column(name = "...")` if different)
- [x] All NOT NULL columns have validation in service
- [x] Audit fields added with defaults (`created_by`, `created_date`)
- [x] Non-existent columns marked as `@Transient`
- [x] Data types match database (int4 → Integer, bigint → Long, varchar → String)
- [x] String length constraints respected (varchar(5) → max 5 chars)
- [x] Foreign keys validated with `findByIdAndIsActive()` where applicable

---

## 9. Summary of Changes

### Entities Modified:
1. ✅ `Employee` - Added `user_name`, `password`, `emp_check_list_status_id`; Marked `passout_year` as `@Transient`
2. ✅ `EmpDetails` - Fixed column names, added audit fields, added required field validations
3. ✅ `EmpPfDetails` - Marked `pre_uan_no` as `@Transient`, added audit fields
4. ✅ `EmpaddressInfo` - Removed `district_id`, added `emp_id`, added audit fields, fixed `addrs_type` length
5. ✅ `EmpFamilyDetails` - Added `gender_id`, added audit fields, added required field validations
6. ✅ `EmpExperienceDetails` - Fixed table name, added audit fields, added required field validations
7. ✅ `EmpQualification` - Added audit fields
8. ✅ `EmpDocuments` - Added audit fields, validated `doc_type_id`
9. ✅ `BankDetails` - Changed `acc_no` handling, added audit fields, added `bank_holder_name` fallback
10. ✅ `MaritalStatus` - Fixed schema name

### Repositories Created/Modified:
- All repositories with `findByIdAndIsActive()` methods for active record filtering
- `MaritalStatusRepository` uses native query for schema compatibility

### DTOs Modified:
- `FamilyInfoDTO` - Added `genderId` field
- `BankInfoDTO` - Added `accountHolderName` to `SalaryAccountDTO`
- `BasicInfoDTO` - Changed employee reference IDs to `String` to support codes

---

## 10. Testing Recommendations

### Test Cases to Verify:
1. ✅ All required NOT NULL fields are validated
2. ✅ Account numbers > 2,147,483,647 are rejected with clear error
3. ✅ Address type values are "CURR" or "PERM" (not longer strings)
4. ✅ Only active records (`is_active = 1`) are used in lookups
5. ✅ Employee references with string codes are logged but skipped
6. ✅ Audit fields are automatically set on save

---

## 11. Known Limitations

1. **Account Number Size:** Database column `acc_no` is `int4`, limiting account numbers to 2,147,483,647
   - **Workaround:** Validate in service and provide clear error message
   - **Fix:** Update database column to `bigint`

2. **Employee Code Lookup:** String employee codes (e.g., "HYD11001111452") are not converted to IDs
   - **Current Behavior:** Logged and skipped
   - **Future Enhancement:** Implement employee code to ID lookup service

3. **District in Addresses:** District field removed from `EmpaddressInfo` as column doesn't exist
   - If needed in future, add `district_id` column to `sce_emp_addrs` table

---

## 12. Code Quality Improvements Made

- ✅ Consistent error messages with clear explanations
- ✅ Validation at service layer prevents database constraint violations
- ✅ Default values for audit fields prevent NOT NULL violations
- ✅ Comprehensive logging for debugging

---

## Conclusion

This documentation captures the complete process of aligning entities with database schemas, creating repositories with custom queries, and implementing proper validation and error handling. All changes were made to ensure data integrity and prevent database constraint violations while maintaining code quality and performance.

