package com.hospital.service;

import com.hospital.dto.LabDashboardDto;
import com.hospital.dto.LabOrderItemRequest;
import com.hospital.dto.LabOrderItemResponse;
import com.hospital.dto.LabOrderRequest;
import com.hospital.dto.LabOrderResponse;
import com.hospital.dto.LabTestRequest;
import com.hospital.dto.LabTestResponse;
import com.hospital.entity.Doctor;
import com.hospital.entity.EhrRecord;
import com.hospital.entity.EhrRecordType;
import com.hospital.entity.LabItemStatus;
import com.hospital.entity.LabOrder;
import com.hospital.entity.LabOrderItem;
import com.hospital.entity.LabOrderStatus;
import com.hospital.entity.LabTest;
import com.hospital.entity.OpdVisit;
import com.hospital.entity.Patient;
import com.hospital.entity.User;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.EhrRecordRepository;
import com.hospital.repository.LabOrderRepository;
import com.hospital.repository.LabTestRepository;
import com.hospital.repository.OpdVisitRepository;
import com.hospital.repository.PatientRepository;
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
public class LaboratoryService {

    private final LabTestRepository labTestRepository;
    private final LabOrderRepository labOrderRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final OpdVisitRepository opdVisitRepository;
    private final EhrRecordRepository ehrRecordRepository;
    private final AuditLogService auditLogService;

    // --- Lab Test Catalog ---

    @Transactional(readOnly = true)
    public List<LabTestResponse> getAllLabTests(Boolean activeOnly) {
        List<LabTest> tests = activeOnly != null && activeOnly ?
                labTestRepository.findByIsActiveTrue() : labTestRepository.findAll();
        return tests.stream().map(this::mapTestToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LabTestResponse getLabTestById(Long id) {
        LabTest test = labTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test not found with ID: " + id));
        return mapTestToResponse(test);
    }

    @Transactional
    public LabTestResponse createLabTest(LabTestRequest request) {
        if (labTestRepository.existsByTestCode(request.getTestCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lab test code already exists: " + request.getTestCode());
        }
        LabTest test = LabTest.builder()
                .testCode(request.getTestCode())
                .testName(request.getTestName())
                .category(request.getCategory())
                .sampleType(request.getSampleType())
                .description(request.getDescription())
                .normalRangeDescription(request.getNormalRangeDescription())
                .unit(request.getUnit())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        test = labTestRepository.save(test);
        auditLogService.logAction("CREATE_LAB_TEST", "LabTest", test.getId().toString(), "Created lab test: " + test.getTestName());
        return mapTestToResponse(test);
    }

    @Transactional
    public LabTestResponse updateLabTest(Long id, LabTestRequest request) {
        LabTest test = labTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test not found with ID: " + id));

        if (request.getTestName() != null) test.setTestName(request.getTestName());
        if (request.getCategory() != null) test.setCategory(request.getCategory());
        if (request.getSampleType() != null) test.setSampleType(request.getSampleType());
        if (request.getDescription() != null) test.setDescription(request.getDescription());
        if (request.getNormalRangeDescription() != null) test.setNormalRangeDescription(request.getNormalRangeDescription());
        if (request.getUnit() != null) test.setUnit(request.getUnit());
        if (request.getIsActive() != null) test.setIsActive(request.getIsActive());

        test = labTestRepository.save(test);
        auditLogService.logAction("UPDATE_LAB_TEST", "LabTest", test.getId().toString(), "Updated lab test: " + test.getTestName());
        return mapTestToResponse(test);
    }

    // --- Lab Orders ---

    @Transactional(readOnly = true)
    public List<LabOrderResponse> getAllOrders(LabOrderStatus status) {
        List<LabOrder> orders = status != null ?
                labOrderRepository.findByStatus(status) : labOrderRepository.findAll();
        return orders.stream().map(this::mapOrderToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LabOrderResponse> getPatientOrders(Long patientId) {
        return labOrderRepository.findByPatientIdOrderByOrderDateDesc(patientId).stream()
                .map(this::mapOrderToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LabOrderResponse getOrderById(Long id) {
        LabOrder order = labOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab order not found with ID: " + id));
        return mapOrderToResponse(order);
    }

    @Transactional
    public LabOrderResponse createOrder(LabOrderRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + request.getPatientId()));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + request.getDoctorId()));

        OpdVisit visit = null;
        if (request.getOpdVisitId() != null) {
            visit = opdVisitRepository.findById(request.getOpdVisitId()).orElse(null);
        }

        long count = labOrderRepository.count() + 1;
        String code = String.format("LAB-%d-%06d", LocalDate.now().getYear(), count);

        LabOrder order = LabOrder.builder()
                .labOrderId(code)
                .patient(patient)
                .doctor(doctor)
                .department(doctor.getDepartment())
                .opdVisit(visit)
                .orderDate(LocalDate.now())
                .priority(request.getPriority())
                .status(LabOrderStatus.ORDERED)
                .clinicalNote(request.getClinicalNote())
                .build();

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (LabOrderItemRequest itemReq : request.getItems()) {
                LabTest test = labTestRepository.findById(itemReq.getLabTestId())
                        .orElseThrow(() -> new ResourceNotFoundException("Lab test not found with ID: " + itemReq.getLabTestId()));

                LabOrderItem item = LabOrderItem.builder()
                        .labOrder(order)
                        .labTest(test)
                        .sampleType(itemReq.getSampleType() != null ? itemReq.getSampleType() : test.getSampleType())
                        .status(LabItemStatus.ORDERED)
                        .build();
                order.getItems().add(item);
            }
        }

        order = labOrderRepository.save(order);
        auditLogService.logAction("CREATE_LAB_ORDER", "LabOrder", order.getId().toString(), "Created lab order " + order.getLabOrderId() + " for patient " + patient.getPatientId());
        return mapOrderToResponse(order);
    }

    @Transactional
    public LabOrderResponse collectSample(Long orderId) {
        LabOrder order = labOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab order not found with ID: " + orderId));

        User currentUser = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();

        order.setStatus(LabOrderStatus.SAMPLE_COLLECTED);
        order.setSampleCollectedAt(now);
        order.setSampleCollectedBy(currentUser);

        for (LabOrderItem item : order.getItems()) {
            if (item.getStatus() == LabItemStatus.ORDERED) {
                item.setStatus(LabItemStatus.SAMPLE_COLLECTED);
                item.setSampleCollectedAt(now);
                item.setSampleCollectedBy(currentUser);
            }
        }

        order = labOrderRepository.save(order);
        auditLogService.logAction("SAMPLE_COLLECTED", "LabOrder", order.getId().toString(), "Sample collected for lab order " + order.getLabOrderId());
        return mapOrderToResponse(order);
    }

    @Transactional
    public LabOrderResponse enterResult(Long orderId, Long itemId, LabOrderItemRequest request) {
        LabOrder order = labOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab order not found with ID: " + orderId));

        LabOrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Lab order item not found with ID: " + itemId));

        item.setResultValue(request.getResultValue());
        item.setResultUnit(request.getResultUnit());
        item.setReferenceRange(request.getReferenceRange());
        item.setAbnormal(request.getIsAbnormal() != null ? request.getIsAbnormal() : false);
        item.setRemarks(request.getRemarks());
        item.setStatus(LabItemStatus.RESULT_ENTERED);

        boolean allEntered = order.getItems().stream()
                .allMatch(i -> i.getStatus() == LabItemStatus.RESULT_ENTERED || i.getStatus() == LabItemStatus.VERIFIED || i.getStatus() == LabItemStatus.COMPLETED);
        if (allEntered) {
            order.setStatus(LabOrderStatus.RESULT_READY);
        }

        order = labOrderRepository.save(order);
        auditLogService.logAction("ENTER_LAB_RESULT", "LabOrderItem", item.getId().toString(), "Entered result for lab order " + order.getLabOrderId());
        return mapOrderToResponse(order);
    }

    @Transactional
    public LabOrderResponse verifyOrder(Long orderId) {
        LabOrder order = labOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab order not found with ID: " + orderId));

        User currentUser = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();

        order.setStatus(LabOrderStatus.COMPLETED);
        for (LabOrderItem item : order.getItems()) {
            item.setStatus(LabItemStatus.COMPLETED);
            item.setVerifiedBy(currentUser);
            item.setVerifiedAt(now);
        }

        order = labOrderRepository.save(order);

        // Log to EHR
        StringBuilder summary = new StringBuilder();
        summary.append("Lab Order ").append(order.getLabOrderId()).append(" Verified:\n");
        for (LabOrderItem item : order.getItems()) {
            summary.append("- ").append(item.getLabTest().getTestName()).append(": ")
                    .append(item.getResultValue()).append(" ").append(item.getResultUnit() != null ? item.getResultUnit() : "")
                    .append(item.getAbnormal() != null && item.getAbnormal() ? " [ABNORMAL]" : "")
                    .append("\n");
        }

        long ehrCount = ehrRecordRepository.count() + 1;
        EhrRecord ehr = EhrRecord.builder()
                .ehrRecordId(String.format("EHR-%d-%06d", LocalDate.now().getYear(), ehrCount))
                .patient(order.getPatient())
                .doctor(order.getDoctor())
                .department(order.getDepartment())
                .opdVisit(order.getOpdVisit())
                .recordType(EhrRecordType.LAB_RESULT)
                .clinicalSummary("Lab Results - Order #" + order.getLabOrderId() + "\n" + summary.toString())
                .build();
        ehrRecordRepository.save(ehr);

        auditLogService.logAction("VERIFY_LAB_ORDER", "LabOrder", order.getId().toString(), "Verified lab order " + order.getLabOrderId());
        return mapOrderToResponse(order);
    }

    @Transactional
    public LabOrderResponse cancelOrder(Long orderId, String reason) {
        LabOrder order = labOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab order not found with ID: " + orderId));

        if (order.getStatus() == LabOrderStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel completed lab order");
        }

        order.setStatus(LabOrderStatus.CANCELLED);
        for (LabOrderItem item : order.getItems()) {
            item.setStatus(LabItemStatus.CANCELLED);
        }

        order = labOrderRepository.save(order);
        auditLogService.logAction("CANCEL_LAB_ORDER", "LabOrder", order.getId().toString(), "Cancelled lab order " + order.getLabOrderId() + ". Reason: " + reason);
        return mapOrderToResponse(order);
    }

    @Transactional(readOnly = true)
    public LabDashboardDto getDashboardStats() {
        LocalDate today = LocalDate.now();
        List<LabOrder> recentPending = labOrderRepository.findByStatus(LabOrderStatus.ORDERED).stream().limit(10).collect(Collectors.toList());

        return LabDashboardDto.builder()
                .totalOrders(labOrderRepository.countByOrderDate(today))
                .pendingOrders(labOrderRepository.countByStatus(LabOrderStatus.ORDERED))
                .sampleCollected(labOrderRepository.countByStatus(LabOrderStatus.SAMPLE_COLLECTED))
                .inProgress(labOrderRepository.countByStatus(LabOrderStatus.PROCESSING))
                .completedOrders(labOrderRepository.countByStatus(LabOrderStatus.COMPLETED))
                .recentPendingOrders(recentPending.stream().map(this::mapOrderToResponse).collect(Collectors.toList()))
                .build();
    }

    // Helper mappers
    private LabTestResponse mapTestToResponse(LabTest test) {
        return LabTestResponse.builder()
                .id(test.getId())
                .testCode(test.getTestCode())
                .testName(test.getTestName())
                .category(test.getCategory())
                .sampleType(test.getSampleType())
                .description(test.getDescription())
                .normalRangeDescription(test.getNormalRangeDescription())
                .unit(test.getUnit())
                .isActive(test.getIsActive())
                .createdAt(test.getCreatedAt())
                .updatedAt(test.getUpdatedAt())
                .build();
    }

    private LabOrderResponse mapOrderToResponse(LabOrder order) {
        List<LabOrderItemResponse> itemResponses = order.getItems() != null ? order.getItems().stream().map(item ->
                LabOrderItemResponse.builder()
                        .id(item.getId())
                        .labTestId(item.getLabTest().getId())
                        .testCode(item.getLabTest().getTestCode())
                        .testName(item.getLabTest().getTestName())
                        .sampleType(item.getSampleType())
                        .status(item.getStatus())
                        .resultValue(item.getResultValue())
                        .resultUnit(item.getResultUnit())
                        .referenceRange(item.getReferenceRange())
                        .resultComment(item.getRemarks())
                        .verifiedByName(item.getVerifiedBy() != null ? item.getVerifiedBy().getFullName() : null)
                        .verifiedAt(item.getVerifiedAt())
                        .createdAt(item.getCreatedAt())
                        .updatedAt(item.getUpdatedAt())
                        .build()
        ).collect(Collectors.toList()) : List.of();

        return LabOrderResponse.builder()
                .id(order.getId())
                .labOrderId(order.getLabOrderId())
                .patientId(order.getPatient().getId())
                .patientCode(order.getPatient().getPatientId())
                .patientName(order.getPatient().getFirstName() + " " + order.getPatient().getLastName())
                .doctorId(order.getDoctor() != null ? order.getDoctor().getId() : null)
                .doctorName(order.getDoctor() != null ? "Dr. " + order.getDoctor().getFirstName() + " " + order.getDoctor().getLastName() : null)
                .opdVisitId(order.getOpdVisit() != null ? order.getOpdVisit().getId() : null)
                .opdVisitCode(order.getOpdVisit() != null ? order.getOpdVisit().getOpdVisitId() : null)
                .departmentId(order.getDepartment() != null ? order.getDepartment().getId() : null)
                .departmentName(order.getDepartment() != null ? order.getDepartment().getDepartmentName() : null)
                .orderDate(order.getOrderDate())
                .priority(order.getPriority())
                .status(order.getStatus())
                .clinicalNote(order.getClinicalNote())
                .sampleCollectedAt(order.getSampleCollectedAt())
                .sampleCollectedByName(order.getSampleCollectedBy() != null ? order.getSampleCollectedBy().getFullName() : null)
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
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
