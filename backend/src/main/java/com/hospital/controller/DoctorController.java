package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.DoctorRequestDto;
import com.hospital.dto.DoctorResponseDto;
import com.hospital.security.UserPrincipal;
import com.hospital.service.DoctorService;
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
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<Page<DoctorResponseDto>>> searchDoctors(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DoctorResponseDto> doctors = doctorService.searchDoctors(query, departmentId, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success("Doctors retrieved successfully", doctors));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<DoctorResponseDto>> getDoctorById(@PathVariable Long id) {
        DoctorResponseDto doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(ApiResponse.success("Doctor retrieved successfully", doctor));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponseDto>> createDoctor(
            @Valid @RequestBody DoctorRequestDto request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        DoctorResponseDto created = doctorService.createDoctor(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Doctor registered successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponseDto>> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorRequestDto request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        DoctorResponseDto updated = doctorService.updateDoctor(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Doctor updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponseDto>> updateDoctorStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> statusMap,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Boolean isActive = statusMap.getOrDefault("isActive", true);
        DoctorResponseDto updated = doctorService.updateDoctorStatus(id, isActive, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Doctor status updated successfully", updated));
    }
}
