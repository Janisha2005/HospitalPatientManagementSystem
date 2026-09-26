package com.hospital.config;

import com.hospital.entity.*;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final OpdVisitRepository opdVisitRepository;
    private final PasswordEncoder passwordEncoder;

    private final MedicineRepository medicineRepository;
    private final LabTestRepository labTestRepository;
    private final RadiologyTestRepository radiologyTestRepository;
    private final PatientAllergyRepository patientAllergyRepository;
    private final PatientConditionRepository patientConditionRepository;

    private final WardRepository wardRepository;
    private final BedRepository bedRepository;
    private final IpdAdmissionRepository ipdAdmissionRepository;
    private final BedAllocationRepository bedAllocationRepository;
    private final InpatientVitalRepository inpatientVitalRepository;
    private final NursingNoteRepository nursingNoteRepository;
    private final InpatientProgressNoteRepository progressNoteRepository;
    private final DischargePlanRepository dischargePlanRepository;
    private final DischargeSummaryRepository dischargeSummaryRepository;

    private final MedicineCategoryRepository medicineCategoryRepository;
    private final SupplierRepository supplierRepository;
    private final MedicineBatchRepository medicineBatchRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final GoodsReceiptItemRepository goodsReceiptItemRepository;
    private final PharmacyDispensingRepository pharmacyDispensingRepository;
    private final PharmacyReturnRepository pharmacyReturnRepository;

    private final ChargeMasterRepository chargeMasterRepository;
    private final BillingAccountRepository billingAccountRepository;
    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final CreditNoteRepository creditNoteRepository;
    private final PatientLedgerEntryRepository patientLedgerEntryRepository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing India-localized database seed data...");

        // Ensure roles table column length supports new roles (PHARMACIST)
        try {
            entityManager.createNativeQuery("ALTER TABLE roles MODIFY COLUMN name VARCHAR(30) NOT NULL").executeUpdate();
        } catch (Exception e) {
            log.debug("Roles column alteration skipped or already updated: {}", e.getMessage());
        }

        // 1. Roles
        Map<Role.RoleName, Role> rolesMap = new EnumMap<>(Role.RoleName.class);
        for (Role.RoleName roleName : Role.RoleName.values()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name(roleName)
                            .description("System " + roleName.name() + " role")
                            .build()));
            rolesMap.put(roleName, role);
        }

        // 2. Users with Indian names & phone numbers
        createOrUpdateUser("admin", "Admin@123", "admin@pulse-hospital.in", "Janisha S", "+919876500001", rolesMap.get(Role.RoleName.ADMIN));
        createOrUpdateUser("doc_smith", "Doctor@123", "arjun.krishnan@pulse-hospital.in", "Dr. Arjun Krishnan", "+919876543211", rolesMap.get(Role.RoleName.DOCTOR));
        createOrUpdateUser("doc_davis", "Doctor@123", "sunita.rao@pulse-hospital.in", "Dr. Sunita Rao", "+919876543222", rolesMap.get(Role.RoleName.DOCTOR));
        createOrUpdateUser("nurse_joy", "Nurse@123", "anitha.nurse@pulse-hospital.in", "Anitha Raman", "+919876500004", rolesMap.get(Role.RoleName.NURSE));
        createOrUpdateUser("receptionist_clara", "Receptionist@123", "kavitha.frontdesk@pulse-hospital.in", "Kavitha Menon", "+919876500005", rolesMap.get(Role.RoleName.RECEPTIONIST));
        createOrUpdateUser("patient_john", "Patient@123", "arun.kumar@example.com", "Arun Kumar", "+919876543210", rolesMap.get(Role.RoleName.PATIENT));
        createOrUpdateUser("pharmacist_rahul", "Pharmacist@123", "rahul.pharmacy@pulse-hospital.in", "Rahul Verma", "+919876500006", rolesMap.get(Role.RoleName.PHARMACIST));
        createOrUpdateUser("billing_vikram", "Billing@123", "vikram.billing@pulse-hospital.in", "Vikram Malhotra", "+919876500007", rolesMap.get(Role.RoleName.BILLING_OFFICER));

        // 3. Indian Hospital Departments
        Department cardio = departmentRepository.findByDepartmentCode("CARDIO")
                .orElseGet(() -> departmentRepository.save(Department.builder()
                        .departmentCode("CARDIO")
                        .departmentName("Cardiology")
                        .description("Department for cardiovascular care and heart surgery")
                        .location("Block A, 3rd Floor")
                        .isActive(true)
                        .build()));

        Department ortho = departmentRepository.findByDepartmentCode("ORTHO")
                .orElseGet(() -> departmentRepository.save(Department.builder()
                        .departmentCode("ORTHO")
                        .departmentName("Orthopedics")
                        .description("Department for joint replacements and trauma care")
                        .location("Block B, 1st Floor")
                        .isActive(true)
                        .build()));

        if (!departmentRepository.existsByDepartmentCode("GENERAL")) {
            departmentRepository.save(Department.builder()
                    .departmentCode("GENERAL")
                    .departmentName("General Medicine")
                    .description("Primary outpatient care and internal medicine")
                    .location("Block A, Ground Floor")
                    .isActive(true)
                    .build());
        }

        if (!departmentRepository.existsByDepartmentCode("PEDIATRICS")) {
            departmentRepository.save(Department.builder()
                    .departmentCode("PEDIATRICS")
                    .departmentName("Pediatrics")
                    .description("Comprehensive child health and neonatal care")
                    .location("Block C, 2nd Floor")
                    .isActive(true)
                    .build());
        }

        if (!departmentRepository.existsByDepartmentCode("DERMATOLOGY")) {
            departmentRepository.save(Department.builder()
                    .departmentCode("DERMATOLOGY")
                    .departmentName("Dermatology")
                    .description("Skin care and cosmetic dermatology")
                    .location("Block B, 3rd Floor")
                    .isActive(true)
                    .build());
        }

        // 4. Doctors (Fictional Indian Doctors)
        if (!doctorRepository.existsByLicenseNumber("MCI-TN-2015-8849") && !doctorRepository.existsByDoctorId("DOC-2026-000001")) {
            doctorRepository.save(Doctor.builder()
                    .doctorId("DOC-2026-000001")
                    .firstName("Arjun")
                    .lastName("Krishnan")
                    .email("arjun.krishnan@pulse-hospital.in")
                    .phone("+919876543211")
                    .specialization("Interventional Cardiology")
                    .qualification("MBBS, MD (Cardiology), DM")
                    .licenseNumber("MCI-TN-2015-8849")
                    .department(cardio)
                    .consultationFee(new BigDecimal("1000.00"))
                    .addressLine1("15 Haddows Road")
                    .addressLine2("Nungambakkam")
                    .city("Chennai")
                    .district("Chennai")
                    .state("Tamil Nadu")
                    .pincode("600006")
                    .country("India")
                    .joiningDate(LocalDate.of(2018, 4, 10))
                    .isActive(true)
                    .build());
        }

        if (!doctorRepository.existsByLicenseNumber("MCI-MH-2017-4312") && !doctorRepository.existsByDoctorId("DOC-2026-000002")) {
            doctorRepository.save(Doctor.builder()
                    .doctorId("DOC-2026-000002")
                    .firstName("Sunita")
                    .lastName("Rao")
                    .email("sunita.rao@pulse-hospital.in")
                    .phone("+919876543222")
                    .specialization("Orthopedic Surgery")
                    .qualification("MBBS, MS (Ortho), DNB")
                    .licenseNumber("MCI-MH-2017-4312")
                    .department(ortho)
                    .consultationFee(new BigDecimal("1200.00"))
                    .addressLine1("42 Linking Road")
                    .addressLine2("Bandra West")
                    .city("Mumbai")
                    .district("Mumbai Suburban")
                    .state("Maharashtra")
                    .pincode("400050")
                    .country("India")
                    .joiningDate(LocalDate.of(2020, 8, 15))
                    .isActive(true)
                    .build());
        }

        // 5. Patients (Fictional Indian Patients)
        if (!patientRepository.existsByPatientId("PAT-2026-000001")) {
            patientRepository.save(Patient.builder()
                    .patientId("PAT-2026-000001")
                    .firstName("Arun")
                    .lastName("Kumar")
                    .dateOfBirth(LocalDate.of(1988, 6, 15))
                    .gender("Male")
                    .bloodGroup("O+")
                    .phone("+919876543210")
                    .email("arun.kumar@example.com")
                    .addressLine1("24 Anna Nagar Main Road")
                    .addressLine2("Near Tower Park")
                    .city("Chennai")
                    .district("Chennai")
                    .state("Tamil Nadu")
                    .pincode("600040")
                    .country("India")
                    .emergencyContactName("Priya Sharma")
                    .emergencyContactPhone("+919876543219")
                    .emergencyContactRelationship("Mother")
                    .registrationDate(LocalDate.of(2026, 1, 10))
                    .isActive(true)
                    .build());
        }

        if (!patientRepository.existsByPatientId("PAT-2026-000002")) {
            patientRepository.save(Patient.builder()
                    .patientId("PAT-2026-000002")
                    .firstName("Priya")
                    .lastName("Nair")
                    .dateOfBirth(LocalDate.of(1994, 11, 22))
                    .gender("Female")
                    .bloodGroup("A+")
                    .phone("+919812345678")
                    .email("priya.nair@example.com")
                    .addressLine1("108 Avinashi Road")
                    .addressLine2("Peelamedu")
                    .city("Coimbatore")
                    .district("Coimbatore")
                    .state("Tamil Nadu")
                    .pincode("641001")
                    .country("India")
                    .emergencyContactName("Ramesh Nair")
                    .emergencyContactPhone("+919812345679")
                    .emergencyContactRelationship("Father")
                    .registrationDate(LocalDate.of(2026, 2, 5))
                    .isActive(true)
                    .build());
        }

        if (!patientRepository.existsByPatientId("PAT-2026-000003")) {
            patientRepository.save(Patient.builder()
                    .patientId("PAT-2026-000003")
                    .firstName("Vikram")
                    .lastName("Singh")
                    .dateOfBirth(LocalDate.of(1982, 3, 30))
                    .gender("Male")
                    .bloodGroup("B+")
                    .phone("+919988776655")
                    .email("vikram.singh@example.com")
                    .addressLine1("78 MG Road")
                    .addressLine2("Indiranagar")
                    .city("Bengaluru")
                    .district("Bengaluru Urban")
                    .state("Karnataka")
                    .pincode("560038")
                    .country("India")
                    .emergencyContactName("Sunita Singh")
                    .emergencyContactPhone("+919988776654")
                    .emergencyContactRelationship("Spouse")
                    .registrationDate(LocalDate.of(2026, 3, 12))
                    .isActive(true)
                    .build());
        }

        log.info("India-localized database seed data initialization completed successfully.");

        // 6. Phase 2 Demo Availability, Appointments, and OPD Visits
        seedPhase2Data(rolesMap.get(Role.RoleName.ADMIN));

        // 7. Phase 3 Catalogs and Patient EHR Demo Data
        seedPhase3Data();

        // 8. Phase 4 Wards, Beds, Admissions, Nursing, and Discharge Demo Data
        seedPhase4Data();

        // 9. Phase 5 Pharmacy & Inventory Demo Data
        seedPhase5Data();
    }

    private void seedPhase3Data() {
        // Medicines Catalog
        if (medicineRepository.count() == 0) {
            medicineRepository.save(Medicine.builder().medicineCode("MED-PCM-500").medicineName("Paracetamol 500mg").genericName("Paracetamol").dosageForm("Tablet").strength("500 mg").manufacturer("GlaxoSmithKline India").isActive(true).build());
            medicineRepository.save(Medicine.builder().medicineCode("MED-AMLO-5").medicineName("Amlodipine 5mg").genericName("Amlodipine Besylate").dosageForm("Tablet").strength("5 mg").manufacturer("Mankind Pharma").isActive(true).build());
            medicineRepository.save(Medicine.builder().medicineCode("MED-MET-500").medicineName("Metformin 500mg").genericName("Metformin Hydrochloride").dosageForm("Tablet").strength("500 mg").manufacturer("USV Pvt Ltd").isActive(true).build());
            medicineRepository.save(Medicine.builder().medicineCode("MED-AZI-500").medicineName("Azithromycin 500mg").genericName("Azithromycin").dosageForm("Tablet").strength("500 mg").manufacturer("Cipla").isActive(true).build());
            medicineRepository.save(Medicine.builder().medicineCode("MED-PAN-40").medicineName("Pantoprazole 40mg").genericName("Pantoprazole").dosageForm("Tablet").strength("40 mg").manufacturer("Alkem Labs").isActive(true).build());
            medicineRepository.save(Medicine.builder().medicineCode("MED-ATOR-10").medicineName("Atorvastatin 10mg").genericName("Atorvastatin").dosageForm("Tablet").strength("10 mg").manufacturer("Zydus Cadila").isActive(true).build());
        }

        // Lab Tests Catalog
        if (labTestRepository.count() == 0) {
            labTestRepository.save(LabTest.builder().testCode("LAB-CBC").testName("Complete Blood Count (CBC)").category("Hematology").normalRangeDescription("Hb: 13.5-17.5 g/dL, WBC: 4.5-11.0 k/uL").unit("Various").sampleType("Whole Blood (EDTA)").isActive(true).build());
            labTestRepository.save(LabTest.builder().testCode("LAB-FBS").testName("Fasting Blood Sugar (FBS)").category("Biochemistry").normalRangeDescription("70 - 99 mg/dL").unit("mg/dL").sampleType("Fluoride Plasma").isActive(true).build());
            labTestRepository.save(LabTest.builder().testCode("LAB-HBA1C").testName("HbA1c (Glycated Hemoglobin)").category("Biochemistry").normalRangeDescription("< 5.7%").unit("%").sampleType("Whole Blood").isActive(true).build());
            labTestRepository.save(LabTest.builder().testCode("LAB-LIPID").testName("Lipid Profile").category("Biochemistry").normalRangeDescription("Cholesterol < 200 mg/dL").unit("mg/dL").sampleType("Serum").isActive(true).build());
            labTestRepository.save(LabTest.builder().testCode("LAB-LFT").testName("Liver Function Test (LFT)").category("Biochemistry").normalRangeDescription("Bilirubin 0.3-1.2 mg/dL").unit("mg/dL").sampleType("Serum").isActive(true).build());
            labTestRepository.save(LabTest.builder().testCode("LAB-KFT").testName("Kidney Function Test (KFT)").category("Biochemistry").normalRangeDescription("Urea 15-45 mg/dL").unit("mg/dL").sampleType("Serum").isActive(true).build());
        }

        // Radiology Tests Catalog
        if (radiologyTestRepository.count() == 0) {
            radiologyTestRepository.save(RadiologyTest.builder().testCode("RAD-CXR").testName("Chest X-Ray PA View").modality(RadiologyModality.X_RAY).bodyPart("Chest").description("Remove metal objects prior to scan.").isActive(true).build());
            radiologyTestRepository.save(RadiologyTest.builder().testCode("RAD-USG-ABD").testName("Ultrasound Whole Abdomen").modality(RadiologyModality.ULTRASOUND).bodyPart("Abdomen").description("Fasting for 6 hours required.").isActive(true).build());
            radiologyTestRepository.save(RadiologyTest.builder().testCode("RAD-CT-BRAIN").testName("CT Scan Brain Plain").modality(RadiologyModality.CT).bodyPart("Head / Brain").description("Plain CT Brain scan.").isActive(true).build());
            radiologyTestRepository.save(RadiologyTest.builder().testCode("RAD-MRI-KNEE").testName("MRI Knee Joint Right").modality(RadiologyModality.MRI).bodyPart("Right Knee").description("Screen for metallic implants.").isActive(true).build());
            radiologyTestRepository.save(RadiologyTest.builder().testCode("RAD-ECG").testName("12-Lead Electrocardiogram (ECG)").modality(RadiologyModality.OTHER).bodyPart("Chest / Heart").description("Standard 12-lead ECG.").isActive(true).build());
        }

        // Patient Allergies & Chronic Conditions Demo
        Patient patient1 = patientRepository.findByPatientId("PAT-2026-000001").orElse(null);
        if (patient1 != null) {
            if (patientAllergyRepository.findByPatientId(patient1.getId()).isEmpty()) {
                patientAllergyRepository.save(PatientAllergy.builder()
                        .patient(patient1)
                        .allergen("Penicillin")
                        .allergyType("Medication")
                        .severity(AllergySeverity.SEVERE)
                        .reaction("Skin rash, urticaria, mild wheezing")
                        .status(AllergyStatus.ACTIVE)
                        .build());
            }

            if (patientConditionRepository.findByPatientId(patient1.getId()).isEmpty()) {
                patientConditionRepository.save(PatientCondition.builder()
                        .patient(patient1)
                        .conditionName("Essential Hypertension")
                        .description("ICD-10: I10 - Managed with Amlodipine 5mg daily. Regular BP checks recommended.")
                        .status(ConditionStatus.ACTIVE)
                        .diagnosedDate(LocalDate.of(2021, 3, 15))
                        .build());
            }
        }
    }

    private void seedPhase2Data(Role adminRole) {
        Doctor doctor = doctorRepository.findByDoctorId("DOC-2026-000001").orElse(null);
        Patient patient1 = patientRepository.findByPatientId("PAT-2026-000001").orElse(null);
        Patient patient2 = patientRepository.findByPatientId("PAT-2026-000002").orElse(null);

        if (doctor != null) {
            // Seed Doctor Availability if none exists
            if (doctorAvailabilityRepository.findByDoctorIdAndIsActiveTrue(doctor.getId()).isEmpty()) {
                for (java.time.DayOfWeek day : java.time.DayOfWeek.values()) {
                    if (day != java.time.DayOfWeek.SUNDAY) {
                        doctorAvailabilityRepository.save(DoctorAvailability.builder()
                                .doctor(doctor)
                                .dayOfWeek(day)
                                .startTime(java.time.LocalTime.of(9, 0))
                                .endTime(java.time.LocalTime.of(17, 0))
                                .slotDurationMinutes(30)
                                .isActive(true)
                                .build());
                    }
                }
            }
        }

        if (doctor != null && patient1 != null && appointmentRepository.count() == 0) {
            User adminUser = userRepository.findByUsername("admin").orElse(null);

            // Sample Appointment 1 - Today
            Appointment apt1 = appointmentRepository.save(Appointment.builder()
                    .appointmentId("APT-2026-000001")
                    .patient(patient1)
                    .doctor(doctor)
                    .department(doctor.getDepartment())
                    .appointmentDate(LocalDate.now())
                    .startTime(java.time.LocalTime.of(10, 0))
                    .endTime(java.time.LocalTime.of(10, 30))
                    .appointmentType(AppointmentType.NEW_CONSULTATION)
                    .reasonForVisit("Chest tightness and mild shortness of breath during morning walk")
                    .status(AppointmentStatus.CHECKED_IN)
                    .notes("Patient arrived on time. Vitals captured by nurse.")
                    .createdBy(adminUser)
                    .build());

            // Sample OPD Visit for Apt 1
            opdVisitRepository.save(OpdVisit.builder()
                    .opdVisitId("OPD-2026-000001")
                    .appointment(apt1)
                    .patient(patient1)
                    .doctor(doctor)
                    .department(doctor.getDepartment())
                    .visitDate(LocalDate.now())
                    .queueNumber(1)
                    .chiefComplaint("Chest tightness and mild shortness of breath")
                    .vitalTemperature(new BigDecimal("36.8"))
                    .vitalPulse(78)
                    .vitalBloodPressure("120/80")
                    .vitalRespiratoryRate(16)
                    .vitalOxygenSaturation(98)
                    .heightCm(new BigDecimal("172.0"))
                    .weightKg(new BigDecimal("74.5"))
                    .visitStatus(OpdVisitStatus.WAITING)
                    .build());

            if (patient2 != null) {
                Appointment apt2 = appointmentRepository.save(Appointment.builder()
                        .appointmentId("APT-2026-000002")
                        .patient(patient2)
                        .doctor(doctor)
                        .department(doctor.getDepartment())
                        .appointmentDate(LocalDate.now())
                        .startTime(java.time.LocalTime.of(11, 0))
                        .endTime(java.time.LocalTime.of(11, 30))
                        .appointmentType(AppointmentType.FOLLOW_UP)
                        .reasonForVisit("Routine BP checkup and ECG review")
                        .status(AppointmentStatus.CHECKED_IN)
                        .createdBy(adminUser)
                        .build());

                opdVisitRepository.save(OpdVisit.builder()
                        .opdVisitId("OPD-2026-000002")
                        .appointment(apt2)
                        .patient(patient2)
                        .doctor(doctor)
                        .department(doctor.getDepartment())
                        .visitDate(LocalDate.now())
                        .queueNumber(2)
                        .chiefComplaint("Routine BP checkup and ECG review")
                        .vitalTemperature(new BigDecimal("37.0"))
                        .vitalPulse(72)
                        .vitalBloodPressure("130/85")
                        .vitalRespiratoryRate(15)
                        .vitalOxygenSaturation(99)
                        .heightCm(new BigDecimal("165.0"))
                        .weightKg(new BigDecimal("62.0"))
                        .visitStatus(OpdVisitStatus.WAITING)
                        .build());
            }
        }
    }

    private void createOrUpdateUser(String username, String rawPassword, String email, String fullName, String phone, Role role) {
        userRepository.findByUsername(username).ifPresentOrElse(user -> {
            user.setEmail(email);
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setRole(role);
            userRepository.save(user);
        }, () -> {
            if (!userRepository.existsByEmail(email)) {
                userRepository.save(User.builder()
                        .username(username)
                        .password(passwordEncoder.encode(rawPassword))
                        .email(email)
                        .fullName(fullName)
                        .phone(phone)
                        .role(role)
                        .isActive(true)
                        .build());
            }
        });
    }

    private void seedPhase4Data() {
        if (wardRepository.count() > 0) return;

        Department cardio = departmentRepository.findByDepartmentCode("CARDIO").orElse(null);
        Department ortho = departmentRepository.findByDepartmentCode("ORTHO").orElse(null);
        Department general = departmentRepository.findByDepartmentCode("GENERAL").orElse(null);

        if (cardio == null || general == null) return;

        // 1. Wards
        Ward genWard = wardRepository.save(Ward.builder()
                .wardCode("WARD-GEN-001")
                .wardName("General Medical Ward")
                .wardType(WardType.GENERAL)
                .department(general)
                .floor("1st Floor")
                .building("Main Block")
                .genderPolicy(WardGenderPolicy.MIXED)
                .capacity(20)
                .isActive(true)
                .build());

        Ward cardioWard = wardRepository.save(Ward.builder()
                .wardCode("WARD-CARD-001")
                .wardName("Cardiology Specialty Ward")
                .wardType(WardType.SEMI_PRIVATE)
                .department(cardio)
                .floor("3rd Floor")
                .building("Block A")
                .genderPolicy(WardGenderPolicy.MIXED)
                .capacity(10)
                .isActive(true)
                .build());

        Ward icuWard = wardRepository.save(Ward.builder()
                .wardCode("WARD-ICU-001")
                .wardName("Intensive Care Unit (ICU)")
                .wardType(WardType.ICU)
                .department(cardio)
                .floor("2nd Floor")
                .building("Critical Care Block")
                .genderPolicy(WardGenderPolicy.MIXED)
                .capacity(8)
                .isActive(true)
                .build());

        // 2. Beds
        Bed bed1 = bedRepository.save(Bed.builder()
                .bedCode("GEN-A-001")
                .ward(genWard)
                .bedNumber("001")
                .bedType(BedType.STANDARD)
                .status(BedStatus.OCCUPIED)
                .isActive(true)
                .build());

        Bed bed2 = bedRepository.save(Bed.builder()
                .bedCode("GEN-A-002")
                .ward(genWard)
                .bedNumber("002")
                .bedType(BedType.STANDARD)
                .status(BedStatus.CLEANING)
                .isActive(true)
                .build());

        Bed bed3 = bedRepository.save(Bed.builder()
                .bedCode("GEN-A-003")
                .ward(genWard)
                .bedNumber("003")
                .bedType(BedType.STANDARD)
                .status(BedStatus.AVAILABLE)
                .isActive(true)
                .build());

        Bed bedCardio = bedRepository.save(Bed.builder()
                .bedCode("CARD-B-001")
                .ward(cardioWard)
                .bedNumber("101")
                .bedType(BedType.SEMI_PRIVATE)
                .status(BedStatus.AVAILABLE)
                .isActive(true)
                .build());

        Bed bedIcu = bedRepository.save(Bed.builder()
                .bedCode("ICU-01")
                .ward(icuWard)
                .bedNumber("ICU-1")
                .bedType(BedType.ICU)
                .status(BedStatus.AVAILABLE)
                .isActive(true)
                .build());

        // 3. Demo IPD Admission for Patient 1
        Patient patient1 = patientRepository.findByPatientId("PAT-2026-000001").orElse(null);
        Doctor doctor = doctorRepository.findByDoctorId("DOC-2026-000001").orElse(null);
        User nurseUser = userRepository.findByUsername("nurse_joy").orElse(null);

        if (patient1 != null && doctor != null && ipdAdmissionRepository.count() == 0) {
            IpdAdmission admission = ipdAdmissionRepository.save(IpdAdmission.builder()
                    .admissionId("ADM-2026-000001")
                    .patient(patient1)
                    .admittingDoctor(doctor)
                    .department(cardio)
                    .ward(genWard)
                    .bed(bed1)
                    .admissionType(AdmissionType.ELECTIVE)
                    .admissionDate(LocalDate.now().minusDays(2))
                    .admissionTime(java.time.LocalTime.of(10, 0))
                    .reasonForAdmission("Severe Angina Pectoris evaluation and inpatient monitoring")
                    .clinicalSummary("Patient presented with chest pain on exertion. Admitted for cardiac enzyme monitoring and observation.")
                    .status(AdmissionStatus.ADMITTED)
                    .expectedDischargeDate(LocalDate.now().plusDays(3))
                    .createdBy("admin")
                    .build());

            // Bed Allocation Record
            bedAllocationRepository.save(BedAllocation.builder()
                    .admission(admission)
                    .patient(patient1)
                    .ward(genWard)
                    .bed(bed1)
                    .allocationType(AllocationType.INITIAL)
                    .startDatetime(java.time.LocalDateTime.now().minusDays(2))
                    .allocatedBy("admin")
                    .reason("Initial IPD Admission")
                    .build());

            // Vitals
            inpatientVitalRepository.save(InpatientVital.builder()
                    .admission(admission)
                    .patient(patient1)
                    .recordedBy("Anitha Raman")
                    .recordedAt(java.time.LocalDateTime.now().minusHours(4))
                    .temperature(new BigDecimal("37.0"))
                    .pulse(74)
                    .bloodPressure("122/82")
                    .respiratoryRate(16)
                    .oxygenSaturation(99)
                    .heightCm(new BigDecimal("172.0"))
                    .weightKg(new BigDecimal("74.5"))
                    .painScore(2)
                    .notes("Patient comfortable in bed. Normal cardiac rhythm.")
                    .build());

            // Nursing Note
            nursingNoteRepository.save(NursingNote.builder()
                    .noteId("NN-2026-000001")
                    .admission(admission)
                    .patient(patient1)
                    .nurse(nurseUser)
                    .noteDatetime(java.time.LocalDateTime.now().minusHours(3))
                    .noteType(NursingNoteType.ROUTINE_NOTE)
                    .noteText("Routine round completed. Vitals stable. IV saline lock patent. Patient reports no active chest pain.")
                    .build());

            // Doctor Progress Note
            progressNoteRepository.save(InpatientProgressNote.builder()
                    .progressNoteId("PN-2026-000001")
                    .admission(admission)
                    .patient(patient1)
                    .doctor(doctor)
                    .noteDatetime(java.time.LocalDateTime.now().minusHours(2))
                    .clinicalAssessment("Angina Pectoris - Improving under medical therapy")
                    .progressSummary("Serial ECGs show non-specific T-wave changes without ST elevation. Cardiac enzymes negative.")
                    .diagnosisUpdate("Stable Angina Pectoris (ICD-10: I20.8)")
                    .treatmentUpdate("Continue Amlodipine 5mg OD, Nitroglycerin sublingual PRN.")
                    .followUpPlan("Plan Echocardiogram tomorrow morning. Consider discharge on Day 4 if stable.")
                    .build());
        }
    }

    private void seedPhase5Data() {
        if (medicineCategoryRepository.count() > 0) return;

        log.info("Seeding Phase 5 Pharmacy & Inventory Demo Data...");

        // 1. Medicine Categories
        MedicineCategory analgesics = medicineCategoryRepository.save(MedicineCategory.builder()
                .categoryCode("CAT-ANALGESICS")
                .categoryName("Analgesics & Antipyretics")
                .description("Pain relievers and fever reducers")
                .isActive(true)
                .build());

        MedicineCategory antibiotics = medicineCategoryRepository.save(MedicineCategory.builder()
                .categoryCode("CAT-ANTIBIOTICS")
                .categoryName("Antibiotics & Anti-infectives")
                .description("Bacterial infection treatments")
                .isActive(true)
                .build());

        MedicineCategory cardioCat = medicineCategoryRepository.save(MedicineCategory.builder()
                .categoryCode("CAT-CARDIO")
                .categoryName("Cardiovascular & Antihypertensives")
                .description("Heart and blood pressure medications")
                .isActive(true)
                .build());

        MedicineCategory gastroCat = medicineCategoryRepository.save(MedicineCategory.builder()
                .categoryCode("CAT-GASTRO")
                .categoryName("Gastrointestinal")
                .description("Antacids, PPIs, and digestive health")
                .isActive(true)
                .build());

        MedicineCategory antidiabeticCat = medicineCategoryRepository.save(MedicineCategory.builder()
                .categoryCode("CAT-DIABETIC")
                .categoryName("Antidiabetic")
                .description("Blood sugar control medications")
                .isActive(true)
                .build());

        // 2. Fictional Indian Suppliers
        Supplier supplier1 = supplierRepository.save(Supplier.builder()
                .supplierCode("SUP-2026-000001")
                .supplierName("MedPlus Supply Solutions Pvt Ltd")
                .contactPerson("Suresh Verma")
                .phone("+919876599001")
                .email("sales@medplus-distributors.in")
                .addressLine1("Plot 45, Industrial Suburb")
                .addressLine2("Peenya 2nd Stage")
                .city("Bengaluru")
                .district("Bengaluru Urban")
                .state("Karnataka")
                .pincode("560058")
                .gstNumber("29AABCM1234H1Z5")
                .drugLicenseNumber("KA-BGL-2022-DL-8874")
                .paymentTerms("Net 30 Days")
                .isActive(true)
                .build());

        Supplier supplier2 = supplierRepository.save(Supplier.builder()
                .supplierCode("SUP-2026-000002")
                .supplierName("Cipla Pharma Distributors India")
                .contactPerson("Venkatesh Iyer")
                .phone("+919876599002")
                .email("contact@cipla-distributors.in")
                .addressLine1("12 Anna Salai")
                .addressLine2("Guindy")
                .city("Chennai")
                .district("Chennai")
                .state("Tamil Nadu")
                .pincode("600032")
                .gstNumber("33AABCC5678J1Z2")
                .drugLicenseNumber("TN-CHN-2021-DL-4321")
                .paymentTerms("Net 15 Days")
                .isActive(true)
                .build());

        // 3. Update Existing Medicines with Categories & Stock Parameters
        medicineRepository.findByMedicineCode("MED-PCM-500").ifPresent(m -> {
            m.setCategory(analgesics);
            m.setReorderLevel(100);
            m.setMaximumStockLevel(1000);
            m.setUnit("Strip (10 Tab)");
            medicineRepository.save(m);
        });

        medicineRepository.findByMedicineCode("MED-AMLO-5").ifPresent(m -> {
            m.setCategory(cardioCat);
            m.setReorderLevel(50);
            m.setMaximumStockLevel(500);
            m.setUnit("Strip (10 Tab)");
            medicineRepository.save(m);
        });

        medicineRepository.findByMedicineCode("MED-MET-500").ifPresent(m -> {
            m.setCategory(antidiabeticCat);
            m.setReorderLevel(60);
            m.setMaximumStockLevel(600);
            m.setUnit("Strip (10 Tab)");
            medicineRepository.save(m);
        });

        medicineRepository.findByMedicineCode("MED-AZI-500").ifPresent(m -> {
            m.setCategory(antibiotics);
            m.setReorderLevel(30);
            m.setMaximumStockLevel(300);
            m.setUnit("Strip (3 Tab)");
            medicineRepository.save(m);
        });

        medicineRepository.findByMedicineCode("MED-PAN-40").ifPresent(m -> {
            m.setCategory(gastroCat);
            m.setReorderLevel(40);
            m.setMaximumStockLevel(400);
            m.setUnit("Strip (10 Tab)");
            medicineRepository.save(m);
        });

        Medicine pcm = medicineRepository.findByMedicineCode("MED-PCM-500").orElse(null);
        Medicine amlo = medicineRepository.findByMedicineCode("MED-AMLO-5").orElse(null);

        if (pcm != null) {
            MedicineBatch batchPcm1 = medicineBatchRepository.save(MedicineBatch.builder()
                    .batchId("BAT-2026-000001")
                    .medicine(pcm)
                    .supplier(supplier1)
                    .batchNumber("PCM-2026-A")
                    .manufacturingDate(LocalDate.of(2025, 11, 1))
                    .expiryDate(LocalDate.of(2026, 11, 1))
                    .purchaseRate(new BigDecimal("12.50"))
                    .mrp(new BigDecimal("25.00"))
                    .sellingRate(new BigDecimal("22.00"))
                    .quantityReceived(500)
                    .quantityAvailable(450)
                    .quantityReserved(0)
                    .quantityDamaged(0)
                    .quantityExpired(0)
                    .storageLocation("Shelf A-1")
                    .status(BatchStatus.AVAILABLE)
                    .build());

            inventoryTransactionRepository.save(InventoryTransaction.builder()
                    .transactionId("INV-2026-000001")
                    .medicine(pcm)
                    .batch(batchPcm1)
                    .transactionType(InventoryTransactionType.OPENING_STOCK)
                    .quantity(500)
                    .unitCost(new BigDecimal("12.50"))
                    .referenceType("SEED_DATA")
                    .referenceId("INIT-001")
                    .remarks("Initial opening stock")
                    .performedBy("System")
                    .transactionDatetime(java.time.LocalDateTime.now().minusDays(30))
                    .build());
        }

        if (amlo != null) {
            MedicineBatch batchAmlo1 = medicineBatchRepository.save(MedicineBatch.builder()
                    .batchId("BAT-2026-000002")
                    .medicine(amlo)
                    .supplier(supplier2)
                    .batchNumber("AML-2026-X")
                    .manufacturingDate(LocalDate.of(2025, 6, 1))
                    .expiryDate(LocalDate.of(2027, 6, 1))
                    .purchaseRate(new BigDecimal("18.00"))
                    .mrp(new BigDecimal("35.00"))
                    .sellingRate(new BigDecimal("32.00"))
                    .quantityReceived(200)
                    .quantityAvailable(180)
                    .storageLocation("Shelf B-3")
                    .status(BatchStatus.AVAILABLE)
                    .build());

            inventoryTransactionRepository.save(InventoryTransaction.builder()
                    .transactionId("INV-2026-000002")
                    .medicine(amlo)
                    .batch(batchAmlo1)
                    .transactionType(InventoryTransactionType.OPENING_STOCK)
                    .quantity(200)
                    .unitCost(new BigDecimal("18.00"))
                    .referenceType("SEED_DATA")
                    .referenceId("INIT-002")
                    .remarks("Initial opening stock")
                    .performedBy("System")
                    .transactionDatetime(java.time.LocalDateTime.now().minusDays(20))
                    .build());
        }

        // 4. Sample Purchase Order & Goods Receipt
        PurchaseOrder po1 = purchaseOrderRepository.save(PurchaseOrder.builder()
                .purchaseOrderId("PO-2026-000001")
                .supplier(supplier1)
                .orderDate(LocalDate.now().minusDays(5))
                .expectedDeliveryDate(LocalDate.now().plusDays(2))
                .status(PurchaseOrderStatus.RECEIVED)
                .subtotal(new BigDecimal("2500.00"))
                .taxAmount(new BigDecimal("300.00"))
                .discountAmount(new BigDecimal("50.00"))
                .grandTotal(new BigDecimal("2750.00"))
                .notes("Emergency stock replenishment for PCM")
                .createdBy("admin")
                .build());

        if (pcm != null) {
            purchaseOrderItemRepository.save(PurchaseOrderItem.builder()
                    .purchaseOrder(po1)
                    .medicine(pcm)
                    .orderedQuantity(200)
                    .receivedQuantity(200)
                    .unitCost(new BigDecimal("12.50"))
                    .taxPercentage(new BigDecimal("12.00"))
                    .discountAmount(new BigDecimal("50.00"))
                    .lineTotal(new BigDecimal("2750.00"))
                    .build());
        }

        GoodsReceipt grn1 = goodsReceiptRepository.save(GoodsReceipt.builder()
                .goodsReceiptId("GRN-2026-000001")
                .purchaseOrder(po1)
                .supplier(supplier1)
                .receiptDate(LocalDate.now().minusDays(2))
                .invoiceNumber("INV-MEDPLUS-9982")
                .invoiceDate(LocalDate.now().minusDays(3))
                .receivedBy("rahul_verma")
                .remarks("Inspected and verified batch quantities")
                .build());

        if (pharmacyDispensingRepository.count() == 0) {
            Patient p = patientRepository.findAll().stream().findFirst().orElse(null);
            Medicine m = medicineRepository.findAll().stream().findFirst().orElse(null);
            MedicineBatch b = medicineBatchRepository.findAll().stream().findFirst().orElse(null);
            if (p != null && m != null && b != null) {
                pharmacyDispensingRepository.save(PharmacyDispensing.builder()
                        .dispensingId("DSP-2026-000001")
                        .patient(p)
                        .medicine(m)
                        .batch(b)
                        .prescribedQuantity(10)
                        .dispensedQuantity(10)
                        .remainingQuantity(0)
                        .dispensedBy("pharmacist_rahul")
                        .dispensedDatetime(java.time.LocalDateTime.now().minusDays(1))
                        .status(DispensingStatus.FULLY_DISPENSED)
                        .remarks("Initial sample dispensing for test")
                        .build());
            }
        }

        log.info("Phase 5 Pharmacy & Inventory Demo Data seeded successfully.");

        // Phase 6 Charge Master & Demo Billing Data
        if (chargeMasterRepository.count() == 0) {
            chargeMasterRepository.save(ChargeMaster.builder()
                    .chargeCode("CHG-OPD-CONSULT")
                    .chargeName("General OPD Consultation")
                    .chargeCategory(ChargeCategory.CONSULTATION)
                    .description("Routine outpatient consultation fee")
                    .unit("per consultation")
                    .baseRate(new BigDecimal("500.00"))
                    .taxPercentage(BigDecimal.ZERO)
                    .isActive(true)
                    .build());

            chargeMasterRepository.save(ChargeMaster.builder()
                    .chargeCode("CHG-SPEC-CONSULT")
                    .chargeName("Specialist Consultation")
                    .chargeCategory(ChargeCategory.CONSULTATION)
                    .description("Specialist / Super-specialist doctor consultation")
                    .unit("per consultation")
                    .baseRate(new BigDecimal("1000.00"))
                    .taxPercentage(BigDecimal.ZERO)
                    .isActive(true)
                    .build());

            chargeMasterRepository.save(ChargeMaster.builder()
                    .chargeCode("CHG-EMERGENCY")
                    .chargeName("Emergency Consultation & Care")
                    .chargeCategory(ChargeCategory.EMERGENCY)
                    .description("Urgent casualty consultation and emergency triage")
                    .unit("per visit")
                    .baseRate(new BigDecimal("1500.00"))
                    .taxPercentage(BigDecimal.ZERO)
                    .isActive(true)
                    .build());

            chargeMasterRepository.save(ChargeMaster.builder()
                    .chargeCode("CHG-LAB-CBC")
                    .chargeName("Complete Blood Count (CBC)")
                    .chargeCategory(ChargeCategory.LABORATORY)
                    .description("Standard 5-part differential blood cell count")
                    .unit("per test")
                    .baseRate(new BigDecimal("350.00"))
                    .taxPercentage(BigDecimal.ZERO)
                    .isActive(true)
                    .build());

            chargeMasterRepository.save(ChargeMaster.builder()
                    .chargeCode("CHG-LAB-HBA1C")
                    .chargeName("Glycated Hemoglobin (HbA1c)")
                    .chargeCategory(ChargeCategory.LABORATORY)
                    .description("Diabetes monitoring test")
                    .unit("per test")
                    .baseRate(new BigDecimal("550.00"))
                    .taxPercentage(BigDecimal.ZERO)
                    .isActive(true)
                    .build());

            chargeMasterRepository.save(ChargeMaster.builder()
                    .chargeCode("CHG-RAD-XRAY")
                    .chargeName("Chest X-Ray PA View")
                    .chargeCategory(ChargeCategory.RADIOLOGY)
                    .description("Digital radiograph chest PA view")
                    .unit("per view")
                    .baseRate(new BigDecimal("800.00"))
                    .taxPercentage(BigDecimal.ZERO)
                    .isActive(true)
                    .build());

            chargeMasterRepository.save(ChargeMaster.builder()
                    .chargeCode("CHG-BED-GENERAL")
                    .chargeName("General Ward Bed")
                    .chargeCategory(ChargeCategory.BED)
                    .description("Standard multi-bed ward stay per day")
                    .unit("per day")
                    .baseRate(new BigDecimal("1500.00"))
                    .taxPercentage(BigDecimal.ZERO)
                    .isActive(true)
                    .build());

            chargeMasterRepository.save(ChargeMaster.builder()
                    .chargeCode("CHG-REGISTRATION")
                    .chargeName("Patient Registration Fee")
                    .chargeCategory(ChargeCategory.REGISTRATION)
                    .description("One-time hospital registration and file opening fee")
                    .unit("one-time")
                    .baseRate(new BigDecimal("200.00"))
                    .taxPercentage(BigDecimal.ZERO)
                    .isActive(true)
                    .build());
        }

        // Demo Bill 1: Paid OPD Bill for Patient 1
        if (billRepository.count() == 0) {
            Patient demoPatient = patientRepository.findAll().stream().findFirst().orElse(null);
            if (demoPatient != null) {
                BillingAccount account = billingAccountRepository.findByPatientId(demoPatient.getId())
                        .orElseGet(() -> billingAccountRepository.save(BillingAccount.builder()
                                .accountNumber("ACC-2026-000001")
                                .patient(demoPatient)
                                .accountStatus(BillingAccountStatus.ACTIVE)
                                .creditLimit(new BigDecimal("10000.00"))
                                .currentBalance(BigDecimal.ZERO)
                                .build()));

                Bill demoBill = billRepository.save(Bill.builder()
                        .billNumber("BILL-2026-000001")
                        .patient(demoPatient)
                        .billingAccount(account)
                        .billType(BillType.OPD)
                        .billDate(LocalDate.now())
                        .dueDate(LocalDate.now().plusDays(7))
                        .subtotal(new BigDecimal("850.00"))
                        .discountAmount(BigDecimal.ZERO)
                        .taxAmount(BigDecimal.ZERO)
                        .roundOff(BigDecimal.ZERO)
                        .grandTotal(new BigDecimal("850.00"))
                        .paidAmount(new BigDecimal("850.00"))
                        .refundedAmount(BigDecimal.ZERO)
                        .outstandingAmount(BigDecimal.ZERO)
                        .status(BillStatus.PAID)
                        .createdBy("billing_vikram")
                        .notes("Initial OPD Consultation & Lab Test Bill")
                        .items(new ArrayList<>())
                        .build());

                billItemRepository.save(BillItem.builder()
                        .bill(demoBill)
                        .chargeCode("CHG-OPD-CONSULT")
                        .description("General OPD Consultation")
                        .sourceType(BillSourceType.CONSULTATION)
                        .sourceId(1L)
                        .quantity(1)
                        .unitRate(new BigDecimal("500.00"))
                        .lineTotal(new BigDecimal("500.00"))
                        .build());

                billItemRepository.save(BillItem.builder()
                        .bill(demoBill)
                        .chargeCode("CHG-LAB-CBC")
                        .description("Complete Blood Count (CBC)")
                        .sourceType(BillSourceType.LAB)
                        .sourceId(1L)
                        .quantity(1)
                        .unitRate(new BigDecimal("350.00"))
                        .lineTotal(new BigDecimal("350.00"))
                        .build());

                Payment demoPay = paymentRepository.save(Payment.builder()
                        .paymentNumber("PAY-2026-000001")
                        .bill(demoBill)
                        .patient(demoPatient)
                        .paymentDate(java.time.LocalDateTime.now())
                        .amount(new BigDecimal("850.00"))
                        .paymentMethod(PaymentMethod.UPI)
                        .referenceNumber("UPI-8899110293")
                        .receivedBy("billing_vikram")
                        .remarks("Full payment received via UPI")
                        .status(PaymentStatus.COMPLETED)
                        .build());

                patientLedgerEntryRepository.save(PatientLedgerEntry.builder()
                        .ledgerNumber("LEG-2026-000001")
                        .patient(demoPatient)
                        .bill(demoBill)
                        .entryType(LedgerEntryType.BILL)
                        .debitAmount(new BigDecimal("850.00"))
                        .creditAmount(BigDecimal.ZERO)
                        .balanceAfter(new BigDecimal("850.00"))
                        .description("Initial OPD Invoice: BILL-2026-000001")
                        .entryDate(java.time.LocalDateTime.now().minusHours(2))
                        .build());

                patientLedgerEntryRepository.save(PatientLedgerEntry.builder()
                        .ledgerNumber("LEG-2026-000002")
                        .patient(demoPatient)
                        .bill(demoBill)
                        .payment(demoPay)
                        .entryType(LedgerEntryType.PAYMENT)
                        .debitAmount(BigDecimal.ZERO)
                        .creditAmount(new BigDecimal("850.00"))
                        .balanceAfter(BigDecimal.ZERO)
                        .description("UPI Payment received: PAY-2026-000001")
                        .entryDate(java.time.LocalDateTime.now().minusHours(1))
                        .build());
            }
        }

        log.info("Phase 6 Billing & Financial Management Demo Data seeded successfully.");
    }
}
