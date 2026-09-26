package com.hospital.service;

import com.hospital.dto.AllergyRequest;
import com.hospital.dto.AllergyResponse;
import com.hospital.entity.AllergyStatus;
import com.hospital.entity.Patient;
import com.hospital.entity.PatientAllergy;
import com.hospital.entity.User;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.PatientAllergyRepository;
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
public class AllergyService {

    private final PatientAllergyRepository allergyRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<AllergyResponse> getPatientAllergies(Long patientId) {
        checkPatientOwnershipById(patientId);
        return allergyRepository.findByPatientIdOrderByRecordedAtDesc(patientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AllergyResponse addAllergy(Long patientId, AllergyRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + patientId));

        User currentUser = getCurrentUser();

        PatientAllergy allergy = PatientAllergy.builder()
                .patient(patient)
                .allergen(request.getAllergen())
                .allergyType(request.getAllergyType())
                .reaction(request.getReaction())
                .severity(request.getSeverity())
                .status(request.getStatus() != null ? request.getStatus() : AllergyStatus.ACTIVE)
                .recordedBy(currentUser)
                .build();

        PatientAllergy saved = allergyRepository.save(allergy);

        auditLogService.logAction("ADD_ALLERGY", "PatientAllergy", saved.getId().toString(),
                "Added allergy (" + saved.getAllergen() + ") for patient ID: " + patient.getPatientId());

        return mapToResponse(saved);
    }

    @Transactional
    public AllergyResponse updateAllergy(Long patientId, Long allergyId, AllergyRequest request) {
        PatientAllergy allergy = allergyRepository.findById(allergyId)
                .orElseThrow(() -> new ResourceNotFoundException("Allergy record not found with ID: " + allergyId));

        if (!allergy.getPatient().getId().equals(patientId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allergy record does not belong to patient ID: " + patientId);
        }

        if (request.getAllergen() != null) allergy.setAllergen(request.getAllergen());
        if (request.getAllergyType() != null) allergy.setAllergyType(request.getAllergyType());
        if (request.getReaction() != null) allergy.setReaction(request.getReaction());
        if (request.getSeverity() != null) allergy.setSeverity(request.getSeverity());
        if (request.getStatus() != null) allergy.setStatus(request.getStatus());

        PatientAllergy saved = allergyRepository.save(allergy);

        auditLogService.logAction("UPDATE_ALLERGY", "PatientAllergy", saved.getId().toString(),
                "Updated allergy details for ID: " + allergyId);

        return mapToResponse(saved);
    }

    @Transactional
    public AllergyResponse updateStatus(Long patientId, Long allergyId, AllergyStatus status) {
        PatientAllergy allergy = allergyRepository.findById(allergyId)
                .orElseThrow(() -> new ResourceNotFoundException("Allergy record not found with ID: " + allergyId));

        if (!allergy.getPatient().getId().equals(patientId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allergy record does not belong to patient ID: " + patientId);
        }

        allergy.setStatus(status);
        PatientAllergy saved = allergyRepository.save(allergy);

        auditLogService.logAction("UPDATE_ALLERGY_STATUS", "PatientAllergy", saved.getId().toString(),
                "Updated allergy status to " + status + " for ID: " + allergyId);

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
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Patient can only access their own allergy records");
                }
            }
        }
    }

    private AllergyResponse mapToResponse(PatientAllergy a) {
        return AllergyResponse.builder()
                .id(a.getId())
                .patientId(a.getPatient().getId())
                .patientName(a.getPatient().getFirstName() + " " + a.getPatient().getLastName())
                .allergen(a.getAllergen())
                .allergyType(a.getAllergyType())
                .reaction(a.getReaction())
                .severity(a.getSeverity())
                .status(a.getStatus())
                .recordedByName(a.getRecordedBy() != null ? a.getRecordedBy().getFullName() : null)
                .recordedAt(a.getRecordedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
