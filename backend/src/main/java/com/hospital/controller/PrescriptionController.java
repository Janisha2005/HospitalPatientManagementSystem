package com.hospital.controller;

import com.hospital.dto.PrescriptionCreateRequest;
import com.hospital.dto.PrescriptionResponse;
import com.hospital.entity.PrescriptionStatus;
import com.hospital.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @GetMapping("/prescriptions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<List<PrescriptionResponse>> getAllPrescriptions(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) PrescriptionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<PrescriptionResponse> paged = prescriptionService.getPrescriptions(patientId, doctorId, date, status, PageRequest.of(page, size));
        return ResponseEntity.ok(paged.getContent());
    }

    @GetMapping("/patients/{patientId}/prescriptions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or (hasRole('PATIENT') and @userSecurity.isPatient(#patientId))")
    public ResponseEntity<List<PrescriptionResponse>> getPatientPrescriptions(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<PrescriptionResponse> paged = prescriptionService.getPrescriptions(patientId, null, null, null, PageRequest.of(page, size));
        return ResponseEntity.ok(paged.getContent());
    }

    @GetMapping("/prescriptions/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('PATIENT')")
    public ResponseEntity<PrescriptionResponse> getPrescriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionById(id));
    }

    @PostMapping("/prescriptions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionResponse> createPrescription(@Valid @RequestBody PrescriptionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prescriptionService.createPrescription(request));
    }

    @PutMapping("/prescriptions/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionResponse> updatePrescription(
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionCreateRequest request) {
        return ResponseEntity.ok(prescriptionService.updatePrescription(id, request));
    }

    @PutMapping("/prescriptions/{id}/issue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionResponse> issuePrescription(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.issuePrescription(id));
    }

    @PutMapping("/prescriptions/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionResponse> cancelPrescription(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.cancelPrescription(id));
    }
}
