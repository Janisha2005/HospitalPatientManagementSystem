package com.hospital.service;

import com.hospital.dto.DepartmentRequestDto;
import com.hospital.dto.DepartmentResponseDto;
import com.hospital.entity.Department;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.DuplicateResourceException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final AuditLogService auditLogService;

    public Page<DepartmentResponseDto> getAllDepartments(Pageable pageable) {
        return departmentRepository.findAll(pageable).map(this::mapToDto);
    }

    public List<DepartmentResponseDto> getAllActiveDepartments() {
        return departmentRepository.findAll().stream()
                .filter(d -> Boolean.TRUE.equals(d.getIsActive()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public DepartmentResponseDto getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return mapToDto(department);
    }

    @Transactional
    public DepartmentResponseDto createDepartment(DepartmentRequestDto request, Long currentUserId) {
        String code = request.getDepartmentCode().toUpperCase();
        if (departmentRepository.existsByDepartmentCode(code)) {
            throw new DuplicateResourceException("Department code '" + code + "' already exists");
        }

        Department department = Department.builder()
                .departmentCode(code)
                .departmentName(request.getDepartmentName())
                .description(request.getDescription())
                .location(request.getLocation())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Department saved = departmentRepository.save(department);

        auditLogService.logAction(currentUserId, "CREATE_DEPARTMENT", "DEPARTMENT", saved.getId().toString(),
                "Created department: " + saved.getDepartmentCode() + " - " + saved.getDepartmentName());

        return mapToDto(saved);
    }

    @Transactional
    public DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto request, Long currentUserId) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        String code = request.getDepartmentCode().toUpperCase();
        if (!department.getDepartmentCode().equalsIgnoreCase(code) && departmentRepository.existsByDepartmentCode(code)) {
            throw new DuplicateResourceException("Department code '" + code + "' already exists");
        }

        department.setDepartmentCode(code);
        department.setDepartmentName(request.getDepartmentName());
        department.setDescription(request.getDescription());
        department.setLocation(request.getLocation());
        if (request.getIsActive() != null) {
            department.setIsActive(request.getIsActive());
        }

        Department updated = departmentRepository.save(department);

        auditLogService.logAction(currentUserId, "UPDATE_DEPARTMENT", "DEPARTMENT", updated.getId().toString(),
                "Updated department: " + updated.getDepartmentCode());

        return mapToDto(updated);
    }

    @Transactional
    public DepartmentResponseDto updateDepartmentStatus(Long id, Boolean isActive, Long currentUserId) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        if (Boolean.FALSE.equals(isActive)) {
            long doctorCount = doctorRepository.countByDepartmentId(id);
            if (doctorCount > 0) {
                // Warning logged but status change permitted as deactivation, or throw if active doctors exist
            }
        }

        department.setIsActive(isActive);
        Department updated = departmentRepository.save(department);

        auditLogService.logAction(currentUserId, "UPDATE_DEPARTMENT_STATUS", "DEPARTMENT", updated.getId().toString(),
                "Set department active status to: " + isActive + " for: " + updated.getDepartmentCode());

        return mapToDto(updated);
    }

    @Transactional
    public void deleteDepartment(Long id, Long currentUserId) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        long doctorCount = doctorRepository.countByDepartmentId(id);
        if (doctorCount > 0) {
            throw new BadRequestException("Cannot delete department with " + doctorCount + " assigned doctor(s). Deactivate the department or reassign doctors first.");
        }

        departmentRepository.delete(department);

        auditLogService.logAction(currentUserId, "DELETE_DEPARTMENT", "DEPARTMENT", id.toString(),
                "Deleted department: " + department.getDepartmentCode());
    }

    private DepartmentResponseDto mapToDto(Department department) {
        long doctorCount = doctorRepository.countByDepartmentId(department.getId());
        return DepartmentResponseDto.builder()
                .id(department.getId())
                .departmentCode(department.getDepartmentCode())
                .departmentName(department.getDepartmentName())
                .description(department.getDescription())
                .location(department.getLocation())
                .isActive(department.getIsActive())
                .doctorCount(doctorCount)
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }
}
