package com.hospital.service;

import com.hospital.dto.*;
import com.hospital.entity.*;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IpdAdmissionService {

    private final IpdAdmissionRepository ipdAdmissionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final WardRepository wardRepository;
    private final BedRepository bedRepository;
    private final BedAllocationRepository bedAllocationRepository;
    private final WardTransferRepository wardTransferRepository;
    private final OpdVisitRepository opdVisitRepository;
    private final AuditLogService auditLogService;

    private static final List<AdmissionStatus> ACTIVE_STATUSES = Arrays.asList(
            AdmissionStatus.REQUESTED,
            AdmissionStatus.APPROVED,
            AdmissionStatus.ADMITTED,
            AdmissionStatus.ON_LEAVE,
            AdmissionStatus.DISCHARGE_PLANNED
    );

    @Transactional(readOnly = true)
    public Page<IpdAdmissionResponse> getAllAdmissions(
            Long patientId, Long doctorId, Long departmentId, Long wardId, Long bedId,
            AdmissionStatus status, AdmissionType admissionType, LocalDate date, Pageable pageable) {

        Specification<IpdAdmission> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (patientId != null) predicates = cb.and(predicates, cb.equal(root.get("patient").get("id"), patientId));
            if (doctorId != null) predicates = cb.and(predicates, cb.equal(root.get("admittingDoctor").get("id"), doctorId));
            if (departmentId != null) predicates = cb.and(predicates, cb.equal(root.get("department").get("id"), departmentId));
            if (wardId != null) predicates = cb.and(predicates, cb.equal(root.get("ward").get("id"), wardId));
            if (bedId != null) predicates = cb.and(predicates, cb.equal(root.get("bed").get("id"), bedId));
            if (status != null) predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            if (admissionType != null) predicates = cb.and(predicates, cb.equal(root.get("admissionType"), admissionType));
            if (date != null) predicates = cb.and(predicates, cb.equal(root.get("admissionDate"), date));
            return predicates;
        };

        return ipdAdmissionRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public IpdAdmissionResponse getAdmissionById(Long id) {
        IpdAdmission admission = ipdAdmissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IPD Admission not found with ID: " + id));
        return mapToResponse(admission);
    }

    @Transactional(readOnly = true)
    public IpdAdmissionResponse getAdmissionByCode(String admissionId) {
        IpdAdmission admission = ipdAdmissionRepository.findByAdmissionId(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("IPD Admission not found with code: " + admissionId));
        return mapToResponse(admission);
    }

    @Transactional
    public IpdAdmissionResponse createAdmissionRequest(IpdAdmissionCreateRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + request.getPatientId()));
        if (!Boolean.TRUE.equals(patient.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient account is inactive");
        }

        // Rule: One active admission per patient
        List<IpdAdmission> activeAdmissions = ipdAdmissionRepository.findActiveAdmissionsByPatientId(patient.getId(), ACTIVE_STATUSES);
        if (!activeAdmissions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient already has an active IPD admission: " + activeAdmissions.get(0).getAdmissionId());
        }

        Doctor doctor = doctorRepository.findById(request.getAdmittingDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + request.getAdmittingDoctorId()));
        if (!Boolean.TRUE.equals(doctor.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor account is inactive");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        OpdVisit opdVisit = null;
        if (request.getOpdVisitId() != null) {
            opdVisit = opdVisitRepository.findById(request.getOpdVisitId()).orElse(null);
        }

        Ward ward = null;
        if (request.getWardId() != null) {
            ward = wardRepository.findById(request.getWardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ward not found with ID: " + request.getWardId()));
        }

        Bed bed = null;
        if (request.getBedId() != null) {
            bed = bedRepository.findByIdForUpdate(request.getBedId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bed not found with ID: " + request.getBedId()));
            if (bed.getStatus() != BedStatus.AVAILABLE || !Boolean.TRUE.equals(bed.getIsActive())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Bed is not available for allocation");
            }
        }

        String admissionCode = generateAdmissionCode();
        String currentUsername = getCurrentUsername();

        IpdAdmission admission = IpdAdmission.builder()
                .admissionId(admissionCode)
                .patient(patient)
                .admittingDoctor(doctor)
                .department(department)
                .ward(ward)
                .bed(bed)
                .opdVisit(opdVisit)
                .admissionType(request.getAdmissionType())
                .admissionDate(request.getAdmissionDate() != null ? request.getAdmissionDate() : LocalDate.now())
                .admissionTime(request.getAdmissionTime() != null ? request.getAdmissionTime() : LocalTime.now())
                .reasonForAdmission(request.getReasonForAdmission())
                .clinicalSummary(request.getClinicalSummary())
                .status(bed != null ? AdmissionStatus.ADMITTED : AdmissionStatus.REQUESTED)
                .expectedDischargeDate(request.getExpectedDischargeDate())
                .createdBy(currentUsername)
                .build();

        IpdAdmission saved = ipdAdmissionRepository.save(admission);

        if (bed != null) {
            bed.setStatus(BedStatus.OCCUPIED);
            bedRepository.save(bed);

            BedAllocation allocation = BedAllocation.builder()
                    .admission(saved)
                    .patient(patient)
                    .ward(ward)
                    .bed(bed)
                    .allocationType(AllocationType.INITIAL)
                    .startDatetime(LocalDateTime.now())
                    .allocatedBy(currentUsername)
                    .reason("Initial Admission")
                    .build();
            bedAllocationRepository.save(allocation);
        }

        auditLogService.logAction("CREATE_ADMISSION", "IPD_ADMISSION", saved.getAdmissionId(), "Created IPD Admission Request for patient " + patient.getFirstName());
        return mapToResponse(saved);
    }

    @Transactional
    public IpdAdmissionResponse approveAdmission(Long id) {
        IpdAdmission admission = ipdAdmissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found with ID: " + id));

        if (admission.getStatus() != AdmissionStatus.REQUESTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only REQUESTED admissions can be approved. Current status: " + admission.getStatus());
        }

        admission.setStatus(AdmissionStatus.APPROVED);
        IpdAdmission updated = ipdAdmissionRepository.save(admission);
        auditLogService.logAction("APPROVE_ADMISSION", "IPD_ADMISSION", updated.getAdmissionId(), "Approved admission request");
        return mapToResponse(updated);
    }

    @Transactional
    public IpdAdmissionResponse admitPatient(Long id, Long wardId, Long bedId) {
        IpdAdmission admission = ipdAdmissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found with ID: " + id));

        if (admission.getStatus() != AdmissionStatus.REQUESTED && admission.getStatus() != AdmissionStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot admit patient with status: " + admission.getStatus());
        }

        allocateBedInternal(admission, wardId, bedId, AllocationType.INITIAL, "Admission Bed Allocation");
        admission.setStatus(AdmissionStatus.ADMITTED);
        IpdAdmission updated = ipdAdmissionRepository.save(admission);

        auditLogService.logAction("ADMIT_PATIENT", "IPD_ADMISSION", updated.getAdmissionId(), "Admitted patient to ward " + updated.getWard().getWardCode());
        return mapToResponse(updated);
    }

    @Transactional
    public BedAllocationResponse allocateBed(Long admissionId, BedAllocationRequest request) {
        IpdAdmission admission = ipdAdmissionRepository.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found with ID: " + admissionId));

        if (admission.getStatus() == AdmissionStatus.DISCHARGED || admission.getStatus() == AdmissionStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot allocate bed to a discharged/cancelled admission");
        }

        BedAllocation allocation = allocateBedInternal(
                admission, request.getWardId(), request.getBedId(),
                request.getAllocationType() != null ? request.getAllocationType() : AllocationType.INITIAL,
                request.getReason() != null ? request.getReason() : "Bed Allocation"
        );

        if (admission.getStatus() == AdmissionStatus.REQUESTED || admission.getStatus() == AdmissionStatus.APPROVED) {
            admission.setStatus(AdmissionStatus.ADMITTED);
            ipdAdmissionRepository.save(admission);
        }

        return mapToAllocationResponse(allocation);
    }

    @Transactional
    public WardTransferResponse transferPatient(Long admissionId, WardTransferRequest request) {
        IpdAdmission admission = ipdAdmissionRepository.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found with ID: " + admissionId));

        if (admission.getStatus() != AdmissionStatus.ADMITTED && admission.getStatus() != AdmissionStatus.DISCHARGE_PLANNED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only active admitted patients can be transferred");
        }

        Ward fromWard = admission.getWard();
        Bed fromBed = admission.getBed();

        if (fromBed == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient does not currently have an allocated bed");
        }

        // Lock new bed
        Bed toBed = bedRepository.findByIdForUpdate(request.getToBedId())
                .orElseThrow(() -> new ResourceNotFoundException("Target Bed not found with ID: " + request.getToBedId()));

        if (toBed.getStatus() != BedStatus.AVAILABLE || !Boolean.TRUE.equals(toBed.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Target bed is not available for transfer");
        }

        Ward toWard = wardRepository.findById(request.getToWardId())
                .orElseThrow(() -> new ResourceNotFoundException("Target Ward not found with ID: " + request.getToWardId()));

        if (!toBed.getWard().getId().equals(toWard.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target bed does not belong to the target ward");
        }

        // 1. Close current bed allocation
        bedAllocationRepository.findFirstByAdmissionIdAndEndDatetimeIsNullOrderByStartDatetimeDesc(admissionId)
                .ifPresent(curr -> {
                    curr.setEndDatetime(LocalDateTime.now());
                    bedAllocationRepository.save(curr);
                });

        // 2. Release old bed to CLEANING
        fromBed.setStatus(BedStatus.CLEANING);
        bedRepository.save(fromBed);

        // 3. Occupy new bed
        toBed.setStatus(BedStatus.OCCUPIED);
        bedRepository.save(toBed);

        // 4. Update admission
        admission.setWard(toWard);
        admission.setBed(toBed);
        ipdAdmissionRepository.save(admission);

        // 5. Create new allocation
        String currentUsername = getCurrentUsername();
        BedAllocation newAllocation = BedAllocation.builder()
                .admission(admission)
                .patient(admission.getPatient())
                .ward(toWard)
                .bed(toBed)
                .allocationType(AllocationType.TRANSFER)
                .startDatetime(LocalDateTime.now())
                .allocatedBy(currentUsername)
                .reason(request.getReason() != null ? request.getReason() : "Ward Transfer")
                .build();
        bedAllocationRepository.save(newAllocation);

        // 6. Record transfer history
        String trfId = "TRF-" + LocalDate.now().getYear() + "-" + String.format("%06d", (int)(System.currentTimeMillis() % 1000000));
        WardTransfer transfer = WardTransfer.builder()
                .transferId(trfId)
                .admission(admission)
                .patient(admission.getPatient())
                .fromWard(fromWard)
                .fromBed(fromBed)
                .toWard(toWard)
                .toBed(toBed)
                .transferDatetime(LocalDateTime.now())
                .reason(request.getReason())
                .requestedBy(currentUsername)
                .approvedBy(currentUsername)
                .status(TransferStatus.COMPLETED)
                .build();

        WardTransfer savedTrf = wardTransferRepository.save(transfer);
        auditLogService.logAction("TRANSFER_PATIENT", "WARD_TRANSFER", savedTrf.getTransferId(),
                "Transferred patient from " + fromBed.getBedCode() + " to " + toBed.getBedCode());

        return mapToTransferResponse(savedTrf);
    }

    @Transactional
    public IpdAdmissionResponse cancelAdmission(Long id, String reason) {
        IpdAdmission admission = ipdAdmissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found with ID: " + id));

        if (admission.getStatus() == AdmissionStatus.DISCHARGED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel a discharged admission");
        }

        if (admission.getBed() != null) {
            Bed bed = admission.getBed();
            bed.setStatus(BedStatus.AVAILABLE);
            bedRepository.save(bed);

            bedAllocationRepository.findFirstByAdmissionIdAndEndDatetimeIsNullOrderByStartDatetimeDesc(id)
                    .ifPresent(curr -> {
                        curr.setEndDatetime(LocalDateTime.now());
                        bedAllocationRepository.save(curr);
                    });
        }

        admission.setStatus(AdmissionStatus.CANCELLED);
        IpdAdmission updated = ipdAdmissionRepository.save(admission);
        auditLogService.logAction("CANCEL_ADMISSION", "IPD_ADMISSION", updated.getAdmissionId(), "Cancelled admission");
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<BedAllocationResponse> getBedHistory(Long admissionId) {
        return bedAllocationRepository.findByAdmissionIdOrderByStartDatetimeDesc(admissionId).stream()
                .map(this::mapToAllocationResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WardTransferResponse> getTransferHistory(Long admissionId) {
        return wardTransferRepository.findByAdmissionIdOrderByTransferDatetimeDesc(admissionId).stream()
                .map(this::mapToTransferResponse)
                .collect(Collectors.toList());
    }

    private BedAllocation allocateBedInternal(IpdAdmission admission, Long wardId, Long bedId, AllocationType allocationType, String reason) {
        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found with ID: " + wardId));
        if (!Boolean.TRUE.equals(ward.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected ward is inactive");
        }

        // Pessimistic Lock on Bed
        Bed bed = bedRepository.findByIdForUpdate(bedId)
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found with ID: " + bedId));

        if (bed.getStatus() != BedStatus.AVAILABLE || !Boolean.TRUE.equals(bed.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bed " + bed.getBedCode() + " is not available");
        }

        if (!bed.getWard().getId().equals(ward.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bed does not belong to selected ward");
        }

        // Close previous allocation if any
        if (admission.getBed() != null) {
            Bed oldBed = admission.getBed();
            oldBed.setStatus(BedStatus.AVAILABLE);
            bedRepository.save(oldBed);

            bedAllocationRepository.findFirstByAdmissionIdAndEndDatetimeIsNullOrderByStartDatetimeDesc(admission.getId())
                    .ifPresent(curr -> {
                        curr.setEndDatetime(LocalDateTime.now());
                        bedAllocationRepository.save(curr);
                    });
        }

        bed.setStatus(BedStatus.OCCUPIED);
        bedRepository.save(bed);

        admission.setWard(ward);
        admission.setBed(bed);
        ipdAdmissionRepository.save(admission);

        String currentUsername = getCurrentUsername();
        BedAllocation allocation = BedAllocation.builder()
                .admission(admission)
                .patient(admission.getPatient())
                .ward(ward)
                .bed(bed)
                .allocationType(allocationType)
                .startDatetime(LocalDateTime.now())
                .allocatedBy(currentUsername)
                .reason(reason)
                .build();

        BedAllocation saved = bedAllocationRepository.save(allocation);
        auditLogService.logAction("ALLOCATE_BED", "BED_ALLOCATION", bed.getBedCode(), "Allocated bed " + bed.getBedCode() + " to admission " + admission.getAdmissionId());
        return saved;
    }

    private String generateAdmissionCode() {
        int year = LocalDate.now().getYear();
        long count = ipdAdmissionRepository.count() + 1;
        String code = String.format("ADM-%d-%06d", year, count);
        while (ipdAdmissionRepository.existsByAdmissionId(code)) {
            count++;
            code = String.format("ADM-%d-%06d", year, count);
        }
        return code;
    }

    private String getCurrentUsername() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
                return principal.getUsername();
            }
        } catch (Exception ignored) {}
        return "system";
    }

    public IpdAdmissionResponse mapToResponse(IpdAdmission admission) {
        Patient p = admission.getPatient();
        Doctor d = admission.getAdmittingDoctor();
        Department dept = admission.getDepartment();
        Ward w = admission.getWard();
        Bed b = admission.getBed();
        OpdVisit ov = admission.getOpdVisit();

        Integer age = p.getDateOfBirth() != null ? Period.between(p.getDateOfBirth(), LocalDate.now()).getYears() : null;

        return IpdAdmissionResponse.builder()
                .id(admission.getId())
                .admissionId(admission.getAdmissionId())
                .patientId(p.getId())
                .patientName(p.getFirstName() + " " + p.getLastName())
                .patientCode(p.getPatientId())
                .patientPhone(p.getPhone())
                .patientGender(p.getGender())
                .patientAge(age)
                .admittingDoctorId(d.getId())
                .doctorName(d.getFirstName() + " " + d.getLastName())
                .departmentId(dept.getId())
                .departmentName(dept.getDepartmentName())
                .wardId(w != null ? w.getId() : null)
                .wardCode(w != null ? w.getWardCode() : null)
                .wardName(w != null ? w.getWardName() : null)
                .bedId(b != null ? b.getId() : null)
                .bedCode(b != null ? b.getBedCode() : null)
                .bedNumber(b != null ? b.getBedNumber() : null)
                .opdVisitId(ov != null ? ov.getId() : null)
                .opdVisitNumber(ov != null ? ov.getOpdVisitId() : null)
                .admissionType(admission.getAdmissionType())
                .admissionDate(admission.getAdmissionDate())
                .admissionTime(admission.getAdmissionTime())
                .reasonForAdmission(admission.getReasonForAdmission())
                .clinicalSummary(admission.getClinicalSummary())
                .status(admission.getStatus())
                .expectedDischargeDate(admission.getExpectedDischargeDate())
                .actualDischargeDate(admission.getActualDischargeDate())
                .dischargeType(admission.getDischargeType())
                .createdBy(admission.getCreatedBy())
                .createdAt(admission.getCreatedAt())
                .updatedAt(admission.getUpdatedAt())
                .build();
    }

    public BedAllocationResponse mapToAllocationResponse(BedAllocation allocation) {
        Patient p = allocation.getPatient();
        Ward w = allocation.getWard();
        Bed b = allocation.getBed();

        return BedAllocationResponse.builder()
                .id(allocation.getId())
                .admissionId(allocation.getAdmission().getId())
                .admissionCode(allocation.getAdmission().getAdmissionId())
                .patientId(p.getId())
                .patientName(p.getFirstName() + " " + p.getLastName())
                .wardId(w.getId())
                .wardCode(w.getWardCode())
                .wardName(w.getWardName())
                .bedId(b.getId())
                .bedCode(b.getBedCode())
                .bedNumber(b.getBedNumber())
                .allocationType(allocation.getAllocationType())
                .startDatetime(allocation.getStartDatetime())
                .endDatetime(allocation.getEndDatetime())
                .allocatedBy(allocation.getAllocatedBy())
                .reason(allocation.getReason())
                .createdAt(allocation.getCreatedAt())
                .build();
    }

    public WardTransferResponse mapToTransferResponse(WardTransfer transfer) {
        Patient p = transfer.getPatient();
        return WardTransferResponse.builder()
                .id(transfer.getId())
                .transferId(transfer.getTransferId())
                .admissionId(transfer.getAdmission().getId())
                .admissionCode(transfer.getAdmission().getAdmissionId())
                .patientId(p.getId())
                .patientName(p.getFirstName() + " " + p.getLastName())
                .fromWardId(transfer.getFromWard().getId())
                .fromWardName(transfer.getFromWard().getWardName())
                .fromBedId(transfer.getFromBed().getId())
                .fromBedCode(transfer.getFromBed().getBedCode())
                .toWardId(transfer.getToWard().getId())
                .toWardName(transfer.getToWard().getWardName())
                .toBedId(transfer.getToBed().getId())
                .toBedCode(transfer.getToBed().getBedCode())
                .transferDatetime(transfer.getTransferDatetime())
                .reason(transfer.getReason())
                .requestedBy(transfer.getRequestedBy())
                .approvedBy(transfer.getApprovedBy())
                .status(transfer.getStatus())
                .createdAt(transfer.getCreatedAt())
                .build();
    }
}
