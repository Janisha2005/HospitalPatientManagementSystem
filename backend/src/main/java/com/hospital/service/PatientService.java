package com.hospital.service;

import com.hospital.dto.PatientRequestDto;
import com.hospital.dto.PatientResponseDto;
import com.hospital.entity.Patient;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.PatientRepository;
import com.hospital.validation.IndiaValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final AuditLogService auditLogService;

    public Page<PatientResponseDto> searchPatients(String query, Boolean isActive, Pageable pageable) {
        return patientRepository.searchPatients(query, isActive, pageable).map(this::mapToDto);
    }

    public PatientResponseDto getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        return mapToDto(patient);
    }

    public PatientResponseDto getPatientByPatientId(String patientId) {
        Patient patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with patient ID: " + patientId));
        return mapToDto(patient);
    }

    @Transactional
    public PatientResponseDto createPatient(PatientRequestDto request, Long currentUserId) {
        String generatedPatientId = generateUniquePatientId();
        String normalizedPhone = IndiaValidationUtils.normalizeIndianPhone(request.getPhone());
        String normalizedEmergencyPhone = IndiaValidationUtils.normalizeIndianPhone(request.getEmergencyContactPhone());

        Patient patient = Patient.builder()
                .patientId(generatedPatientId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .bloodGroup(request.getBloodGroup())
                .phone(normalizedPhone)
                .email(request.getEmail())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .district(request.getDistrict())
                .state(request.getState())
                .pincode(request.getPincode())
                .country(request.getCountry() != null ? request.getCountry() : "India")
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(normalizedEmergencyPhone)
                .emergencyContactRelationship(request.getEmergencyContactRelationship())
                .registrationDate(LocalDate.now())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Patient saved = patientRepository.save(patient);

        auditLogService.logAction(currentUserId, "CREATE_PATIENT", "PATIENT", saved.getId().toString(),
                "Registered patient: " + saved.getPatientId() + " (" + saved.getFirstName() + " " + saved.getLastName() + ")");

        return mapToDto(saved);
    }

    @Transactional
    public PatientResponseDto updatePatient(Long id, PatientRequestDto request, Long currentUserId) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        String normalizedPhone = IndiaValidationUtils.normalizeIndianPhone(request.getPhone());
        String normalizedEmergencyPhone = IndiaValidationUtils.normalizeIndianPhone(request.getEmergencyContactPhone());

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setPhone(normalizedPhone);
        patient.setEmail(request.getEmail());
        patient.setAddressLine1(request.getAddressLine1());
        patient.setAddressLine2(request.getAddressLine2());
        patient.setCity(request.getCity());
        patient.setDistrict(request.getDistrict());
        patient.setState(request.getState());
        patient.setPincode(request.getPincode());
        if (request.getCountry() != null) {
            patient.setCountry(request.getCountry());
        }
        patient.setEmergencyContactName(request.getEmergencyContactName());
        patient.setEmergencyContactPhone(normalizedEmergencyPhone);
        patient.setEmergencyContactRelationship(request.getEmergencyContactRelationship());

        if (request.getIsActive() != null) {
            patient.setIsActive(request.getIsActive());
        }

        Patient updated = patientRepository.save(patient);

        auditLogService.logAction(currentUserId, "UPDATE_PATIENT", "PATIENT", updated.getId().toString(),
                "Updated patient: " + updated.getPatientId());

        return mapToDto(updated);
    }

    @Transactional
    public PatientResponseDto updatePatientStatus(Long id, Boolean isActive, Long currentUserId) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        patient.setIsActive(isActive);
        Patient updated = patientRepository.save(patient);

        auditLogService.logAction(currentUserId, "UPDATE_PATIENT_STATUS", "PATIENT", updated.getId().toString(),
                "Set patient active status to: " + isActive + " for patient ID: " + updated.getPatientId());

        return mapToDto(updated);
    }

    private synchronized String generateUniquePatientId() {
        int currentYear = Year.now().getValue();
        long count = patientRepository.count() + 1;
        String formattedId;
        do {
            formattedId = String.format("PAT-%d-%06d", currentYear, count);
            count++;
        } while (patientRepository.existsByPatientId(formattedId));
        return formattedId;
    }

    private PatientResponseDto mapToDto(Patient patient) {
        return PatientResponseDto.builder()
                .id(patient.getId())
                .patientId(patient.getPatientId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .bloodGroup(patient.getBloodGroup())
                .phone(patient.getPhone())
                .email(patient.getEmail())
                .addressLine1(patient.getAddressLine1())
                .addressLine2(patient.getAddressLine2())
                .city(patient.getCity())
                .district(patient.getDistrict())
                .state(patient.getState())
                .pincode(patient.getPincode())
                .country(patient.getCountry())
                .emergencyContactName(patient.getEmergencyContactName())
                .emergencyContactPhone(patient.getEmergencyContactPhone())
                .emergencyContactRelationship(patient.getEmergencyContactRelationship())
                .registrationDate(patient.getRegistrationDate())
                .isActive(patient.getIsActive())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }
}
