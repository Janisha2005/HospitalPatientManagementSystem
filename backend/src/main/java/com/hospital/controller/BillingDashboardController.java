package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.BillingDashboardDto;
import com.hospital.dto.RevenueReportDto;
import com.hospital.service.BillingDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingDashboardController {

    private final BillingDashboardService dashboardService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BillingDashboardDto>> getDashboardMetrics() {
        return ResponseEntity.ok(ApiResponse.success("Billing dashboard metrics retrieved successfully", dashboardService.getDashboardMetrics()));
    }

    @GetMapping("/reports/revenue")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER')")
    public ResponseEntity<ApiResponse<RevenueReportDto>> getRevenueReport(@RequestParam(defaultValue = "TODAY") String period) {
        return ResponseEntity.ok(ApiResponse.success("Revenue report retrieved successfully", dashboardService.getRevenueReport(period)));
    }
}
