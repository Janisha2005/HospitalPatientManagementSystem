package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.InventoryDashboardDto;
import com.hospital.dto.PharmacyDashboardDto;
import com.hospital.service.PharmacyDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pharmacy")
@RequiredArgsConstructor
public class PharmacyDashboardController {

    private final PharmacyDashboardService dashboardService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PharmacyDashboardDto>> getPharmacyDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Pharmacy dashboard metrics retrieved successfully", dashboardService.getPharmacyDashboard()));
    }

    @GetMapping("/inventory/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<InventoryDashboardDto>> getInventoryDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Inventory dashboard metrics retrieved successfully", dashboardService.getInventoryDashboard()));
    }
}
