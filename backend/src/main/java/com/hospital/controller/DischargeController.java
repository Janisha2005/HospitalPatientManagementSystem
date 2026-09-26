package com.hospital.controller;

import com.hospital.dto.*;
import com.hospital.service.DischargeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DischargeController {

    private final DischargeService dischargeService;

    @GetMapping("/api/ipd/admissions/{admissionId}/discharge-plan")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'PATIENT')")
    public ResponseEntity<ApiResponse<DischargePlanResponse>> getDischargePlan(@PathVariable Long admissionId) {
        DischargePlanResponse plan = dischargeService.getDischargePlan(admissionId);
        return ResponseEntity.ok(ApiResponse.success("Discharge plan retrieved successfully", plan));
    }

    @PostMapping("/api/ipd/admissions/{admissionId}/discharge-plan")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<DischargePlanResponse>> createDischargePlan(
            @PathVariable Long admissionId,
            @Valid @RequestBody DischargePlanRequest request) {
        DischargePlanResponse plan = dischargeService.createOrUpdateDischargePlan(admissionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Discharge plan saved successfully", plan));
    }

    @PutMapping("/api/ipd/discharge-plans/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<DischargePlanResponse>> updateDischargePlan(
            @PathVariable Long id,
            @Valid @RequestBody DischargePlanRequest request) {
        // Find admissionId via discharge plan or call createOrUpdate
        // Let's implement directly
        DischargePlanResponse plan = dischargeService.createOrUpdateDischargePlan(id, request);
        return ResponseEntity.ok(ApiResponse.success("Discharge plan updated successfully", plan));
    }

    @GetMapping("/api/ipd/admissions/{admissionId}/discharge-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'PATIENT')")
    public ResponseEntity<ApiResponse<DischargeSummaryResponse>> getDischargeSummary(@PathVariable Long admissionId) {
        DischargeSummaryResponse summary = dischargeService.getDischargeSummary(admissionId);
        return ResponseEntity.ok(ApiResponse.success("Discharge summary retrieved successfully", summary));
    }

    @PostMapping("/api/ipd/admissions/{admissionId}/discharge-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<DischargeSummaryResponse>> createDischargeSummary(
            @PathVariable Long admissionId,
            @Valid @RequestBody DischargeSummaryRequest request) {
        DischargeSummaryResponse summary = dischargeService.createOrUpdateDischargeSummary(admissionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Discharge summary saved successfully", summary));
    }

    @PutMapping("/api/ipd/discharge-summaries/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<DischargeSummaryResponse>> updateDischargeSummary(
            @PathVariable Long id,
            @Valid @RequestBody DischargeSummaryRequest request) {
        DischargeSummaryResponse summary = dischargeService.createOrUpdateDischargeSummary(id, request);
        return ResponseEntity.ok(ApiResponse.success("Discharge summary updated successfully", summary));
    }

    @PostMapping("/api/ipd/admissions/{admissionId}/discharge")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<IpdAdmissionResponse>> dischargePatient(
            @PathVariable Long admissionId,
            @Valid @RequestBody DischargeExecuteRequest request) {
        IpdAdmissionResponse response = dischargeService.dischargePatient(admissionId, request);
        return ResponseEntity.ok(ApiResponse.success("Patient discharged successfully and bed released", response));
    }
}
