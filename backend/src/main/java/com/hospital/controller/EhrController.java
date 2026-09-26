package com.hospital.controller;

import com.hospital.dto.EhrRecordRequest;
import com.hospital.dto.EhrRecordResponse;
import com.hospital.dto.ClinicalTimelineResponse;
import com.hospital.service.EhrService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ehr")
@CrossOrigin(origins = "*", maxAge = 3600)
public class EhrController {

    @Autowired
    private EhrService ehrService;

    @GetMapping("/patients/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or (hasRole('PATIENT') and @userSecurity.isPatient(#patientId))")
    public ResponseEntity<List<EhrRecordResponse>> getPatientEhrRecords(
            @PathVariable Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<EhrRecordResponse> paged = ehrService.getRecords(patientId, doctorId, departmentId, PageRequest.of(page, size));
        return ResponseEntity.ok(paged.getContent());
    }

    @GetMapping("/patients/{patientId}/timeline")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or (hasRole('PATIENT') and @userSecurity.isPatient(#patientId))")
    public ResponseEntity<ClinicalTimelineResponse> getPatientClinicalTimeline(@PathVariable Long patientId) {
        return ResponseEntity.ok(ehrService.getPatientTimeline(patientId));
    }

    @PostMapping("/records")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<EhrRecordResponse> createEhrRecord(@Valid @RequestBody EhrRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ehrService.createRecord(request));
    }
}
