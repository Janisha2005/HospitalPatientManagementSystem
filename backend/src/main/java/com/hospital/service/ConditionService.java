package com.hospital.service;

import com.hospital.dto.ConditionRequest;
import com.hospital.dto.ConditionResponse;
import com.hospital.entity.ConditionStatus;
import com.hospital.entity.Patient;
import com.hospital.entity.PatientCondition;
import com.hospital.entity.User;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.PatientConditionRepository;
import com.hospital.repository.PatientRepository;
import com.hospital.repository.UserRepository;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConditionService {

    private final PatientConditionRepository conditionRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<ConditionResponse> getPatientConditions(Long patientId) {
        checkPatientOwnershipById(patientId);
        return conditionRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ConditionResponse addCondition(Long patientId, ConditionRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + patientId));

        User currentUser = getCurrentUser();

        PatientCondition condition = PatientCondition.builder()
                .patient(patient)
                .conditionName(request.getConditionName())
                .description(request.getDescription())
                .diagnosedDate(request.getDiagnosedDate())
                .status(request.getStatus() != null ? request.getStatus() : ConditionStatus.ACTIVE)
                .recordedBy(currentUser)
                .build();

        PatientCondition saved = conditionRepository.save(condition);

        auditLogService.logAction("ADD_CONDITION", "PatientCondition", saved.getId().toString(),
                "Added chronic condition (" + saved.getConditionName() + ") for patient ID: " + patient.getPatientId());

        return mapToResponse(saved);
    }

    @Transactional
    public ConditionResponse updateCondition(Long patientId, Long conditionId, ConditionRequest request) {
        PatientCondition condition = conditionRepository.findById(conditionId)
                .orElseThrow(() -> new ResourceNotFoundException("Condition record not found with ID: " + conditionId));

        if (!condition.getPatient().getId().equals(patientId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Condition record does not belong to patient ID: " + patientId);
        }

        if (request.getConditionName() != null) condition.setConditionName(request.getConditionName());
        if (request.getDescription() != null) condition.setDescription(request.getDescription());
        if (request.getDiagnosedDate() != null) condition.setDiagnosedDate(request.getDiagnosedDate());
        if (request.getStatus() != null) condition.setStatus(request.getStatus());

        PatientCondition saved = conditionRepository.save(condition);

        auditLogService.logAction("UPDATE_CONDITION", "PatientCondition", saved.getId().toString(),
                "Updated chronic condition details for ID: " + conditionId);

        return mapToResponse(saved);
    }

    @Transactional
    public ConditionResponse updateStatus(Long patientId, Long conditionId, ConditionStatus status) {
        PatientCondition condition = conditionRepository.findById(conditionId)
                .orElseThrow(() -> new ResourceNotFoundException("Condition record not found with ID: " + conditionId));

        if (!condition.getPatient().getId().equals(patientId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Condition record does not belong to patient ID: " + patientId);
        }

        condition.setStatus(status);
        PatientCondition saved = conditionRepository.save(condition);

        auditLogService.logAction("UPDATE_CONDITION_STATUS", "PatientCondition", saved.getId().toString(),
                "Updated condition status to " + status + " for ID: " + conditionId);

        return mapToResponse(saved);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userDetails) {
            return userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        }
        return null;
    }

    private void checkPatientOwnershipById(Long patientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userDetails) {
            boolean isPatient = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
            if (isPatient) {
                Patient patient = patientRepository.findById(patientId).orElse(null);
                if (patient != null && !userDetails.getUsername().equalsIgnoreCase(patient.getEmail())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Patient can only access their own chronic condition records");
                }
            }
        }
    }

    private ConditionResponse mapToResponse(PatientCondition c) {
        return ConditionResponse.builder()
                .id(c.getId())
                .patientId(c.getPatient().getId())
                .patientName(c.getPatient().getFirstName() + " " + c.getPatient().getLastName())
                .conditionName(c.getConditionName())
                .description(c.getDescription())
                .diagnosedDate(c.getDiagnosedDate())
                .status(c.getStatus())
                .recordedByName(c.getRecordedBy() != null ? c.getRecordedBy().getFullName() : null)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
