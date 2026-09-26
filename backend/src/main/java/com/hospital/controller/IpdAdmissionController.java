package com.hospital.controller;

import com.hospital.dto.*;
import com.hospital.entity.AdmissionStatus;
import com.hospital.entity.AdmissionType;
import com.hospital.entity.Patient;
import com.hospital.exception.ForbiddenException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.PatientRepository;
import com.hospital.security.UserPrincipal;
import com.hospital.service.IpdAdmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ipd/admissions")
@RequiredArgsConstructor
public class IpdAdmissionController {

    private final IpdAdmissionService ipdAdmissionService;
    private final PatientRepository patientRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<Page<IpdAdmissionResponse>>> getAllAdmissions(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long wardId,
            @RequestParam(required = false) Long bedId,
            @RequestParam(required = false) AdmissionStatus status,
            @RequestParam(required = false) AdmissionType admissionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if ("PATIENT".equals(currentUser.getRole())) {
            Patient patient = patientRepository.findByEmail(currentUser.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient record not found for user"));
            patientId = patient.getId();
        }

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<IpdAdmissionResponse> admissions = ipdAdmissionService.getAllAdmissions(
                patientId, doctorId, departmentId, wardId, bedId, status, admissionType, date, pageable
        );
        return ResponseEntity.ok(ApiResponse.success("Admissions retrieved successfully", admissions));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<IpdAdmissionResponse>> getAdmissionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        IpdAdmissionResponse admission = ipdAdmissionService.getAdmissionById(id);

        if ("PATIENT".equals(currentUser.getRole())) {
            Patient patient = patientRepository.findByEmail(currentUser.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient record not found"));
            if (!admission.getPatientId().equals(patient.getId())) {
                throw new ForbiddenException("You can only access your own IPD admission records");
            }
        }

        return ResponseEntity.ok(ApiResponse.success("Admission retrieved successfully", admission));
    }

    @GetMapping("/code/{admissionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<IpdAdmissionResponse>> getAdmissionByCode(
            @PathVariable String admissionId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        IpdAdmissionResponse admission = ipdAdmissionService.getAdmissionByCode(admissionId);

        if ("PATIENT".equals(currentUser.getRole())) {
            Patient patient = patientRepository.findByEmail(currentUser.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient record not found"));
            if (!admission.getPatientId().equals(patient.getId())) {
                throw new ForbiddenException("You can only access your own IPD admission records");
            }
        }

        return ResponseEntity.ok(ApiResponse.success("Admission retrieved successfully", admission));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<IpdAdmissionResponse>> createAdmission(
            @Valid @RequestBody IpdAdmissionCreateRequest request) {
        IpdAdmissionResponse created = ipdAdmissionService.createAdmissionRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("IPD Admission request created successfully", created));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<IpdAdmissionResponse>> approveAdmission(@PathVariable Long id) {
        IpdAdmissionResponse approved = ipdAdmissionService.approveAdmission(id);
        return ResponseEntity.ok(ApiResponse.success("Admission approved successfully", approved));
    }

    @PostMapping("/{id}/admit")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<IpdAdmissionResponse>> admitPatient(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        Long wardId = body.get("wardId");
        Long bedId = body.get("bedId");
        IpdAdmissionResponse admitted = ipdAdmissionService.admitPatient(id, wardId, bedId);
        return ResponseEntity.ok(ApiResponse.success("Patient admitted successfully", admitted));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<IpdAdmissionResponse>> cancelAdmission(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : "Admission cancelled";
        IpdAdmissionResponse cancelled = ipdAdmissionService.cancelAdmission(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Admission cancelled successfully", cancelled));
    }

    @PostMapping("/{admissionId}/allocate-bed")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BedAllocationResponse>> allocateBed(
            @PathVariable Long admissionId,
            @Valid @RequestBody BedAllocationRequest request) {
        BedAllocationResponse allocation = ipdAdmissionService.allocateBed(admissionId, request);
        return ResponseEntity.ok(ApiResponse.success("Bed allocated successfully", allocation));
    }

    @PostMapping("/{admissionId}/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<WardTransferResponse>> transferPatient(
            @PathVariable Long admissionId,
            @Valid @RequestBody WardTransferRequest request) {
        WardTransferResponse transfer = ipdAdmissionService.transferPatient(admissionId, request);
        return ResponseEntity.ok(ApiResponse.success("Patient transferred successfully", transfer));
    }

    @GetMapping("/{admissionId}/bed-history")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<BedAllocationResponse>>> getBedHistory(@PathVariable Long admissionId) {
        List<BedAllocationResponse> history = ipdAdmissionService.getBedHistory(admissionId);
        return ResponseEntity.ok(ApiResponse.success("Bed allocation history retrieved successfully", history));
    }

    @GetMapping("/{admissionId}/transfers")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<WardTransferResponse>>> getTransfers(@PathVariable Long admissionId) {
        List<WardTransferResponse> transfers = ipdAdmissionService.getTransferHistory(admissionId);
        return ResponseEntity.ok(ApiResponse.success("Transfer history retrieved successfully", transfers));
    }
}
