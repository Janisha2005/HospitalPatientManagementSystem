package com.hospital.controller;

import com.hospital.dto.*;
import com.hospital.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/daily-collection")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PaymentCollectionReportDto>> getDailyCollectionReport(
            @RequestParam(defaultValue = "TODAY") String period,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(ApiResponse.success("Daily collection report retrieved successfully",
                analyticsService.getPaymentCollectionReport(period, fromDate, toDate)));
    }

    @GetMapping("/outstanding")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<OutstandingReportDto>> getOutstandingReport() {
        return ResponseEntity.ok(ApiResponse.success("Outstanding report retrieved successfully",
                analyticsService.getOutstandingReport()));
    }

    @GetMapping("/bed-occupancy")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<BedOccupancyReportDto>> getBedOccupancyReport() {
        return ResponseEntity.ok(ApiResponse.success("Bed occupancy report retrieved successfully",
                analyticsService.getBedOccupancyReport()));
    }

    @GetMapping("/operational")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<OperationalReportDto>> getOperationalReport(
            @RequestParam(defaultValue = "TODAY") String period,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(ApiResponse.success("Operational report retrieved successfully",
                analyticsService.getOperationalReport(period, fromDate, toDate)));
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST')")
    public ResponseEntity<String> exportCSVReport(
            @RequestParam(defaultValue = "OPERATIONAL") String reportType,
            @RequestParam(defaultValue = "TODAY") String period,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {

        String csvContent = analyticsService.generateCSVReport(reportType, period, fromDate, toDate);
        String filename = reportType.toLowerCase() + "_report.csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvContent);
    }
}
