package com.hospital.controller;

import com.hospital.dto.*;
import com.hospital.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST', 'DOCTOR', 'NURSE', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<ExecutiveAnalyticsDto>> getExecutiveAnalytics(
            @RequestParam(defaultValue = "TODAY") String period,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(ApiResponse.success("Executive analytics retrieved successfully",
                analyticsService.getExecutiveAnalytics(period, fromDate, toDate)));
    }

    @GetMapping("/opd")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<OPDAnalyticsDto>> getOPDAnalytics(
            @RequestParam(defaultValue = "TODAY") String period,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(ApiResponse.success("OPD analytics retrieved successfully",
                analyticsService.getOPDAnalytics(period, fromDate, toDate)));
    }

    @GetMapping("/ipd")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<IPDAnalyticsDto>> getIPDAnalytics(
            @RequestParam(defaultValue = "TODAY") String period,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(ApiResponse.success("IPD analytics retrieved successfully",
                analyticsService.getIPDAnalytics(period, fromDate, toDate)));
    }

    @GetMapping("/beds")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<BedOccupancyReportDto>> getBedOccupancyReport() {
        return ResponseEntity.ok(ApiResponse.success("Bed occupancy report retrieved successfully",
                analyticsService.getBedOccupancyReport()));
    }

    @GetMapping("/pharmacy")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'PHARMACIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PharmacyAnalyticsDto>> getPharmacyAnalytics(
            @RequestParam(defaultValue = "TODAY") String period,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(ApiResponse.success("Pharmacy analytics retrieved successfully",
                analyticsService.getPharmacyAnalytics(period, fromDate, toDate)));
    }

    @GetMapping("/laboratory")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<LaboratoryAnalyticsDto>> getLaboratoryAnalytics(
            @RequestParam(defaultValue = "TODAY") String period,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(ApiResponse.success("Laboratory analytics retrieved successfully",
                analyticsService.getLaboratoryAnalytics(period, fromDate, toDate)));
    }

    @GetMapping("/radiology")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<RadiologyAnalyticsDto>> getRadiologyAnalytics(
            @RequestParam(defaultValue = "TODAY") String period,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(ApiResponse.success("Radiology analytics retrieved successfully",
                analyticsService.getRadiologyAnalytics(period, fromDate, toDate)));
    }

    @GetMapping("/patients")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PatientAnalyticsDto>> getPatientAnalytics(
            @RequestParam(defaultValue = "TODAY") String period,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(ApiResponse.success("Patient analytics retrieved successfully",
                analyticsService.getPatientAnalytics(period, fromDate, toDate)));
    }

    @GetMapping("/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PaymentCollectionReportDto>> getPaymentCollectionReport(
            @RequestParam(defaultValue = "TODAY") String period,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(ApiResponse.success("Payment collection report retrieved successfully",
                analyticsService.getPaymentCollectionReport(period, fromDate, toDate)));
    }

    @GetMapping("/outstanding")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<OutstandingReportDto>> getOutstandingReport() {
        return ResponseEntity.ok(ApiResponse.success("Outstanding report retrieved successfully",
                analyticsService.getOutstandingReport()));
    }
}
