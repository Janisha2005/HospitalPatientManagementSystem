package com.hospital.service;

import com.hospital.dto.PrescriptionCreateRequest;
import com.hospital.dto.PrescriptionItemResponse;
import com.hospital.dto.PrescriptionResponse;
import com.hospital.entity.*;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.OpdVisitRepository;
import com.hospital.repository.PatientRepository;
import com.hospital.repository.PrescriptionRepository;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final OpdVisitRepository opdVisitRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<PrescriptionResponse> getPrescriptions(Long patientId, Long doctorId, LocalDate date,
                                                       PrescriptionStatus status, Pageable pageable) {
        checkPatientOwnershipById(patientId);
        Page<Prescription> page = prescriptionRepository.findPrescriptionsWithFilters(patientId, doctorId, date, status, pageable);
        return page.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionById(Long id) {
        Prescription p = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + id));
        checkPatientOwnershipById(p.getPatient().getId());
        return mapToResponse(p);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionByCode(String prescriptionId) {
        Prescription p = prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with Code: " + prescriptionId));
        checkPatientOwnershipById(p.getPatient().getId());
        return mapToResponse(p);
    }

    @Transactional
    public PrescriptionResponse createPrescription(PrescriptionCreateRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + request.getPatientId()));
        if (!patient.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot issue prescription for inactive patient");
        }

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + request.getDoctorId()));

        OpdVisit opdVisit = null;
        if (request.getOpdVisitId() != null) {
            opdVisit = opdVisitRepository.findById(request.getOpdVisitId()).orElse(null);
        }

        String code = generatePrescriptionCode();

        Prescription prescription = Prescription.builder()
                .prescriptionId(code)
                .patient(patient)
                .doctor(doctor)
                .opdVisit(opdVisit)
                .prescriptionDate(LocalDate.now())
                .status(PrescriptionStatus.DRAFT)
                .clinicalNotes(request.getClinicalNotes())
                .build();

        List<PrescriptionItem> items = request.getItems().stream().map(i -> PrescriptionItem.builder()
                .prescription(prescription)
                .medicineName(i.getMedicineName())
                .strength(i.getStrength())
                .dosage(i.getDosage())
                .route(i.getRoute())
                .frequency(i.getFrequency())
                .durationValue(i.getDurationValue())
                .durationUnit(i.getDurationUnit())
                .quantity(i.getQuantity())
                .instructions(i.getInstructions())
                .build()).collect(Collectors.toList());

        prescription.setItems(items);

        Prescription saved = prescriptionRepository.save(prescription);

        auditLogService.logAction("CREATE_PRESCRIPTION", "Prescription", saved.getId().toString(),
                "Created draft prescription " + saved.getPrescriptionId() + " for patient " + patient.getPatientId());

        return mapToResponse(saved);
    }

    @Transactional
    public PrescriptionResponse updatePrescription(Long id, PrescriptionCreateRequest request) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + id));

        if (prescription.getStatus() == PrescriptionStatus.ISSUED || prescription.getStatus() == PrescriptionStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Issued/Completed prescription is immutable and cannot be freely modified. Create a new prescription instead.");
        }

        if (request.getClinicalNotes() != null) {
            prescription.setClinicalNotes(request.getClinicalNotes());
        }

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            prescription.getItems().clear();
            List<PrescriptionItem> newItems = request.getItems().stream().map(i -> PrescriptionItem.builder()
                    .prescription(prescription)
                    .medicineName(i.getMedicineName())
                    .strength(i.getStrength())
                    .dosage(i.getDosage())
                    .route(i.getRoute())
                    .frequency(i.getFrequency())
                    .durationValue(i.getDurationValue())
                    .durationUnit(i.getDurationUnit())
                    .quantity(i.getQuantity())
                    .instructions(i.getInstructions())
                    .build()).collect(Collectors.toList());
            prescription.getItems().addAll(newItems);
        }

        Prescription saved = prescriptionRepository.save(prescription);

        auditLogService.logAction("UPDATE_PRESCRIPTION", "Prescription", saved.getId().toString(),
                "Updated draft prescription ID: " + saved.getPrescriptionId());

        return mapToResponse(saved);
    }

    @Transactional
    public PrescriptionResponse issuePrescription(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + id));

        if (prescription.getStatus() == PrescriptionStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot issue a cancelled prescription");
        }

        prescription.setStatus(PrescriptionStatus.ISSUED);
        Prescription saved = prescriptionRepository.save(prescription);

        auditLogService.logAction("ISSUE_PRESCRIPTION", "Prescription", saved.getId().toString(),
                "Issued prescription " + saved.getPrescriptionId() + " to patient " + saved.getPatient().getPatientId());

        return mapToResponse(saved);
    }

    @Transactional
    public PrescriptionResponse cancelPrescription(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + id));

        prescription.setStatus(PrescriptionStatus.CANCELLED);
        Prescription saved = prescriptionRepository.save(prescription);

        auditLogService.logAction("CANCEL_PRESCRIPTION", "Prescription", saved.getId().toString(),
                "Cancelled prescription " + saved.getPrescriptionId());

        return mapToResponse(saved);
    }

    private String generatePrescriptionCode() {
        int year = LocalDate.now().getYear();
        long count = prescriptionRepository.count() + 1;
        return String.format("RX-%d-%06d", year, count);
    }

    private void checkPatientOwnershipById(Long patientId) {
        if (patientId == null) return;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userDetails) {
            boolean isPatient = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
            if (isPatient) {
                Patient patient = patientRepository.findById(patientId).orElse(null);
                if (patient != null && !userDetails.getUsername().equalsIgnoreCase(patient.getEmail())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Patient can only access their own prescriptions");
                }
            }
        }
    }

    private PrescriptionResponse mapToResponse(Prescription p) {
        List<PrescriptionItemResponse> itemDtos = p.getItems().stream().map(i -> PrescriptionItemResponse.builder()
                .id(i.getId())
                .medicineName(i.getMedicineName())
                .strength(i.getStrength())
                .dosage(i.getDosage())
                .route(i.getRoute())
                .frequency(i.getFrequency())
                .durationValue(i.getDurationValue())
                .durationUnit(i.getDurationUnit())
                .quantity(i.getQuantity())
                .instructions(i.getInstructions())
                .build()).collect(Collectors.toList());

        return PrescriptionResponse.builder()
                .id(p.getId())
                .prescriptionId(p.getPrescriptionId())
                .patientId(p.getPatient().getId())
                .patientCode(p.getPatient().getPatientId())
                .patientName(p.getPatient().getFirstName() + " " + p.getPatient().getLastName())
                .doctorId(p.getDoctor().getId())
                .doctorName("Dr. " + p.getDoctor().getFirstName() + " " + p.getDoctor().getLastName())
                .opdVisitId(p.getOpdVisit() != null ? p.getOpdVisit().getId() : null)
                .opdVisitCode(p.getOpdVisit() != null ? p.getOpdVisit().getOpdVisitId() : null)
                .prescriptionDate(p.getPrescriptionDate())
                .status(p.getStatus())
                .clinicalNotes(p.getClinicalNotes())
                .items(itemDtos)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
