package com.hospital.service;

import com.hospital.dto.DoctorRequestDto;
import com.hospital.dto.DoctorResponseDto;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.exception.DuplicateResourceException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.DoctorRepository;
import com.hospital.validation.IndiaValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;

    public Page<DoctorResponseDto> searchDoctors(String query, Long departmentId, Boolean isActive, Pageable pageable) {
        return doctorRepository.searchDoctors(query, departmentId, isActive, pageable).map(this::mapToDto);
    }

    public DoctorResponseDto getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        return mapToDto(doctor);
    }

    @Transactional
    public DoctorResponseDto createDoctor(DoctorRequestDto request, Long currentUserId) {
        if (doctorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Doctor email '" + request.getEmail() + "' is already registered");
        }

        if (doctorRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new DuplicateResourceException("License number '" + request.getLicenseNumber() + "' is already registered");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        String generatedDoctorId = generateUniqueDoctorId();
        String normalizedPhone = IndiaValidationUtils.normalizeIndianPhone(request.getPhone());

        Doctor doctor = Doctor.builder()
                .doctorId(generatedDoctorId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(normalizedPhone)
                .specialization(request.getSpecialization())
                .qualification(request.getQualification())
                .licenseNumber(request.getLicenseNumber())
                .department(department)
                .consultationFee(request.getConsultationFee())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .district(request.getDistrict())
                .state(request.getState())
                .pincode(request.getPincode())
                .country(request.getCountry() != null ? request.getCountry() : "India")
                .joiningDate(request.getJoiningDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Doctor saved = doctorRepository.save(doctor);

        auditLogService.logAction(currentUserId, "CREATE_DOCTOR", "DOCTOR", saved.getId().toString(),
                "Registered doctor: " + saved.getDoctorId() + " (" + saved.getFirstName() + " " + saved.getLastName() + ")");

        return mapToDto(saved);
    }

    @Transactional
    public DoctorResponseDto updateDoctor(Long id, DoctorRequestDto request, Long currentUserId) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));

        if (!doctor.getEmail().equalsIgnoreCase(request.getEmail()) && doctorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Doctor email '" + request.getEmail() + "' is already registered");
        }

        if (!doctor.getLicenseNumber().equalsIgnoreCase(request.getLicenseNumber()) && doctorRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new DuplicateResourceException("License number '" + request.getLicenseNumber() + "' is already registered");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        String normalizedPhone = IndiaValidationUtils.normalizeIndianPhone(request.getPhone());

        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setEmail(request.getEmail());
        doctor.setPhone(normalizedPhone);
        doctor.setSpecialization(request.getSpecialization());
        doctor.setQualification(request.getQualification());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setDepartment(department);
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setAddressLine1(request.getAddressLine1());
        doctor.setAddressLine2(request.getAddressLine2());
        doctor.setCity(request.getCity());
        doctor.setDistrict(request.getDistrict());
        doctor.setState(request.getState());
        doctor.setPincode(request.getPincode());
        if (request.getCountry() != null) {
            doctor.setCountry(request.getCountry());
        }
        doctor.setJoiningDate(request.getJoiningDate());

        if (request.getIsActive() != null) {
            doctor.setIsActive(request.getIsActive());
        }

        Doctor updated = doctorRepository.save(doctor);

        auditLogService.logAction(currentUserId, "UPDATE_DOCTOR", "DOCTOR", updated.getId().toString(),
                "Updated doctor: " + updated.getDoctorId());

        return mapToDto(updated);
    }

    @Transactional
    public DoctorResponseDto updateDoctorStatus(Long id, Boolean isActive, Long currentUserId) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));

        doctor.setIsActive(isActive);
        Doctor updated = doctorRepository.save(doctor);

        auditLogService.logAction(currentUserId, "UPDATE_DOCTOR_STATUS", "DOCTOR", updated.getId().toString(),
                "Set doctor active status to: " + isActive + " for doctor ID: " + updated.getDoctorId());

        return mapToDto(updated);
    }

    private synchronized String generateUniqueDoctorId() {
        int currentYear = Year.now().getValue();
        long count = doctorRepository.count() + 1;
        String formattedId;
        do {
            formattedId = String.format("DOC-%d-%06d", currentYear, count);
            count++;
        } while (doctorRepository.existsByDoctorId(formattedId));
        return formattedId;
    }

    private DoctorResponseDto mapToDto(Doctor doctor) {
        return DoctorResponseDto.builder()
                .id(doctor.getId())
                .doctorId(doctor.getDoctorId())
                .firstName(doctor.getFirstName())
                .lastName(doctor.getLastName())
                .email(doctor.getEmail())
                .phone(doctor.getPhone())
                .specialization(doctor.getSpecialization())
                .qualification(doctor.getQualification())
                .licenseNumber(doctor.getLicenseNumber())
                .departmentId(doctor.getDepartment().getId())
                .departmentName(doctor.getDepartment().getDepartmentName())
                .departmentCode(doctor.getDepartment().getDepartmentCode())
                .consultationFee(doctor.getConsultationFee())
                .currency("INR")
                .addressLine1(doctor.getAddressLine1())
                .addressLine2(doctor.getAddressLine2())
                .city(doctor.getCity())
                .district(doctor.getDistrict())
                .state(doctor.getState())
                .pincode(doctor.getPincode())
                .country(doctor.getCountry())
                .joiningDate(doctor.getJoiningDate())
                .isActive(doctor.getIsActive())
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .build();
    }
}
