package com.hospital.controller;

import com.hospital.dto.*;
import com.hospital.service.NursingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NursingController {

    private final NursingService nursingService;

    @GetMapping("/api/ipd/nursing/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'DOCTOR')")
    public ResponseEntity<ApiResponse<NursingDashboardDto>> getNursingDashboard() {
        NursingDashboardDto dashboard = nursingService.getNursingDashboard();
        return ResponseEntity.ok(ApiResponse.success("Nursing dashboard metrics retrieved successfully", dashboard));
    }

    @GetMapping("/api/ipd/admissions/{admissionId}/vitals")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<InpatientVitalResponse>>> getVitalsForAdmission(@PathVariable Long admissionId) {
        List<InpatientVitalResponse> vitals = nursingService.getVitalsForAdmission(admissionId);
        return ResponseEntity.ok(ApiResponse.success("Inpatient vitals retrieved successfully", vitals));
    }

    @PostMapping("/api/ipd/admissions/{admissionId}/vitals")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<InpatientVitalResponse>> recordVitals(
            @PathVariable Long admissionId,
            @Valid @RequestBody InpatientVitalRequest request) {
        InpatientVitalResponse recorded = nursingService.recordVitals(admissionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inpatient vitals recorded successfully", recorded));
    }

    @GetMapping("/api/ipd/admissions/{admissionId}/nursing-notes")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<List<NursingNoteResponse>>> getNursingNotesForAdmission(@PathVariable Long admissionId) {
        List<NursingNoteResponse> notes = nursingService.getNursingNotesForAdmission(admissionId);
        return ResponseEntity.ok(ApiResponse.success("Nursing notes retrieved successfully", notes));
    }

    @PostMapping("/api/ipd/admissions/{admissionId}/nursing-notes")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'DOCTOR')")
    public ResponseEntity<ApiResponse<NursingNoteResponse>> createNursingNote(
            @PathVariable Long admissionId,
            @Valid @RequestBody NursingNoteRequest request) {
        NursingNoteResponse created = nursingService.createNursingNote(admissionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Nursing note created successfully", created));
    }

    @PutMapping("/api/ipd/nursing-notes/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'DOCTOR')")
    public ResponseEntity<ApiResponse<NursingNoteResponse>> updateNursingNote(
            @PathVariable Long id,
            @Valid @RequestBody NursingNoteRequest request) {
        NursingNoteResponse updated = nursingService.updateNursingNote(id, request);
        return ResponseEntity.ok(ApiResponse.success("Nursing note updated successfully", updated));
    }
}
