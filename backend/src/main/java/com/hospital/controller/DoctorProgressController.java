package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.InpatientProgressNoteRequest;
import com.hospital.dto.InpatientProgressNoteResponse;
import com.hospital.service.DoctorProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DoctorProgressController {

    private final DoctorProgressService doctorProgressService;

    @GetMapping("/api/ipd/admissions/{admissionId}/progress-notes")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<InpatientProgressNoteResponse>>> getProgressNotesForAdmission(
            @PathVariable Long admissionId) {
        List<InpatientProgressNoteResponse> notes = doctorProgressService.getProgressNotesForAdmission(admissionId);
        return ResponseEntity.ok(ApiResponse.success("Doctor progress notes retrieved successfully", notes));
    }

    @PostMapping("/api/ipd/admissions/{admissionId}/progress-notes")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<InpatientProgressNoteResponse>> createProgressNote(
            @PathVariable Long admissionId,
            @Valid @RequestBody InpatientProgressNoteRequest request) {
        InpatientProgressNoteResponse created = doctorProgressService.createProgressNote(admissionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Doctor progress note recorded successfully", created));
    }

    @PutMapping("/api/ipd/progress-notes/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<InpatientProgressNoteResponse>> updateProgressNote(
            @PathVariable Long id,
            @Valid @RequestBody InpatientProgressNoteRequest request) {
        InpatientProgressNoteResponse updated = doctorProgressService.updateProgressNote(id, request);
        return ResponseEntity.ok(ApiResponse.success("Doctor progress note updated successfully", updated));
    }
}
