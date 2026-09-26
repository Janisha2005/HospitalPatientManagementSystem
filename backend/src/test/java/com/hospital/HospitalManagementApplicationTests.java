package com.hospital;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.*;
import com.hospital.entity.AppointmentStatus;
import com.hospital.entity.AppointmentType;
import com.hospital.entity.OpdVisitStatus;
import com.hospital.validation.IndiaValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class HospitalManagementApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    private String getAdminToken() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("admin")
                .password("Admin@123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseBody, ApiResponse.class);
        return (String) ((java.util.Map<?, ?>) apiResponse.getData()).get("accessToken");
    }

    private String getDoctorToken() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("doc_smith")
                .password("Doctor@123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseBody, ApiResponse.class);
        return (String) ((java.util.Map<?, ?>) apiResponse.getData()).get("accessToken");
    }

    private String getPatientToken() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("patient_john")
                .password("Patient@123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseBody, ApiResponse.class);
        return (String) ((java.util.Map<?, ?>) apiResponse.getData()).get("accessToken");
    }

    private String getBillingOfficerToken() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("billing_vikram")
                .password("Billing@123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseBody, ApiResponse.class);
        return (String) ((java.util.Map<?, ?>) apiResponse.getData()).get("accessToken");
    }

    @Test
    @DisplayName("1. Admin Login Works")
    void testAdminLoginSuccess() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("Admin@123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.username").value("admin"))
                .andExpect(jsonPath("$.data.user.role").value("ADMIN"));
    }

    @Test
    @DisplayName("2. Unauthorized Access Rejected")
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @DisplayName("3. Role Authorization - Doctor Cannot Access Admin Endpoint")
    void testRoleAuthorizationForbidden() throws Exception {
        String doctorToken = getDoctorToken();

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You do not have permission to access this resource"));
    }

    @Test
    @DisplayName("4. Indian Phone Number Validation & Normalization")
    void testIndianPhoneValidation() {
        assertTrue(IndiaValidationUtils.isValidIndianPhone("9876543210"));
        assertTrue(IndiaValidationUtils.isValidIndianPhone("+919876543210"));
        assertTrue(IndiaValidationUtils.isValidIndianPhone("+91 9876543210"));

        assertFalse(IndiaValidationUtils.isValidIndianPhone("1234567890"));
        assertFalse(IndiaValidationUtils.isValidIndianPhone("0000000000"));
        assertFalse(IndiaValidationUtils.isValidIndianPhone("12345"));

        assertEquals("+919876543210", IndiaValidationUtils.normalizeIndianPhone("9876543210"));
        assertEquals("+919876543210", IndiaValidationUtils.normalizeIndianPhone("+919876543210"));
    }

    @Test
    @DisplayName("5. Indian PIN Code Validation")
    void testIndianPincodeValidation() {
        assertTrue(IndiaValidationUtils.isValidPincode("600040"));
        assertTrue(IndiaValidationUtils.isValidPincode("560001"));

        assertFalse(IndiaValidationUtils.isValidPincode("012345")); // Cannot start with 0
        assertFalse(IndiaValidationUtils.isValidPincode("12345"));  // 5 digits
        assertFalse(IndiaValidationUtils.isValidPincode("ABC123")); // Non-numeric
    }

    @Test
    @DisplayName("6. Doctor Creation with INR Currency")
    void testDoctorCreationWithINR() throws Exception {
        String adminToken = getAdminToken();

        DoctorRequestDto request = DoctorRequestDto.builder()
                .firstName("Ramesh")
                .lastName("Verma")
                .email("ramesh.verma@pulse-hospital.in")
                .phone("+919876543300")
                .specialization("General Medicine")
                .qualification("MBBS, MD")
                .licenseNumber("MCI-DL-2019-1234")
                .departmentId(1L)
                .consultationFee(new BigDecimal("800.00"))
                .addressLine1("12 Connaught Place")
                .city("New Delhi")
                .state("Delhi")
                .pincode("110001")
                .country("India")
                .joiningDate(LocalDate.of(2025, 1, 1))
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/doctors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.doctorId").exists())
                .andExpect(jsonPath("$.data.currency").value("INR"))
                .andExpect(jsonPath("$.data.consultationFee").value(800.00));
    }

    @Test
    @DisplayName("7. Patient Registration with Indian Address")
    void testPatientRegistrationWithIndianAddress() throws Exception {
        String adminToken = getAdminToken();

        PatientRequestDto createRequest = PatientRequestDto.builder()
                .firstName("Deepak")
                .lastName("Patel")
                .dateOfBirth(LocalDate.of(1991, 5, 20))
                .gender("Male")
                .bloodGroup("B+")
                .phone("+919898989898")
                .email("deepak.patel@example.com")
                .addressLine1("45 CG Road")
                .city("Ahmedabad")
                .district("Ahmedabad")
                .state("Gujarat")
                .pincode("380009")
                .country("India")
                .emergencyContactName("Sanjay Patel")
                .emergencyContactPhone("+919797979797")
                .emergencyContactRelationship("Brother")
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.patientId").value(org.hamcrest.Matchers.startsWith("PAT-")))
                .andExpect(jsonPath("$.data.state").value("Gujarat"))
                .andExpect(jsonPath("$.data.phone").value("+919898989898"));
    }

    @Test
    @DisplayName("8. Invalid Phone Format Rejected")
    void testInvalidPhoneFormat() throws Exception {
        String adminToken = getAdminToken();

        PatientRequestDto createRequest = PatientRequestDto.builder()
                .firstName("Test")
                .lastName("User")
                .dateOfBirth(LocalDate.of(1995, 1, 1))
                .gender("Male")
                .phone("12345") // Invalid
                .emergencyContactName("Contact")
                .emergencyContactPhone("+919876543210")
                .emergencyContactRelationship("Parent")
                .build();

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // --- PHASE 2 CLINICAL TESTS ---

    @Test
    @DisplayName("9. Doctor Availability & Time Slots Calculation")
    void testDoctorAvailabilityAndSlots() throws Exception {
        String adminToken = getAdminToken();

        DoctorAvailabilityRequest availReq = DoctorAvailabilityRequest.builder()
                .dayOfWeek(DayOfWeek.SUNDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(12, 0))
                .slotDurationMinutes(30)
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/doctors/1/availability")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(availReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/doctors/1/available-slots?date=2026-10-05") // A Monday
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("10. Appointment Creation & Double Booking 409 Conflict Rejection")
    void testAppointmentCreationAndDoubleBookingConflict() throws Exception {
        String adminToken = getAdminToken();
        LocalDate targetDate = LocalDate.now().plusDays(1);
        // Ensure date falls on non-Sunday
        if (targetDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            targetDate = targetDate.plusDays(1);
        }

        AppointmentCreateRequest req1 = AppointmentCreateRequest.builder()
                .patientId(1L)
                .doctorId(1L)
                .departmentId(1L)
                .appointmentDate(targetDate)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .appointmentType(AppointmentType.NEW_CONSULTATION)
                .reasonForVisit("Routine Cardiology Checkup")
                .build();

        // 1. Create first appointment
        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.appointmentId").value(org.hamcrest.Matchers.startsWith("APT-")));

        // 2. Overlapping appointment for doctor at 14:15 - should be rejected with 409 CONFLICT
        AppointmentCreateRequest req2 = AppointmentCreateRequest.builder()
                .patientId(2L)
                .doctorId(1L)
                .departmentId(1L)
                .appointmentDate(targetDate)
                .startTime(LocalTime.of(14, 15))
                .endTime(LocalTime.of(14, 45))
                .appointmentType(AppointmentType.FOLLOW_UP)
                .reasonForVisit("Overlapping slot test")
                .build();

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("11. OPD Patient Check-In & Queue Workflow")
    void testOPDCheckInAndQueue() throws Exception {
        String adminToken = getAdminToken();
        LocalDate today = LocalDate.now();

        AppointmentCreateRequest req = AppointmentCreateRequest.builder()
                .patientId(2L)
                .doctorId(1L)
                .departmentId(1L)
                .appointmentDate(today)
                .startTime(LocalTime.of(15, 0))
                .endTime(LocalTime.of(15, 30))
                .appointmentType(AppointmentType.NEW_CONSULTATION)
                .reasonForVisit("BP examination")
                .build();

        MvcResult aptResult = mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String json = aptResult.getResponse().getContentAsString();
        ApiResponse<?> responseObj = objectMapper.readValue(json, ApiResponse.class);
        Integer createdId = (Integer) ((java.util.Map<?, ?>) responseObj.getData()).get("id");

        // Perform Check-in
        mockMvc.perform(post("/api/appointments/" + createdId + "/check-in")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.opdVisitId").value(org.hamcrest.Matchers.startsWith("OPD-")))
                .andExpect(jsonPath("$.data.visitStatus").value("WAITING"));

        // Duplicate Check-in should fail with 409 CONFLICT
        mockMvc.perform(post("/api/appointments/" + createdId + "/check-in")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("12. Doctor OPD Consultation Complete Workflow")
    void testDoctorConsultationWorkflow() throws Exception {
        String doctorToken = getDoctorToken();

        // 1. Get today's queue
        MvcResult queueRes = mockMvc.perform(get("/api/opd/queue/today")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        // 2. Complete consultation on first queue item if present
        OpdVisitUpdateRequest updateReq = OpdVisitUpdateRequest.builder()
                .chiefComplaint("Patient complained of headache")
                .clinicalNotes("BP 120/80, normal reflexes")
                .diagnosis("Tension Headache")
                .treatmentPlan("Rest, hydration, and Paracetamol 500mg as needed")
                .vitalTemperature(new BigDecimal("37.0"))
                .vitalPulse(72)
                .vitalBloodPressure("120/80")
                .vitalRespiratoryRate(16)
                .vitalOxygenSaturation(99)
                .build();

        mockMvc.perform(post("/api/opd/queue/1/complete")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.visitStatus").value("COMPLETED"));
    }

    @Test
    @DisplayName("13. Patient Ownership Privacy Protection")
    void testPatientOwnershipPrivacy() throws Exception {
        String patientToken = getPatientToken();

        // Patient accesses own appointments -> OK
        mockMvc.perform(get("/api/appointments")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // --- PHASE 3 CLINICAL EHR & ANCILLARY TESTS ---

    @Test
    @DisplayName("14. Patient EHR Record & Timeline Aggregation")
    void testEhrRecordAndTimeline() throws Exception {
        String doctorToken = getDoctorToken();

        // Get patient 1 timeline
        mockMvc.perform(get("/api/ehr/patients/1/timeline")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1));

        // Create an EHR note
        EhrRecordRequest ehrReq = EhrRecordRequest.builder()
                .patientId(1L)
                .doctorId(1L)
                .departmentId(1L)
                .recordType(com.hospital.entity.EhrRecordType.GENERAL_NOTE)
                .clinicalSummary("Annual Health Checkup Note: Patient in good overall health.")
                .build();

        mockMvc.perform(post("/api/ehr/records")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ehrReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clinicalSummary").value(org.hamcrest.Matchers.startsWith("Annual Health Checkup")));
    }

    @Test
    @DisplayName("15. Patient Allergy & Chronic Condition Management")
    void testAllergiesAndConditions() throws Exception {
        String doctorToken = getDoctorToken();

        // Add Allergy
        AllergyRequest allergyReq = AllergyRequest.builder()
                .allergen("Sulfa Drugs")
                .allergyType("Medication")
                .severity(com.hospital.entity.AllergySeverity.SEVERE)
                .reaction("Severe rash, swelling")
                .build();

        mockMvc.perform(post("/api/patients/1/allergies")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(allergyReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.allergen").value("Sulfa Drugs"));

        // Add Condition
        ConditionRequest condReq = ConditionRequest.builder()
                .conditionName("Type 2 Diabetes Mellitus")
                .description("ICD-10: E11 - Controlled with lifestyle and Metformin")
                .diagnosedDate(LocalDate.of(2020, 1, 15))
                .status(com.hospital.entity.ConditionStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/patients/1/conditions")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(condReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.conditionName").value("Type 2 Diabetes Mellitus"));
    }

    @Test
    @DisplayName("16. Prescription Creation & Immutability Upon Issue")
    void testPrescriptionWorkflowAndImmutability() throws Exception {
        String doctorToken = getDoctorToken();

        // 1. Create draft prescription
        PrescriptionCreateRequest createReq = PrescriptionCreateRequest.builder()
                .patientId(1L)
                .doctorId(1L)
                .clinicalNotes("Prescription for hypertension and mild infection")
                .items(java.util.List.of(
                        PrescriptionItemRequest.builder()
                                .medicineName("Paracetamol 500mg")
                                .dosage("500 mg")
                                .frequency(com.hospital.entity.PrescriptionFrequency.THREE_TIMES_DAILY)
                                .route(com.hospital.entity.PrescriptionRoute.ORAL)
                                .durationValue(5)
                                .durationUnit("Days")
                                .instructions("Take after meals")
                                .build()
                ))
                .build();

        MvcResult result = mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PrescriptionResponse rx = objectMapper.readValue(json, PrescriptionResponse.class);
        Long rxId = rx.getId();

        // 2. Issue prescription
        mockMvc.perform(put("/api/prescriptions/" + rxId + "/issue")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ISSUED"));

        // 3. Attempting to update ISSUED prescription must be rejected (400 BAD REQUEST)
        PrescriptionCreateRequest updateReq = PrescriptionCreateRequest.builder()
                .patientId(1L)
                .doctorId(1L)
                .clinicalNotes("Attempted modification of issued prescription")
                .items(java.util.List.of(
                        PrescriptionItemRequest.builder()
                                .medicineName("Amlodipine 5mg")
                                .dosage("5 mg")
                                .frequency(com.hospital.entity.PrescriptionFrequency.ONCE_DAILY)
                                .route(com.hospital.entity.PrescriptionRoute.ORAL)
                                .durationValue(30)
                                .durationUnit("Days")
                                .build()
                ))
                .build();

        mockMvc.perform(put("/api/prescriptions/" + rxId)
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("17. Laboratory Order, Sample Collection, Result Entry, Verification")
    void testLaboratoryWorkflow() throws Exception {
        String doctorToken = getDoctorToken();

        // 1. Create Lab Order
        LabOrderRequest orderReq = LabOrderRequest.builder()
                .patientId(1L)
                .doctorId(1L)
                .departmentId(1L)
                .priority(com.hospital.entity.OrderPriority.ROUTINE)
                .clinicalNote("Routine CBC & Fasting Blood Sugar test")
                .items(java.util.List.of(
                        LabOrderItemRequest.builder().labTestId(1L).build()
                ))
                .build();

        MvcResult createRes = mockMvc.perform(post("/api/lab/orders")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ORDERED"))
                .andReturn();

        String json = createRes.getResponse().getContentAsString();
        LabOrderResponse labOrder = objectMapper.readValue(json, LabOrderResponse.class);
        Long orderId = labOrder.getId();
        Long itemId = labOrder.getItems().get(0).getId();

        // 2. Sample Collection
        mockMvc.perform(put("/api/lab/orders/" + orderId + "/collect-sample")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SAMPLE_COLLECTED"));

        // 3. Result Entry
        LabOrderItemRequest resultReq = LabOrderItemRequest.builder()
                .resultValue("14.5")
                .resultUnit("g/dL")
                .referenceRange("13.5-17.5 g/dL")
                .isAbnormal(false)
                .remarks("Normal hemoglobin")
                .build();

        mockMvc.perform(put("/api/lab/orders/" + orderId + "/items/" + itemId + "/result")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resultReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESULT_READY"));

        // 4. Verify Order
        mockMvc.perform(put("/api/lab/orders/" + orderId + "/verify")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("18. Radiology Order & Report Finalization")
    void testRadiologyWorkflow() throws Exception {
        String doctorToken = getDoctorToken();

        // 1. Create Radiology Order
        RadiologyOrderRequest orderReq = RadiologyOrderRequest.builder()
                .patientId(1L)
                .doctorId(1L)
                .departmentId(1L)
                .radiologyTestId(1L)
                .priority(com.hospital.entity.OrderPriority.URGENT)
                .clinicalIndication("Rule out chest infection or congestion")
                .build();

        MvcResult createRes = mockMvc.perform(post("/api/radiology/orders")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ORDERED"))
                .andReturn();

        String json = createRes.getResponse().getContentAsString();
        RadiologyOrderResponse radOrder = objectMapper.readValue(json, RadiologyOrderResponse.class);
        Long orderId = radOrder.getId();

        // 2. Draft & Finalize Report
        RadiologyReportRequest reportReq = RadiologyReportRequest.builder()
                .findings("Lungs are clear bilaterally. No focal infiltrate or pleural effusion.")
                .impression("Normal Chest X-Ray PA view.")
                .recommendations("Clinical correlation recommended if symptoms persist.")
                .isFinal(true)
                .build();

        mockMvc.perform(post("/api/radiology/orders/" + orderId + "/report")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reportReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINAL"));
    }

    // --- PHASE 4 IPD & WARD & BED TESTS ---

    @Test
    @DisplayName("19. Ward Creation & Duplicate Ward Code Rejection")
    void testWardCreationAndDuplicateRejection() throws Exception {
        String adminToken = getAdminToken();

        WardRequest request = WardRequest.builder()
                .wardCode("WARD-TEST-001")
                .wardName("Test Surgical Ward")
                .wardType(com.hospital.entity.WardType.GENERAL)
                .departmentId(1L)
                .floor("2nd Floor")
                .building("Block B")
                .genderPolicy(com.hospital.entity.WardGenderPolicy.MIXED)
                .capacity(15)
                .isActive(true)
                .build();

        // 1. Create Ward
        mockMvc.perform(post("/api/wards")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.wardCode").value("WARD-TEST-001"));

        // 2. Duplicate Ward Code -> 409 Conflict
        mockMvc.perform(post("/api/wards")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("20. Bed Creation & Available Bed Retrieval")
    void testBedCreationAndAvailableRetrieval() throws Exception {
        String adminToken = getAdminToken();

        BedRequest request = BedRequest.builder()
                .bedCode("TEST-BED-001")
                .wardId(1L)
                .bedNumber("T-01")
                .bedType(com.hospital.entity.BedType.STANDARD)
                .status(com.hospital.entity.BedStatus.AVAILABLE)
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/beds")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bedCode").value("TEST-BED-001"));

        mockMvc.perform(get("/api/beds/available?wardId=1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("21. IPD Admission Creation & One Active Admission Rule 409 Conflict")
    void testIpdAdmissionAndOneActiveAdmissionRule() throws Exception {
        String adminToken = getAdminToken();

        IpdAdmissionCreateRequest req1 = IpdAdmissionCreateRequest.builder()
                .patientId(2L) // Priya Nair
                .admittingDoctorId(1L)
                .departmentId(1L)
                .admissionType(com.hospital.entity.AdmissionType.ELECTIVE)
                .admissionDate(LocalDate.now())
                .reasonForAdmission("Elective cardiac evaluation and monitoring")
                .clinicalSummary("History of palpitations")
                .build();

        // 1. Create IPD Admission Request
        mockMvc.perform(post("/api/ipd/admissions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.admissionId").value(org.hamcrest.Matchers.startsWith("ADM-")))
                .andExpect(jsonPath("$.data.status").value("REQUESTED"));

        // 2. Second Active Admission Request for same patient -> 409 CONFLICT
        mockMvc.perform(post("/api/ipd/admissions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("22. IPD Admission Approval, Bed Allocation, Nursing & Discharge Workflow")
    void testIpdAdmissionWorkflowToDischarge() throws Exception {
        String adminToken = getAdminToken();
        String doctorToken = getDoctorToken();

        // 1. Create Admission
        IpdAdmissionCreateRequest req = IpdAdmissionCreateRequest.builder()
                .patientId(3L) // Vikram Singh
                .admittingDoctorId(1L)
                .departmentId(1L)
                .admissionType(com.hospital.entity.AdmissionType.EMERGENCY)
                .admissionDate(LocalDate.now())
                .reasonForAdmission("Acute chest discomfort evaluation")
                .build();

        MvcResult admResult = mockMvc.perform(post("/api/ipd/admissions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String json = admResult.getResponse().getContentAsString();
        ApiResponse<?> responseObj = objectMapper.readValue(json, ApiResponse.class);
        Integer admissionIdInt = (Integer) ((java.util.Map<?, ?>) responseObj.getData()).get("id");
        Long admissionId = admissionIdInt.longValue();

        // 2. Approve Admission
        mockMvc.perform(post("/api/ipd/admissions/" + admissionId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        // 3. Allocate Available Bed (Bed 3: GEN-A-003)
        BedAllocationRequest allocReq = BedAllocationRequest.builder()
                .wardId(1L)
                .bedId(3L)
                .allocationType(com.hospital.entity.AllocationType.INITIAL)
                .reason("Emergency IPD bed assignment")
                .build();

        mockMvc.perform(post("/api/ipd/admissions/" + admissionId + "/allocate-bed")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(allocReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 4. Record Inpatient Vitals
        InpatientVitalRequest vitalReq = InpatientVitalRequest.builder()
                .temperature(new BigDecimal("37.1"))
                .pulse(76)
                .bloodPressure("118/78")
                .respiratoryRate(16)
                .oxygenSaturation(99)
                .painScore(1)
                .notes("Patient resting comfortably.")
                .build();

        mockMvc.perform(post("/api/ipd/admissions/" + admissionId + "/vitals")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vitalReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // 5. Record Doctor Progress Note
        InpatientProgressNoteRequest progressReq = InpatientProgressNoteRequest.builder()
                .clinicalAssessment("Patient stable after emergency admission")
                .progressSummary("Serial ECGs clear. Troponin I levels within normal limits.")
                .diagnosisUpdate("Non-cardiac chest pain")
                .treatmentUpdate("Observation and discharge planning")
                .build();

        mockMvc.perform(post("/api/ipd/admissions/" + admissionId + "/progress-notes")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(progressReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // 6. Create Discharge Summary
        DischargeSummaryRequest summaryReq = DischargeSummaryRequest.builder()
                .admissionSummary("Emergency admission for acute chest discomfort.")
                .clinicalCourse("Monitored for 24h. Cardiac markers negative.")
                .finalDiagnosis("Non-cardiac chest pain")
                .conditionAtDischarge("Asymptomatic, hemodynamically stable")
                .followUpInstructions("Follow up in Cardiology OPD after 1 week.")
                .dischargeDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/ipd/admissions/" + admissionId + "/discharge-summary")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(summaryReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // 7. Execute Discharge -> Releases Bed to CLEANING state
        DischargeExecuteRequest dischargeReq = DischargeExecuteRequest.builder()
                .dischargeType(com.hospital.entity.DischargeType.NORMAL)
                .dischargeDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/ipd/admissions/" + admissionId + "/discharge")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dischargeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DISCHARGED"));
    }

    // --- PHASE 5 PHARMACY & INVENTORY TESTS ---

    private String getPharmacistToken() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("pharmacist_rahul")
                .password("Pharmacist@123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseBody, ApiResponse.class);
        return (String) ((java.util.Map<?, ?>) apiResponse.getData()).get("accessToken");
    }

    @Test
    @DisplayName("23. Pharmacist Login & Role Authentication Works")
    void testPharmacistLoginSuccess() throws Exception {
        String token = getPharmacistToken();
        assertNotNull(token);
    }

    @Test
    @DisplayName("24. Create Medicine Category & Duplicate Code Rejection")
    void testCategoryCreationAndDuplicateRejection() throws Exception {
        String pharmacistToken = getPharmacistToken();

        MedicineCategoryDto cat = MedicineCategoryDto.builder()
                .categoryCode("CAT-RESPIRATORY")
                .categoryName("Respiratory Medications")
                .description("Inhalers, bronchodilators, and antiasthmatics")
                .isActive(true)
                .build();

        // 1. Create Category
        mockMvc.perform(post("/api/pharmacy/categories")
                        .header("Authorization", "Bearer " + pharmacistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cat)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categoryCode").value("CAT-RESPIRATORY"));

        // 2. Duplicate Category Code -> 409 Conflict
        mockMvc.perform(post("/api/pharmacy/categories")
                        .header("Authorization", "Bearer " + pharmacistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cat)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("25. Supplier Registration with GST & Indian Phone Validation")
    void testSupplierRegistration() throws Exception {
        String pharmacistToken = getPharmacistToken();

        SupplierDto supplier = SupplierDto.builder()
                .supplierName("Apollo Pharmacy Distributors")
                .contactPerson("Rajeev Menon")
                .phone("+919876512345")
                .email("orders@apollo-distributors.in")
                .addressLine1("88 Mount Road")
                .city("Chennai")
                .state("Tamil Nadu")
                .pincode("600002")
                .gstNumber("33AABCX9988H1Z1")
                .drugLicenseNumber("TN-CHN-2023-DL-9912")
                .paymentTerms("Net 30 Days")
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/pharmacy/suppliers")
                        .header("Authorization", "Bearer " + pharmacistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplier)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.supplierCode").value(org.hamcrest.Matchers.startsWith("SUP-")));
    }

    @Test
    @DisplayName("26. Batch Creation & Expiry Validation")
    void testBatchCreationAndValidation() throws Exception {
        String pharmacistToken = getPharmacistToken();

        MedicineBatchDto batch = MedicineBatchDto.builder()
                .medicineId(1L)
                .supplierId(1L)
                .batchNumber("TEST-BATCH-2026")
                .manufacturingDate(LocalDate.of(2025, 1, 1))
                .expiryDate(LocalDate.of(2027, 12, 31))
                .purchaseRate(new BigDecimal("10.00"))
                .mrp(new BigDecimal("20.00"))
                .sellingRate(new BigDecimal("18.00"))
                .quantityReceived(100)
                .quantityAvailable(100)
                .storageLocation("Shelf A-2")
                .build();

        mockMvc.perform(post("/api/pharmacy/batches")
                        .header("Authorization", "Bearer " + pharmacistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batch)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.batchId").value(org.hamcrest.Matchers.startsWith("BAT-")));
    }

    @Test
    @DisplayName("27. Expired Batch Dispensing Rejection (409 Conflict)")
    void testExpiredBatchDispensingRejection() throws Exception {
        String pharmacistToken = getPharmacistToken();

        // Create an expired batch
        MedicineBatchDto expiredBatch = MedicineBatchDto.builder()
                .medicineId(1L)
                .batchNumber("EXPIRED-BATCH-001")
                .manufacturingDate(LocalDate.of(2023, 1, 1))
                .expiryDate(LocalDate.of(2024, 1, 1)) // Past expiry
                .purchaseRate(new BigDecimal("10.00"))
                .mrp(new BigDecimal("20.00"))
                .quantityReceived(50)
                .quantityAvailable(50)
                .build();

        MvcResult result = mockMvc.perform(post("/api/pharmacy/batches")
                        .header("Authorization", "Bearer " + pharmacistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expiredBatch)))
                .andExpect(status().isCreated())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        ApiResponse<?> responseObj = objectMapper.readValue(json, ApiResponse.class);
        Integer batchIdInt = (Integer) ((java.util.Map<?, ?>) responseObj.getData()).get("id");

        // Attempt dispensing from expired batch -> 409 Conflict
        DispenseRequest req = DispenseRequest.builder()
                .patientId(1L)
                .medicineId(1L)
                .batchId(batchIdInt.longValue())
                .prescribedQuantity(10)
                .dispensedQuantity(10)
                .build();

        mockMvc.perform(post("/api/pharmacy/dispensing")
                        .header("Authorization", "Bearer " + pharmacistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("28. Purchase Order Server-side Total Calculation")
    void testPurchaseOrderCreationAndCalculation() throws Exception {
        String pharmacistToken = getPharmacistToken();

        PurchaseOrderDto po = PurchaseOrderDto.builder()
                .supplierId(1L)
                .expectedDeliveryDate(LocalDate.now().plusDays(5))
                .notes("Test Purchase Order")
                .items(java.util.List.of(
                        PurchaseOrderItemDto.builder()
                                .medicineId(1L)
                                .orderedQuantity(100)
                                .unitCost(new BigDecimal("10.00"))
                                .taxPercentage(new BigDecimal("10.00"))
                                .discountAmount(new BigDecimal("50.00"))
                                .build()
                ))
                .build();

        mockMvc.perform(post("/api/pharmacy/purchase-orders")
                        .header("Authorization", "Bearer " + pharmacistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(po)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.grandTotal").value(1050.00)); // 1000 base + 100 tax - 50 discount
    }

    @Test
    @DisplayName("29. Goods Receipt (GRN) Stock Arrival & Inventory Transaction Logging")
    void testGoodsReceiptStockArrival() throws Exception {
        String pharmacistToken = getPharmacistToken();

        GoodsReceiptDto grn = GoodsReceiptDto.builder()
                .supplierId(1L)
                .receiptDate(LocalDate.now())
                .invoiceNumber("INV-GRN-1001")
                .remarks("Verified stock delivery")
                .items(java.util.List.of(
                        GoodsReceiptItemDto.builder()
                                .medicineId(1L)
                                .batchNumber("GRN-BATCH-001")
                                .quantityReceived(200)
                                .purchaseRate(new BigDecimal("15.00"))
                                .mrp(new BigDecimal("30.00"))
                                .expiryDate(LocalDate.now().plusYears(2))
                                .build()
                ))
                .build();

        mockMvc.perform(post("/api/pharmacy/goods-receipts")
                        .header("Authorization", "Bearer " + pharmacistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grn)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.goodsReceiptId").value(org.hamcrest.Matchers.startsWith("GRN-")));
    }

    @Test
    @DisplayName("30. Prescription Dispensing using FEFO Algorithm & Stock Deduction")
    void testFefoPrescriptionDispensing() throws Exception {
        String pharmacistToken = getPharmacistToken();

        DispenseRequest dispenseReq = DispenseRequest.builder()
                .patientId(1L)
                .medicineId(1L)
                .prescribedQuantity(10)
                .dispensedQuantity(10)
                .remarks("FEFO automatic dispensing test")
                .build();

        mockMvc.perform(post("/api/pharmacy/dispensing")
                        .header("Authorization", "Bearer " + pharmacistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dispenseReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].dispensedQuantity").value(10));
    }

    @Test
    @DisplayName("31. Authorized Stock Adjustment (IN & OUT)")
    void testStockAdjustment() throws Exception {
        String pharmacistToken = getPharmacistToken();

        StockAdjustmentDto adjustment = StockAdjustmentDto.builder()
                .medicineId(1L)
                .batchId(1L)
                .adjustmentQuantity(5)
                .direction("IN")
                .reason("Physical count difference")
                .remarks("Found extra stock during audit")
                .build();

        mockMvc.perform(post("/api/pharmacy/inventory/adjust")
                        .header("Authorization", "Bearer " + pharmacistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adjustment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactionType").value("STOCK_ADJUSTMENT_IN"));
    }

    @Test
    @DisplayName("32. Patient Medicine Return & Restocking Workflow")
    void testPatientReturnWorkflow() throws Exception {
        String pharmacistToken = getPharmacistToken();

        // First perform a dispensing so a valid dispensing record is guaranteed to exist
        DispenseRequest dispenseReq = DispenseRequest.builder()
                .patientId(1L)
                .medicineId(1L)
                .prescribedQuantity(10)
                .dispensedQuantity(10)
                .remarks("Dispense before return test")
                .build();

        String dispenseResp = mockMvc.perform(post("/api/pharmacy/dispensing")
                        .header("Authorization", "Bearer " + pharmacistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dispenseReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer dispensingId = com.jayway.jsonpath.JsonPath.read(dispenseResp, "$.data[0].id");

        PatientReturnRequest returnReq = PatientReturnRequest.builder()
                .dispensingId(dispensingId.longValue())
                .patientId(1L)
                .medicineId(1L)
                .batchId(1L)
                .quantityReturned(2)
                .isRestocked(true)
                .reason("Unused medicine returned")
                .build();

        mockMvc.perform(post("/api/pharmacy/returns/patient")
                        .header("Authorization", "Bearer " + pharmacistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(returnReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.returnId").value(org.hamcrest.Matchers.startsWith("RET-")));
    }

    @Test
    @DisplayName("33. Supplier Return Processing & Stock Reduction")
    void testSupplierReturnWorkflow() throws Exception {
        String pharmacistToken = getPharmacistToken();

        SupplierReturnRequest returnReq = SupplierReturnRequest.builder()
                .supplierId(1L)
                .medicineId(1L)
                .batchId(1L)
                .quantityReturned(5)
                .reason("Damaged stock return to vendor")
                .build();

        mockMvc.perform(post("/api/pharmacy/returns/supplier")
                        .header("Authorization", "Bearer " + pharmacistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(returnReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.returnType").value("SUPPLIER_RETURN"));
    }

    @Test
    @DisplayName("34. Pharmacy Dashboard Metrics Retrieval")
    void testPharmacyDashboardMetrics() throws Exception {
        String pharmacistToken = getPharmacistToken();

        mockMvc.perform(get("/api/pharmacy/dashboard")
                        .header("Authorization", "Bearer " + pharmacistToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalMedicines").exists());
    }

    @Test
    @DisplayName("35. RBAC Enforcement - Doctor Cannot Perform Stock Adjustment")
    void testRbacDoctorCannotAdjustStock() throws Exception {
        String doctorToken = getDoctorToken();

        StockAdjustmentDto adjustment = StockAdjustmentDto.builder()
                .medicineId(1L)
                .batchId(1L)
                .adjustmentQuantity(5)
                .direction("IN")
                .reason("Unauthorized adjustment attempt")
                .build();

        mockMvc.perform(post("/api/pharmacy/inventory/adjust")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adjustment)))
                .andExpect(status().isForbidden());
    }

    // --- PHASE 6 INTEGRATION TESTS ---

    @Test
    @DisplayName("36. Role BILLING_OFFICER Seeding & Authentication")
    void testBillingOfficerRoleAndAuth() throws Exception {
        String token = getBillingOfficerToken();
        assertNotNull(token);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("BILLING_OFFICER"));
    }

    @Test
    @DisplayName("37. Charge Master Catalog Management")
    void testChargeMasterManagement() throws Exception {
        String token = getBillingOfficerToken();

        ChargeMasterDto charge = ChargeMasterDto.builder()
                .chargeName("Echocardiogram (Echo)")
                .chargeCategory(com.hospital.entity.ChargeCategory.RADIOLOGY)
                .description("Transthoracic 2D Echo with Doppler")
                .unit("per test")
                .baseRate(new BigDecimal("2500.00"))
                .taxPercentage(new BigDecimal("5.00"))
                .build();

        mockMvc.perform(post("/api/billing/charges")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(charge)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.chargeCode").value(org.hamcrest.Matchers.startsWith("CHG-")));
    }

    @Test
    @DisplayName("38. Create Draft Bill & Add Bill Line Items")
    void testDraftBillCreationAndItemAddition() throws Exception {
        String token = getBillingOfficerToken();

        BillDto draftReq = BillDto.builder()
                .patientId(1L)
                .billType(com.hospital.entity.BillType.OPD)
                .notes("Custom draft OPD bill")
                .build();

        String resp = mockMvc.perform(post("/api/billing/bills")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(draftReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();

        Integer billId = com.jayway.jsonpath.JsonPath.read(resp, "$.data.id");

        BillItemDto item = BillItemDto.builder()
                .chargeCode("CHG-SPEC-CONSULT")
                .description("Specialist Consultation")
                .sourceType(com.hospital.entity.BillSourceType.CONSULTATION)
                .quantity(1)
                .unitRate(new BigDecimal("1000.00"))
                .discountAmount(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/api/billing/bills/" + billId + "/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.subtotal").value(1000.00))
                .andExpect(jsonPath("$.data.grandTotal").value(900.00));
    }

    @Test
    @DisplayName("39. OPD Source Billing Generation & Duplicate Protection")
    void testOpdSourceBillingAndDuplicateProtection() throws Exception {
        String token = getBillingOfficerToken();

        // Generate OPD bill for Visit 2 (Visit 1 was already billed in demo data)
        mockMvc.perform(post("/api/billing/bills/opd/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.billType").value("OPD"));

        // Second attempt must fail with 409 Conflict
        mockMvc.perform(post("/api/billing/bills/opd/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("40. Full & Partial Payment Recording & Outstanding Calculation")
    void testPaymentRecordingAndOutstanding() throws Exception {
        String token = getBillingOfficerToken();

        // Create draft and finalize bill
        BillDto draft = BillDto.builder()
                .patientId(1L)
                .billType(com.hospital.entity.BillType.LABORATORY)
                .items(java.util.List.of(
                        BillItemDto.builder()
                                .description("Lipid Profile Test")
                                .sourceType(com.hospital.entity.BillSourceType.LAB)
                                .quantity(1)
                                .unitRate(new BigDecimal("2000.00"))
                                .build()
                ))
                .build();

        String resp = mockMvc.perform(post("/api/billing/bills")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(draft)))
                .andReturn().getResponse().getContentAsString();

        Integer billId = com.jayway.jsonpath.JsonPath.read(resp, "$.data.id");

        mockMvc.perform(post("/api/billing/bills/" + billId + "/finalize")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("GENERATED"));

        // Partial payment 1: 800
        PaymentRequest pay1 = PaymentRequest.builder()
                .billId(billId.longValue())
                .amount(new BigDecimal("800.00"))
                .paymentMethod(com.hospital.entity.PaymentMethod.CASH)
                .remarks("First installment")
                .build();

        mockMvc.perform(post("/api/billing/payments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pay1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // Verify PARTIALLY_PAID status and outstanding 1200
        mockMvc.perform(get("/api/billing/bills/" + billId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PARTIALLY_PAID"))
                .andExpect(jsonPath("$.data.outstandingAmount").value(1200.00));
    }

    @Test
    @DisplayName("41. Payment Exceeding Outstanding Amount Rejected")
    void testPaymentExceedingOutstandingRejected() throws Exception {
        String token = getBillingOfficerToken();

        BillDto draft = BillDto.builder()
                .patientId(1L)
                .billType(com.hospital.entity.BillType.OPD)
                .items(java.util.List.of(
                        BillItemDto.builder()
                                .description("Test OPD Charge")
                                .sourceType(com.hospital.entity.BillSourceType.CONSULTATION)
                                .quantity(1)
                                .unitRate(new BigDecimal("500.00"))
                                .build()
                ))
                .build();

        String resp = mockMvc.perform(post("/api/billing/bills")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(draft)))
                .andReturn().getResponse().getContentAsString();

        Integer billId = com.jayway.jsonpath.JsonPath.read(resp, "$.data.id");

        mockMvc.perform(post("/api/billing/bills/" + billId + "/finalize")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        PaymentRequest excessPay = PaymentRequest.builder()
                .billId(billId.longValue())
                .amount(new BigDecimal("999999.00"))
                .paymentMethod(com.hospital.entity.PaymentMethod.CASH)
                .build();

        mockMvc.perform(post("/api/billing/payments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(excessPay)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("42. Patient Financial Refund & Restocking Credit")
    void testRefundProcessing() throws Exception {
        String token = getBillingOfficerToken();

        BillDto draft = BillDto.builder()
                .patientId(1L)
                .billType(com.hospital.entity.BillType.OPD)
                .items(java.util.List.of(
                        BillItemDto.builder()
                                .description("Test Refundable Charge")
                                .sourceType(com.hospital.entity.BillSourceType.CONSULTATION)
                                .quantity(1)
                                .unitRate(new BigDecimal("1000.00"))
                                .build()
                ))
                .build();

        String resp = mockMvc.perform(post("/api/billing/bills")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(draft)))
                .andReturn().getResponse().getContentAsString();

        Integer billId = com.jayway.jsonpath.JsonPath.read(resp, "$.data.id");

        mockMvc.perform(post("/api/billing/bills/" + billId + "/finalize")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Pay 1000 first
        PaymentRequest payReq = PaymentRequest.builder()
                .billId(billId.longValue())
                .amount(new BigDecimal("1000.00"))
                .paymentMethod(com.hospital.entity.PaymentMethod.CASH)
                .build();

        mockMvc.perform(post("/api/billing/payments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payReq)))
                .andExpect(status().isCreated());

        RefundRequest refundReq = RefundRequest.builder()
                .billId(billId.longValue())
                .refundAmount(new BigDecimal("100.00"))
                .refundMethod(com.hospital.entity.PaymentMethod.CASH)
                .reason("Discount error adjustment")
                .build();

        mockMvc.perform(post("/api/billing/refunds")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refundReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.refundNumber").value(org.hamcrest.Matchers.startsWith("REF-")));
    }

    @Test
    @DisplayName("43. Authorized Credit Note Issuance")
    void testCreditNoteIssuance() throws Exception {
        String token = getBillingOfficerToken();

        BillDto draft = BillDto.builder()
                .patientId(1L)
                .billType(com.hospital.entity.BillType.OPD)
                .items(java.util.List.of(
                        BillItemDto.builder()
                                .description("Test Credit Note Charge")
                                .sourceType(com.hospital.entity.BillSourceType.CONSULTATION)
                                .quantity(1)
                                .unitRate(new BigDecimal("500.00"))
                                .build()
                ))
                .build();

        String resp = mockMvc.perform(post("/api/billing/bills")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(draft)))
                .andReturn().getResponse().getContentAsString();

        Integer billId = com.jayway.jsonpath.JsonPath.read(resp, "$.data.id");

        mockMvc.perform(post("/api/billing/bills/" + billId + "/finalize")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        CreditNoteRequest cnReq = CreditNoteRequest.builder()
                .billId(billId.longValue())
                .amount(new BigDecimal("50.00"))
                .reason("Billing rounding correction")
                .build();

        mockMvc.perform(post("/api/billing/credit-notes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cnReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.creditNoteNumber").value(org.hamcrest.Matchers.startsWith("CN-")));
    }

    @Test
    @DisplayName("44. Patient Financial Ledger Audit Log Retrieval")
    void testPatientLedgerRetrieval() throws Exception {
        String token = getBillingOfficerToken();

        mockMvc.perform(get("/api/billing/patients/1/ledger")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("45. IPD Final Billing & Discharge Clearance Calculation")
    void testIpdFinalBillingAndDischargeClearance() throws Exception {
        String token = getBillingOfficerToken();

        mockMvc.perform(post("/api/billing/bills/ipd/1/clearance")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.clearanceStatus").exists());
    }

    @Test
    @DisplayName("46. Billing Dashboard Real Metric Calculations")
    void testBillingDashboardMetrics() throws Exception {
        String token = getBillingOfficerToken();

        mockMvc.perform(get("/api/billing/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.todayBillsCount").exists())
                .andExpect(jsonPath("$.data.todayBilledAmount").exists());
    }

    @Test
    @DisplayName("47. RBAC Enforcement - Doctor Cannot Process Refund")
    void testRbacDoctorCannotProcessRefund() throws Exception {
        String doctorToken = getDoctorToken();

        RefundRequest refundReq = RefundRequest.builder()
                .billId(1L)
                .refundAmount(new BigDecimal("50.00"))
                .refundMethod(com.hospital.entity.PaymentMethod.CASH)
                .reason("Unauthorized doctor refund")
                .build();

        mockMvc.perform(post("/api/billing/refunds")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refundReq)))
                .andExpect(status().isForbidden());
    }
}


