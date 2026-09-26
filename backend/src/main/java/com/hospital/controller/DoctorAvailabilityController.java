package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.DoctorAvailabilityRequest;
import com.hospital.dto.DoctorAvailabilityResponse;
import com.hospital.dto.TimeSlotDto;
import com.hospital.service.DoctorAvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService availabilityService;

    @GetMapping("/{doctorId}/availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE')")
    public ResponseEntity<ApiResponse<List<DoctorAvailabilityResponse>>> getDoctorAvailability(@PathVariable Long doctorId) {
        List<DoctorAvailabilityResponse> availability = availabilityService.getDoctorAvailability(doctorId);
        return ResponseEntity.ok(ApiResponse.success("Doctor availability schedule retrieved successfully", availability));
    }

    @PostMapping("/{doctorId}/availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<DoctorAvailabilityResponse>> createAvailability(
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorAvailabilityRequest request) {
        DoctorAvailabilityResponse created = availabilityService.createAvailability(doctorId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Doctor availability created successfully", created));
    }

    @PutMapping("/{doctorId}/availability/{availabilityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<DoctorAvailabilityResponse>> updateAvailability(
            @PathVariable Long doctorId,
            @PathVariable Long availabilityId,
            @Valid @RequestBody DoctorAvailabilityRequest request) {
        DoctorAvailabilityResponse updated = availabilityService.updateAvailability(doctorId, availabilityId, request);
        return ResponseEntity.ok(ApiResponse.success("Doctor availability updated successfully", updated));
    }

    @DeleteMapping("/{doctorId}/availability/{availabilityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<Void>> deleteAvailability(
            @PathVariable Long doctorId,
            @PathVariable Long availabilityId) {
        availabilityService.deleteAvailability(doctorId, availabilityId);
        return ResponseEntity.ok(ApiResponse.success("Doctor availability schedule deleted successfully", null));
    }

    @GetMapping("/{doctorId}/available-slots")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<TimeSlotDto>>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TimeSlotDto> slots = availabilityService.getAvailableSlots(doctorId, date);
        return ResponseEntity.ok(ApiResponse.success("Available appointment slots calculated successfully", slots));
    }
}
