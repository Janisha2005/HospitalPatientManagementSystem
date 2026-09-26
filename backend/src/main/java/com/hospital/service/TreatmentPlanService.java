package com.hospital.service;

import com.hospital.dto.TreatmentPlanRequest;
import com.hospital.dto.TreatmentPlanResponse;
import com.hospital.entity.*;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.OpdVisitRepository;
import com.hospital.repository.PatientRepository;
import com.hospital.repository.TreatmentPlanRepository;
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
public class TreatmentPlanService {

    private final TreatmentPlanRepository treatmentPlanRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final OpdVisitRepository opdVisitRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<TreatmentPlanResponse> getPatientTreatmentPlans(Long patientId, Pageable pageable) {
        checkPatientOwnershipById(patientId);
        return treatmentPlanRepository.findByPatientId(patientId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<TreatmentPlanResponse> getOpdVisitTreatmentPlans(Long opdVisitId) {
        return treatmentPlanRepository.findByOpdVisitId(opdVisitId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public TreatmentPlanResponse createTreatmentPlanForOpd(Long opdVisitId, TreatmentPlanRequest request) {
        OpdVisit visit = opdVisitRepository.findById(opdVisitId)
                .orElseThrow(() -> new ResourceNotFoundException("OPD Visit not found with ID: " + opdVisitId));

        // Mark existing ACTIVE treatment plans for this patient as SUPERSEDED to preserve clinical history
        List<TreatmentPlan> existingPlans = treatmentPlanRepository.findByPatientIdOrderByCreatedAtDesc(visit.getPatient().getId());
        existingPlans.stream()
                .filter(p -> p.getStatus() == TreatmentPlanStatus.ACTIVE)
                .forEach(p -> {
                    p.setStatus(TreatmentPlanStatus.SUPERSEDED);
                    treatmentPlanRepository.save(p);
                });

        String code = generateTreatmentPlanCode();

        TreatmentPlan plan = TreatmentPlan.builder()
                .treatmentPlanId(code)
                .patient(visit.getPatient())
                .opdVisit(visit)
                .doctor(visit.getDoctor())
                .planDetails(request.getPlanDetails())
                .followUpDate(request.getFollowUpDate())
                .status(request.getStatus() != null ? request.getStatus() : TreatmentPlanStatus.ACTIVE)
                .build();

        TreatmentPlan saved = treatmentPlanRepository.save(plan);

        // Also sync main OPD visit treatment plan text
        if (visit.getTreatmentPlan() == null || visit.getTreatmentPlan().isBlank()) {
            visit.setTreatmentPlan(saved.getPlanDetails());
            opdVisitRepository.save(visit);
        }

        auditLogService.logAction("CREATE_TREATMENT_PLAN", "TreatmentPlan", saved.getId().toString(),
                "Created treatment plan " + saved.getTreatmentPlanId() + " for patient " + visit.getPatient().getPatientId());

        return mapToResponse(saved);
    }

    @Transactional
    public TreatmentPlanResponse updateTreatmentPlan(Long id, TreatmentPlanRequest request) {
        TreatmentPlan plan = treatmentPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Treatment Plan not found with ID: " + id));

        if (request.getPlanDetails() != null) plan.setPlanDetails(request.getPlanDetails());
        if (request.getFollowUpDate() != null) plan.setFollowUpDate(request.getFollowUpDate());
        if (request.getStatus() != null) plan.setStatus(request.getStatus());

        TreatmentPlan saved = treatmentPlanRepository.save(plan);

        auditLogService.logAction("UPDATE_TREATMENT_PLAN", "TreatmentPlan", saved.getId().toString(),
                "Updated treatment plan details for ID: " + saved.getTreatmentPlanId());

        return mapToResponse(saved);
    }

    @Transactional
    public TreatmentPlanResponse updateStatus(Long id, TreatmentPlanStatus status) {
        TreatmentPlan plan = treatmentPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Treatment Plan not found with ID: " + id));

        plan.setStatus(status);
        TreatmentPlan saved = treatmentPlanRepository.save(plan);

        auditLogService.logAction("UPDATE_TREATMENT_PLAN_STATUS", "TreatmentPlan", saved.getId().toString(),
                "Updated status to " + status + " for treatment plan ID: " + saved.getTreatmentPlanId());

        return mapToResponse(saved);
    }

    private String generateTreatmentPlanCode() {
        int year = LocalDate.now().getYear();
        long count = treatmentPlanRepository.count() + 1;
        return String.format("TP-%d-%06d", year, count);
    }

    private void checkPatientOwnershipById(Long patientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userDetails) {
            boolean isPatient = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
            if (isPatient) {
                Patient patient = patientRepository.findById(patientId).orElse(null);
                if (patient != null && !userDetails.getUsername().equalsIgnoreCase(patient.getEmail())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Patient can only access their own treatment plan history");
                }
            }
        }
    }

    private TreatmentPlanResponse mapToResponse(TreatmentPlan t) {
        return TreatmentPlanResponse.builder()
                .id(t.getId())
                .treatmentPlanId(t.getTreatmentPlanId())
                .patientId(t.getPatient().getId())
                .patientName(t.getPatient().getFirstName() + " " + t.getPatient().getLastName())
                .opdVisitId(t.getOpdVisit() != null ? t.getOpdVisit().getId() : null)
                .opdVisitCode(t.getOpdVisit() != null ? t.getOpdVisit().getOpdVisitId() : null)
                .doctorId(t.getDoctor().getId())
                .doctorName("Dr. " + t.getDoctor().getFirstName() + " " + t.getDoctor().getLastName())
                .planDetails(t.getPlanDetails())
                .followUpDate(t.getFollowUpDate())
                .status(t.getStatus())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
