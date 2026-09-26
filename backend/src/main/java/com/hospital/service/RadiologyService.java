package com.hospital.service;

import com.hospital.dto.RadiologyDashboardDto;
import com.hospital.dto.RadiologyOrderRequest;
import com.hospital.dto.RadiologyOrderResponse;
import com.hospital.dto.RadiologyReportRequest;
import com.hospital.dto.RadiologyReportResponse;
import com.hospital.dto.RadiologyTestRequest;
import com.hospital.dto.RadiologyTestResponse;
import com.hospital.entity.Doctor;
import com.hospital.entity.EhrRecord;
import com.hospital.entity.EhrRecordType;
import com.hospital.entity.OpdVisit;
import com.hospital.entity.Patient;
import com.hospital.entity.RadiologyOrder;
import com.hospital.entity.RadiologyOrderStatus;
import com.hospital.entity.RadiologyReport;
import com.hospital.entity.RadiologyReportStatus;
import com.hospital.entity.RadiologyTest;
import com.hospital.entity.User;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.EhrRecordRepository;
import com.hospital.repository.OpdVisitRepository;
import com.hospital.repository.PatientRepository;
import com.hospital.repository.RadiologyOrderRepository;
import com.hospital.repository.RadiologyReportRepository;
import com.hospital.repository.RadiologyTestRepository;
import com.hospital.repository.UserRepository;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RadiologyService {

    private final RadiologyTestRepository radiologyTestRepository;
    private final RadiologyOrderRepository radiologyOrderRepository;
    private final RadiologyReportRepository radiologyReportRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final OpdVisitRepository opdVisitRepository;
    private final EhrRecordRepository ehrRecordRepository;
    private final AuditLogService auditLogService;

    // --- Radiology Test Catalog ---

    @Transactional(readOnly = true)
    public List<RadiologyTestResponse> getAllRadiologyTests(Boolean activeOnly) {
        List<RadiologyTest> tests = activeOnly != null && activeOnly ?
                radiologyTestRepository.findByIsActiveTrue() : radiologyTestRepository.findAll();
        return tests.stream().map(this::mapTestToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RadiologyTestResponse getRadiologyTestById(Long id) {
        RadiologyTest test = radiologyTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Radiology test not found with ID: " + id));
        return mapTestToResponse(test);
    }

    @Transactional
    public RadiologyTestResponse createRadiologyTest(RadiologyTestRequest request) {
        if (radiologyTestRepository.existsByTestCode(request.getTestCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Radiology test code already exists: " + request.getTestCode());
        }
        RadiologyTest test = RadiologyTest.builder()
                .testCode(request.getTestCode())
                .testName(request.getTestName())
                .modality(request.getModality())
                .bodyPart(request.getBodyPart())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        test = radiologyTestRepository.save(test);
        auditLogService.logAction("CREATE_RADIOLOGY_TEST", "RadiologyTest", test.getId().toString(), "Created radiology test: " + test.getTestName());
        return mapTestToResponse(test);
    }

    @Transactional
    public RadiologyTestResponse updateRadiologyTest(Long id, RadiologyTestRequest request) {
        RadiologyTest test = radiologyTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Radiology test not found with ID: " + id));

        if (request.getTestName() != null) test.setTestName(request.getTestName());
        if (request.getModality() != null) test.setModality(request.getModality());
        if (request.getBodyPart() != null) test.setBodyPart(request.getBodyPart());
        if (request.getDescription() != null) test.setDescription(request.getDescription());
        if (request.getIsActive() != null) test.setIsActive(request.getIsActive());

        test = radiologyTestRepository.save(test);
        auditLogService.logAction("UPDATE_RADIOLOGY_TEST", "RadiologyTest", test.getId().toString(), "Updated radiology test: " + test.getTestName());
        return mapTestToResponse(test);
    }

    // --- Radiology Orders ---

    @Transactional(readOnly = true)
    public List<RadiologyOrderResponse> getAllOrders(RadiologyOrderStatus status) {
        List<RadiologyOrder> orders = status != null ?
                radiologyOrderRepository.findByStatus(status) : radiologyOrderRepository.findAll();
        return orders.stream().map(this::mapOrderToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RadiologyOrderResponse> getPatientOrders(Long patientId) {
        return radiologyOrderRepository.findByPatientIdOrderByOrderDateDesc(patientId).stream()
                .map(this::mapOrderToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RadiologyOrderResponse getOrderById(Long id) {
        RadiologyOrder order = radiologyOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Radiology order not found with ID: " + id));
        return mapOrderToResponse(order);
    }

    @Transactional
    public RadiologyOrderResponse createOrder(RadiologyOrderRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + request.getPatientId()));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + request.getDoctorId()));

        RadiologyTest test = radiologyTestRepository.findById(request.getRadiologyTestId())
                .orElseThrow(() -> new ResourceNotFoundException("Radiology test not found with ID: " + request.getRadiologyTestId()));

        OpdVisit visit = null;
        if (request.getOpdVisitId() != null) {
            visit = opdVisitRepository.findById(request.getOpdVisitId()).orElse(null);
        }

        long count = radiologyOrderRepository.count() + 1;
        String code = String.format("RAD-%d-%06d", LocalDate.now().getYear(), count);

        RadiologyOrder order = RadiologyOrder.builder()
                .radiologyOrderId(code)
                .patient(patient)
                .doctor(doctor)
                .department(doctor.getDepartment())
                .radiologyTest(test)
                .opdVisit(visit)
                .orderDate(LocalDate.now())
                .priority(request.getPriority())
                .clinicalIndication(request.getClinicalIndication())
                .status(RadiologyOrderStatus.ORDERED)
                .build();

        order = radiologyOrderRepository.save(order);
        auditLogService.logAction("CREATE_RADIOLOGY_ORDER", "RadiologyOrder", order.getId().toString(), "Created radiology order " + order.getRadiologyOrderId() + " for patient " + patient.getPatientId());
        return mapOrderToResponse(order);
    }

    @Transactional
    public RadiologyOrderResponse updateOrderStatus(Long orderId, RadiologyOrderStatus newStatus) {
        RadiologyOrder order = radiologyOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Radiology order not found with ID: " + orderId));

        order.setStatus(newStatus);
        if (newStatus == RadiologyOrderStatus.PERFORMED) {
            order.setPerformedAt(LocalDateTime.now());
        }
        order = radiologyOrderRepository.save(order);
        auditLogService.logAction("UPDATE_RADIOLOGY_ORDER_STATUS", "RadiologyOrder", order.getId().toString(), "Updated status to " + newStatus + " for order " + order.getRadiologyOrderId());
        return mapOrderToResponse(order);
    }

    // --- Radiology Reports ---

    @Transactional(readOnly = true)
    public RadiologyReportResponse getReportByOrderId(Long orderId) {
        RadiologyReport report = radiologyReportRepository.findByRadiologyOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found for radiology order ID: " + orderId));
        return mapReportToResponse(report);
    }

    @Transactional(readOnly = true)
    public List<RadiologyReportResponse> getPatientReports(Long patientId) {
        return radiologyReportRepository.findByPatientIdOrderByReportedAtDesc(patientId).stream()
                .map(this::mapReportToResponse).collect(Collectors.toList());
    }

    @Transactional
    public RadiologyReportResponse createOrUpdateReport(Long orderId, RadiologyReportRequest request) {
        RadiologyOrder order = radiologyOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Radiology order not found with ID: " + orderId));

        User radiologist = getCurrentUser();

        RadiologyReport report = radiologyReportRepository.findByRadiologyOrderId(orderId).orElse(null);

        if (report != null && report.getStatus() == RadiologyReportStatus.FINAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update a finalized radiology report");
        }

        if (report == null) {
            long count = radiologyReportRepository.count() + 1;
            String code = String.format("REP-%d-%06d", LocalDate.now().getYear(), count);
            report = RadiologyReport.builder()
                    .reportId(code)
                    .radiologyOrder(order)
                    .patient(order.getPatient())
                    .radiologist(radiologist)
                    .reportedAt(LocalDateTime.now())
                    .status(RadiologyReportStatus.DRAFT)
                    .build();
        }

        report.setFindings(request.getFindings());
        report.setImpression(request.getImpression());
        if (request.getStatus() != null) {
            report.setStatus(request.getStatus());
        }

        boolean isFinal = request.getStatus() == RadiologyReportStatus.FINAL || Boolean.TRUE.equals(request.getIsFinal());
        if (isFinal) {
            report.setStatus(RadiologyReportStatus.FINAL);
            report.setVerifiedAt(LocalDateTime.now());
            order.setStatus(RadiologyOrderStatus.COMPLETED);
            radiologyOrderRepository.save(order);

            // Log to EHR
            long ehrCount = ehrRecordRepository.count() + 1;
            EhrRecord ehr = EhrRecord.builder()
                    .ehrRecordId(String.format("EHR-%d-%06d", LocalDate.now().getYear(), ehrCount))
                    .patient(order.getPatient())
                    .doctor(order.getDoctor())
                    .department(order.getDepartment())
                    .opdVisit(order.getOpdVisit())
                    .recordType(EhrRecordType.RADIOLOGY_REPORT)
                    .clinicalSummary("Radiology Report - " + order.getRadiologyTest().getTestName() + "\nModality: " + order.getRadiologyTest().getModality() + "\nFindings: " + report.getFindings() + "\nImpression: " + report.getImpression())
                    .build();
            ehrRecordRepository.save(ehr);
        }

        report = radiologyReportRepository.save(report);
        auditLogService.logAction("SAVE_RADIOLOGY_REPORT", "RadiologyReport", report.getId().toString(), "Saved radiology report for order " + order.getRadiologyOrderId() + " (Final: " + (report.getStatus() == RadiologyReportStatus.FINAL) + ")");
        return mapReportToResponse(report);
    }

    @Transactional(readOnly = true)
    public RadiologyDashboardDto getDashboardStats() {
        LocalDate today = LocalDate.now();
        return RadiologyDashboardDto.builder()
                .todaysOrders(radiologyOrderRepository.countByOrderDate(today))
                .scheduled(radiologyOrderRepository.countByStatus(RadiologyOrderStatus.SCHEDULED))
                .pendingPerformance(radiologyOrderRepository.countByStatus(RadiologyOrderStatus.ORDERED))
                .reportsDraft(radiologyReportRepository.countByStatus(RadiologyReportStatus.DRAFT))
                .reportsPendingVerification(radiologyReportRepository.countByStatus(RadiologyReportStatus.DRAFT))
                .verifiedReports(radiologyReportRepository.countByStatus(RadiologyReportStatus.FINAL))
                .build();
    }

    // Helper mappers
    private RadiologyTestResponse mapTestToResponse(RadiologyTest test) {
        return RadiologyTestResponse.builder()
                .id(test.getId())
                .testCode(test.getTestCode())
                .testName(test.getTestName())
                .modality(test.getModality())
                .bodyPart(test.getBodyPart())
                .description(test.getDescription())
                .isActive(test.getIsActive())
                .createdAt(test.getCreatedAt())
                .updatedAt(test.getUpdatedAt())
                .build();
    }

    private RadiologyOrderResponse mapOrderToResponse(RadiologyOrder order) {
        return RadiologyOrderResponse.builder()
                .id(order.getId())
                .radiologyOrderId(order.getRadiologyOrderId())
                .patientId(order.getPatient().getId())
                .patientCode(order.getPatient().getPatientId())
                .patientName(order.getPatient().getFirstName() + " " + order.getPatient().getLastName())
                .doctorId(order.getDoctor() != null ? order.getDoctor().getId() : null)
                .doctorName(order.getDoctor() != null ? "Dr. " + order.getDoctor().getFirstName() + " " + order.getDoctor().getLastName() : null)
                .opdVisitId(order.getOpdVisit() != null ? order.getOpdVisit().getId() : null)
                .opdVisitCode(order.getOpdVisit() != null ? order.getOpdVisit().getOpdVisitId() : null)
                .departmentId(order.getDepartment() != null ? order.getDepartment().getId() : null)
                .departmentName(order.getDepartment() != null ? order.getDepartment().getDepartmentName() : null)
                .radiologyTestId(order.getRadiologyTest().getId())
                .testCode(order.getRadiologyTest().getTestCode())
                .testName(order.getRadiologyTest().getTestName())
                .modality(order.getRadiologyTest().getModality())
                .orderDate(order.getOrderDate())
                .priority(order.getPriority())
                .status(order.getStatus())
                .clinicalIndication(order.getClinicalIndication())
                .report(order.getReport() != null ? mapReportToResponse(order.getReport()) : null)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private RadiologyReportResponse mapReportToResponse(RadiologyReport report) {
        return RadiologyReportResponse.builder()
                .id(report.getId())
                .reportId(report.getReportId())
                .radiologyOrderId(report.getRadiologyOrder().getId())
                .radiologyOrderCode(report.getRadiologyOrder().getRadiologyOrderId())
                .patientId(report.getPatient().getId())
                .patientName(report.getPatient().getFirstName() + " " + report.getPatient().getLastName())
                .radiologistId(report.getRadiologist() != null ? report.getRadiologist().getId() : null)
                .radiologistName(report.getRadiologist() != null ? report.getRadiologist().getFullName() : null)
                .findings(report.getFindings())
                .impression(report.getImpression())
                .status(report.getStatus())
                .reportedAt(report.getReportedAt())
                .verifiedAt(report.getVerifiedAt())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }

    private User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return userRepository.findById(principal.getId()).orElse(null);
        }
        return null;
    }
}
