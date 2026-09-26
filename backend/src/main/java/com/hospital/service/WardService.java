package com.hospital.service;

import com.hospital.dto.WardRequest;
import com.hospital.dto.WardResponse;
import com.hospital.entity.BedStatus;
import com.hospital.entity.Department;
import com.hospital.entity.Ward;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.BedRepository;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.IpdAdmissionRepository;
import com.hospital.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WardService {

    private final WardRepository wardRepository;
    private final DepartmentRepository departmentRepository;
    private final BedRepository bedRepository;
    private final IpdAdmissionRepository ipdAdmissionRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<WardResponse> getAllWards(Long departmentId, Boolean activeOnly) {
        List<Ward> wards;
        if (departmentId != null) {
            wards = wardRepository.findByDepartmentIdAndIsActiveTrue(departmentId);
        } else if (Boolean.TRUE.equals(activeOnly)) {
            wards = wardRepository.findByIsActiveTrue();
        } else {
            wards = wardRepository.findAll();
        }
        return wards.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WardResponse getWardById(Long id) {
        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found with ID: " + id));
        return mapToResponse(ward);
    }

    @Transactional
    public WardResponse createWard(WardRequest request) {
        if (wardRepository.existsByWardCode(request.getWardCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ward code already exists: " + request.getWardCode());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        Ward ward = Ward.builder()
                .wardCode(request.getWardCode())
                .wardName(request.getWardName())
                .wardType(request.getWardType())
                .department(department)
                .floor(request.getFloor())
                .building(request.getBuilding())
                .genderPolicy(request.getGenderPolicy() != null ? request.getGenderPolicy() : com.hospital.entity.WardGenderPolicy.MIXED)
                .capacity(request.getCapacity() != null ? request.getCapacity() : 10)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Ward saved = wardRepository.save(ward);
        auditLogService.logAction("CREATE_WARD", "WARD", saved.getWardCode(), "Created ward " + saved.getWardName());
        return mapToResponse(saved);
    }

    @Transactional
    public WardResponse updateWard(Long id, WardRequest request) {
        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found with ID: " + id));

        if (!ward.getWardCode().equalsIgnoreCase(request.getWardCode()) && wardRepository.existsByWardCode(request.getWardCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ward code already exists: " + request.getWardCode());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        ward.setWardCode(request.getWardCode());
        ward.setWardName(request.getWardName());
        ward.setWardType(request.getWardType());
        ward.setDepartment(department);
        ward.setFloor(request.getFloor());
        ward.setBuilding(request.getBuilding());
        if (request.getGenderPolicy() != null) ward.setGenderPolicy(request.getGenderPolicy());
        if (request.getCapacity() != null) ward.setCapacity(request.getCapacity());
        if (request.getIsActive() != null) ward.setIsActive(request.getIsActive());

        Ward updated = wardRepository.save(ward);
        auditLogService.logAction("UPDATE_WARD", "WARD", updated.getWardCode(), "Updated ward " + updated.getWardName());
        return mapToResponse(updated);
    }

    @Transactional
    public WardResponse updateWardStatus(Long id, Boolean active) {
        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found with ID: " + id));
        ward.setIsActive(active);
        Ward updated = wardRepository.save(ward);
        auditLogService.logAction("UPDATE_WARD", "WARD", updated.getWardCode(), "Set ward active status to " + active);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteWard(Long id) {
        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found with ID: " + id));

        // Check if active beds or admissions exist
        long bedCount = bedRepository.findByWardIdAndIsActiveTrue(id).size();
        if (bedCount > 0 || ipdAdmissionRepository.existsByWardId(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete ward with existing beds or admissions. Deactivate it instead.");
        }

        wardRepository.delete(ward);
        auditLogService.logAction("DELETE_WARD", "WARD", ward.getWardCode(), "Deleted ward " + ward.getWardName());
    }

    public WardResponse mapToResponse(Ward ward) {
        long occupied = bedRepository.findByWardIdAndStatusAndIsActiveTrue(ward.getId(), BedStatus.OCCUPIED).size();
        long available = bedRepository.findByWardIdAndStatusAndIsActiveTrue(ward.getId(), BedStatus.AVAILABLE).size();

        return WardResponse.builder()
                .id(ward.getId())
                .wardCode(ward.getWardCode())
                .wardName(ward.getWardName())
                .wardType(ward.getWardType())
                .departmentId(ward.getDepartment().getId())
                .departmentName(ward.getDepartment().getDepartmentName())
                .floor(ward.getFloor())
                .building(ward.getBuilding())
                .genderPolicy(ward.getGenderPolicy())
                .capacity(ward.getCapacity())
                .occupiedBeds((int) occupied)
                .availableBeds((int) available)
                .isActive(ward.getIsActive())
                .createdAt(ward.getCreatedAt())
                .updatedAt(ward.getUpdatedAt())
                .build();
    }
}
