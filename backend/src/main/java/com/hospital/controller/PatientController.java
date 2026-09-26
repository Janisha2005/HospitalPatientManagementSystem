package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.PatientRequestDto;
import com.hospital.dto.PatientResponseDto;
import com.hospital.security.UserPrincipal;
import com.hospital.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<Page<PatientResponseDto>>> searchPatients(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PatientResponseDto> patients = patientService.searchPatients(query, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success("Patients retrieved successfully", patients));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PatientResponseDto>> getPatientById(@PathVariable Long id) {
        PatientResponseDto patient = patientService.getPatientById(id);
        return ResponseEntity.ok(ApiResponse.success("Patient retrieved successfully", patient));
    }

    @GetMapping("/code/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PatientResponseDto>> getPatientByPatientId(@PathVariable String patientId) {
        PatientResponseDto patient = patientService.getPatientByPatientId(patientId);
        return ResponseEntity.ok(ApiResponse.success("Patient retrieved successfully", patient));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PatientResponseDto>> createPatient(
            @Valid @RequestBody PatientRequestDto request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        PatientResponseDto created = patientService.createPatient(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Patient registered successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PatientResponseDto>> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequestDto request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        PatientResponseDto updated = patientService.updatePatient(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Patient updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PatientResponseDto>> updatePatientStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> statusMap,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Boolean isActive = statusMap.getOrDefault("isActive", true);
        PatientResponseDto updated = patientService.updatePatientStatus(id, isActive, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Patient status updated successfully", updated));
    }
}
