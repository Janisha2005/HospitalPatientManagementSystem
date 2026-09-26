package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.WardRequest;
import com.hospital.dto.WardResponse;
import com.hospital.service.WardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wards")
@RequiredArgsConstructor
public class WardController {

    private final WardService wardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<WardResponse>>> getAllWards(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean activeOnly) {
        List<WardResponse> wards = wardService.getAllWards(departmentId, activeOnly);
        return ResponseEntity.ok(ApiResponse.success("Wards retrieved successfully", wards));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<WardResponse>> getWardById(@PathVariable Long id) {
        WardResponse ward = wardService.getWardById(id);
        return ResponseEntity.ok(ApiResponse.success("Ward retrieved successfully", ward));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WardResponse>> createWard(@Valid @RequestBody WardRequest request) {
        WardResponse created = wardService.createWard(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ward created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WardResponse>> updateWard(
            @PathVariable Long id,
            @Valid @RequestBody WardRequest request) {
        WardResponse updated = wardService.updateWard(id, request);
        return ResponseEntity.ok(ApiResponse.success("Ward updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WardResponse>> updateWardStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        Boolean active = body.getOrDefault("isActive", true);
        WardResponse updated = wardService.updateWardStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Ward status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteWard(@PathVariable Long id) {
        wardService.deleteWard(id);
        return ResponseEntity.ok(ApiResponse.success("Ward deleted successfully"));
    }
}
