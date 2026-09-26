package com.hospital.service;

import com.hospital.dto.DiagnosisRequest;
import com.hospital.dto.DiagnosisResponse;
import com.hospital.entity.*;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.ClinicalDiagnosisRepository;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.OpdVisitRepository;
import com.hospital.repository.PatientRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private final ClinicalDiagnosisRepository diagnosisRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final OpdVisitRepository opdVisitRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<DiagnosisResponse> getPatientDiagnoses(Long patientId, Pageable pageable) {
        checkPatientOwnershipById(patientId);
        return diagnosisRepository.findByPatientId(patientId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<DiagnosisResponse> getOpdVisitDiagnoses(Long opdVisitId) {
        return diagnosisRepository.findByOpdVisitId(opdVisitId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public DiagnosisResponse createDiagnosisForOpd(Long opdVisitId, DiagnosisRequest request) {
        OpdVisit visit = opdVisitRepository.findById(opdVisitId)
                .orElseThrow(() -> new ResourceNotFoundException("OPD Visit not found with ID: " + opdVisitId));

        String code = generateDiagnosisCode();

        ClinicalDiagnosis diagnosis = ClinicalDiagnosis.builder()
                .diagnosisId(code)
                .patient(visit.getPatient())
                .opdVisit(visit)
                .doctor(visit.getDoctor())
                .diagnosisName(request.getDiagnosisName())
                .diagnosisDescription(request.getDiagnosisDescription())
                .diagnosisType(request.getDiagnosisType() != null ? request.getDiagnosisType() : DiagnosisType.PRIMARY)
                .status(request.getStatus() != null ? request.getStatus() : DiagnosisStatus.ACTIVE)
                .diagnosedAt(LocalDateTime.now())
                .build();

        ClinicalDiagnosis saved = diagnosisRepository.save(diagnosis);

        // Also sync main OPD visit diagnosis text field
        if (visit.getDiagnosis() == null || visit.getDiagnosis().isBlank()) {
            visit.setDiagnosis(saved.getDiagnosisName());
            opdVisitRepository.save(visit);
        }

        auditLogService.logAction("CREATE_DIAGNOSIS", "ClinicalDiagnosis", saved.getId().toString(),
                "Recorded diagnosis " + saved.getDiagnosisId() + " (" + saved.getDiagnosisName() + ") for patient " + visit.getPatient().getPatientId());

        return mapToResponse(saved);
    }

    @Transactional
    public DiagnosisResponse updateDiagnosis(Long id, DiagnosisRequest request) {
        ClinicalDiagnosis diagnosis = diagnosisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnosis not found with ID: " + id));

        if (request.getDiagnosisName() != null) diagnosis.setDiagnosisName(request.getDiagnosisName());
        if (request.getDiagnosisDescription() != null) diagnosis.setDiagnosisDescription(request.getDiagnosisDescription());
        if (request.getDiagnosisType() != null) diagnosis.setDiagnosisType(request.getDiagnosisType());
        if (request.getStatus() != null) diagnosis.setStatus(request.getStatus());

        ClinicalDiagnosis saved = diagnosisRepository.save(diagnosis);

        auditLogService.logAction("UPDATE_DIAGNOSIS", "ClinicalDiagnosis", saved.getId().toString(),
                "Updated diagnosis details for ID: " + saved.getDiagnosisId());

        return mapToResponse(saved);
    }

    @Transactional
    public DiagnosisResponse updateStatus(Long id, DiagnosisStatus status) {
        ClinicalDiagnosis diagnosis = diagnosisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnosis not found with ID: " + id));

        diagnosis.setStatus(status);
        ClinicalDiagnosis saved = diagnosisRepository.save(diagnosis);

        auditLogService.logAction("UPDATE_DIAGNOSIS_STATUS", "ClinicalDiagnosis", saved.getId().toString(),
                "Updated status to " + status + " for diagnosis ID: " + saved.getDiagnosisId());

        return mapToResponse(saved);
    }

    private String generateDiagnosisCode() {
        int year = LocalDate.now().getYear();
        long count = diagnosisRepository.count() + 1;
        return String.format("DX-%d-%06d", year, count);
    }

    private void checkPatientOwnershipById(Long patientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userDetails) {
            boolean isPatient = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
            if (isPatient) {
                Patient patient = patientRepository.findById(patientId).orElse(null);
                if (patient != null && !userDetails.getUsername().equalsIgnoreCase(patient.getEmail())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Patient can only access their own diagnosis history");
                }
            }
        }
    }

    private DiagnosisResponse mapToResponse(ClinicalDiagnosis d) {
        return DiagnosisResponse.builder()
                .id(d.getId())
                .diagnosisId(d.getDiagnosisId())
                .patientId(d.getPatient().getId())
                .patientName(d.getPatient().getFirstName() + " " + d.getPatient().getLastName())
                .opdVisitId(d.getOpdVisit() != null ? d.getOpdVisit().getId() : null)
                .opdVisitCode(d.getOpdVisit() != null ? d.getOpdVisit().getOpdVisitId() : null)
                .doctorId(d.getDoctor().getId())
                .doctorName("Dr. " + d.getDoctor().getFirstName() + " " + d.getDoctor().getLastName())
                .diagnosisName(d.getDiagnosisName())
                .diagnosisDescription(d.getDiagnosisDescription())
                .diagnosisType(d.getDiagnosisType())
                .status(d.getStatus())
                .diagnosedAt(d.getDiagnosedAt())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
