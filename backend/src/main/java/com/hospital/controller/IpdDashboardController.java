package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.IpdDashboardDto;
import com.hospital.service.IpdDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ipd/dashboard")
@RequiredArgsConstructor
public class IpdDashboardController {

    private final IpdDashboardService ipdDashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<IpdDashboardDto>> getIpdDashboardMetrics() {
        IpdDashboardDto metrics = ipdDashboardService.getDashboardMetrics();
        return ResponseEntity.ok(ApiResponse.success("IPD dashboard metrics retrieved successfully", metrics));
    }
}
