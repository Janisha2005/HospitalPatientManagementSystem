package com.hospital.service;

import com.hospital.dto.BedRequest;
import com.hospital.dto.BedResponse;
import com.hospital.entity.*;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.BedAllocationRepository;
import com.hospital.repository.BedRepository;
import com.hospital.repository.IpdAdmissionRepository;
import com.hospital.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BedService {

    private final BedRepository bedRepository;
    private final WardRepository wardRepository;
    private final IpdAdmissionRepository ipdAdmissionRepository;
    private final BedAllocationRepository bedAllocationRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<BedResponse> getAllBeds(Long wardId, BedType bedType, BedStatus status) {
        List<Bed> beds;
        if (wardId != null && status != null) {
            beds = bedRepository.findByWardIdAndStatusAndIsActiveTrue(wardId, status);
        } else if (wardId != null) {
            beds = bedRepository.findByWardIdAndIsActiveTrue(wardId);
        } else if (status != null) {
            beds = bedRepository.findByStatusAndIsActiveTrue(status);
        } else {
            beds = bedRepository.findAll();
        }

        if (bedType != null) {
            beds = beds.stream().filter(b -> b.getBedType() == bedType).collect(Collectors.toList());
        }

        return beds.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BedResponse> getAvailableBeds(Long wardId, BedType bedType) {
        return getAllBeds(wardId, bedType, BedStatus.AVAILABLE);
    }

    @Transactional(readOnly = true)
    public BedResponse getBedById(Long id) {
        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found with ID: " + id));
        return mapToResponse(bed);
    }

    @Transactional
    public BedResponse createBed(BedRequest request) {
        if (bedRepository.existsByBedCode(request.getBedCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bed code already exists: " + request.getBedCode());
        }
        if (bedRepository.existsByWardIdAndBedNumber(request.getWardId(), request.getBedNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bed number already exists in this ward: " + request.getBedNumber());
        }

        Ward ward = wardRepository.findById(request.getWardId())
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found with ID: " + request.getWardId()));

        if (!Boolean.TRUE.equals(ward.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot add bed to an inactive ward");
        }

        Bed bed = Bed.builder()
                .bedCode(request.getBedCode())
                .ward(ward)
                .bedNumber(request.getBedNumber())
                .bedType(request.getBedType())
                .status(request.getStatus() != null ? request.getStatus() : BedStatus.AVAILABLE)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Bed saved = bedRepository.save(bed);
        auditLogService.logAction("CREATE_BED", "BED", saved.getBedCode(), "Created bed " + saved.getBedCode());
        return mapToResponse(saved);
    }

    @Transactional
    public BedResponse updateBed(Long id, BedRequest request) {
        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found with ID: " + id));

        if (!bed.getBedCode().equalsIgnoreCase(request.getBedCode()) && bedRepository.existsByBedCode(request.getBedCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bed code already exists: " + request.getBedCode());
        }

        if (!bed.getWard().getId().equals(request.getWardId()) || !bed.getBedNumber().equalsIgnoreCase(request.getBedNumber())) {
            if (bedRepository.existsByWardIdAndBedNumber(request.getWardId(), request.getBedNumber())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Bed number already exists in this ward: " + request.getBedNumber());
            }
        }

        Ward ward = wardRepository.findById(request.getWardId())
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found with ID: " + request.getWardId()));

        bed.setBedCode(request.getBedCode());
        bed.setWard(ward);
        bed.setBedNumber(request.getBedNumber());
        bed.setBedType(request.getBedType());
        if (request.getStatus() != null) bed.setStatus(request.getStatus());
        if (request.getIsActive() != null) bed.setIsActive(request.getIsActive());

        Bed updated = bedRepository.save(bed);
        auditLogService.logAction("UPDATE_BED", "BED", updated.getBedCode(), "Updated bed " + updated.getBedCode());
        return mapToResponse(updated);
    }

    @Transactional
    public BedResponse updateBedStatus(Long id, BedStatus status) {
        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found with ID: " + id));

        if (bed.getStatus() == BedStatus.OCCUPIED && status == BedStatus.AVAILABLE) {
            auditLogService.logAction("RELEASE_BED", "BED", bed.getBedCode(), "Bed released to AVAILABLE");
        } else {
            auditLogService.logAction("UPDATE_BED", "BED", bed.getBedCode(), "Changed bed status to " + status);
        }

        bed.setStatus(status);
        Bed updated = bedRepository.save(bed);
        return mapToResponse(updated);
    }

    public BedResponse mapToResponse(Bed bed) {
        String currentPatientName = null;
        String currentAdmissionId = null;

        if (bed.getStatus() == BedStatus.OCCUPIED) {
            List<AdmissionStatus> activeStatuses = Arrays.asList(
                    AdmissionStatus.REQUESTED, AdmissionStatus.APPROVED, AdmissionStatus.ADMITTED,
                    AdmissionStatus.ON_LEAVE, AdmissionStatus.DISCHARGE_PLANNED
            );
            List<IpdAdmission> admissions = ipdAdmissionRepository.findAll().stream()
                    .filter(a -> a.getBed() != null && a.getBed().getId().equals(bed.getId()) && activeStatuses.contains(a.getStatus()))
                    .collect(Collectors.toList());

            if (!admissions.isEmpty()) {
                IpdAdmission activeAdm = admissions.get(0);
                currentPatientName = activeAdm.getPatient().getFirstName() + " " + activeAdm.getPatient().getLastName();
                currentAdmissionId = activeAdm.getAdmissionId();
            }
        }

        return BedResponse.builder()
                .id(bed.getId())
                .bedCode(bed.getBedCode())
                .wardId(bed.getWard().getId())
                .wardCode(bed.getWard().getWardCode())
                .wardName(bed.getWard().getWardName())
                .bedNumber(bed.getBedNumber())
                .bedType(bed.getBedType())
                .status(bed.getStatus())
                .isActive(bed.getIsActive())
                .currentPatientName(currentPatientName)
                .currentAdmissionId(currentAdmissionId)
                .createdAt(bed.getCreatedAt())
                .updatedAt(bed.getUpdatedAt())
                .build();
    }
}
