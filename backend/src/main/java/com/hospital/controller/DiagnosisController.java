package com.hospital.controller;

import com.hospital.dto.DiagnosisRequest;
import com.hospital.dto.DiagnosisResponse;
import com.hospital.service.DiagnosisService;
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
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DiagnosisController {

    @Autowired
    private DiagnosisService diagnosisService;

    @GetMapping("/patients/{patientId}/diagnoses")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or (hasRole('PATIENT') and @userSecurity.isPatient(#patientId))")
    public ResponseEntity<List<DiagnosisResponse>> getPatientDiagnoses(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<DiagnosisResponse> paged = diagnosisService.getPatientDiagnoses(patientId, PageRequest.of(page, size));
        return ResponseEntity.ok(paged.getContent());
    }

    @GetMapping("/opd/visits/{opdVisitId}/diagnoses")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<List<DiagnosisResponse>> getVisitDiagnoses(@PathVariable Long opdVisitId) {
        return ResponseEntity.ok(diagnosisService.getOpdVisitDiagnoses(opdVisitId));
    }

    @PostMapping("/opd/visits/{opdVisitId}/diagnoses")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<DiagnosisResponse> createDiagnosis(
            @PathVariable Long opdVisitId,
            @Valid @RequestBody DiagnosisRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(diagnosisService.createDiagnosisForOpd(opdVisitId, request));
    }
}
