package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.BedRequest;
import com.hospital.dto.BedResponse;
import com.hospital.entity.BedStatus;
import com.hospital.entity.BedType;
import com.hospital.service.BedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BedController {

    private final BedService bedService;

    @GetMapping("/api/beds")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<BedResponse>>> getAllBeds(
            @RequestParam(required = false) Long wardId,
            @RequestParam(required = false) BedType bedType,
            @RequestParam(required = false) BedStatus status) {
        List<BedResponse> beds = bedService.getAllBeds(wardId, bedType, status);
        return ResponseEntity.ok(ApiResponse.success("Beds retrieved successfully", beds));
    }

    @GetMapping("/api/beds/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<BedResponse>>> getAvailableBeds(
            @RequestParam(required = false) Long wardId,
            @RequestParam(required = false) BedType bedType) {
        List<BedResponse> beds = bedService.getAvailableBeds(wardId, bedType);
        return ResponseEntity.ok(ApiResponse.success("Available beds retrieved successfully", beds));
    }

    @GetMapping("/api/wards/{wardId}/beds")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<BedResponse>>> getBedsByWard(@PathVariable Long wardId) {
        List<BedResponse> beds = bedService.getAllBeds(wardId, null, null);
        return ResponseEntity.ok(ApiResponse.success("Ward beds retrieved successfully", beds));
    }

    @GetMapping("/api/beds/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BedResponse>> getBedById(@PathVariable Long id) {
        BedResponse bed = bedService.getBedById(id);
        return ResponseEntity.ok(ApiResponse.success("Bed retrieved successfully", bed));
    }

    @PostMapping("/api/beds")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BedResponse>> createBed(@Valid @RequestBody BedRequest request) {
        BedResponse created = bedService.createBed(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bed created successfully", created));
    }

    @PutMapping("/api/beds/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BedResponse>> updateBed(
            @PathVariable Long id,
            @Valid @RequestBody BedRequest request) {
        BedResponse updated = bedService.updateBed(id, request);
        return ResponseEntity.ok(ApiResponse.success("Bed updated successfully", updated));
    }

    @PatchMapping("/api/beds/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'DOCTOR')")
    public ResponseEntity<ApiResponse<BedResponse>> updateBedStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        BedStatus status = BedStatus.valueOf(statusStr);
        BedResponse updated = bedService.updateBedStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Bed status updated successfully", updated));
    }
}
