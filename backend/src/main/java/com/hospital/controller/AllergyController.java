package com.hospital.controller;

import com.hospital.dto.AllergyRequest;
import com.hospital.dto.AllergyResponse;
import com.hospital.entity.AllergyStatus;
import com.hospital.service.AllergyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/{patientId}/allergies")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AllergyController {

    @Autowired
    private AllergyService allergyService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or (hasRole('PATIENT') and @userSecurity.isPatient(#patientId))")
    public ResponseEntity<List<AllergyResponse>> getPatientAllergies(@PathVariable Long patientId) {
        return ResponseEntity.ok(allergyService.getPatientAllergies(patientId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<AllergyResponse> addAllergy(
            @PathVariable Long patientId,
            @Valid @RequestBody AllergyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(allergyService.addAllergy(patientId, request));
    }

    @PutMapping("/{allergyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<AllergyResponse> updateAllergy(
            @PathVariable Long patientId,
            @PathVariable Long allergyId,
            @Valid @RequestBody AllergyRequest request) {
        return ResponseEntity.ok(allergyService.updateAllergy(patientId, allergyId, request));
    }

    @PatchMapping("/{allergyId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<AllergyResponse> toggleAllergyStatus(
            @PathVariable Long patientId,
            @PathVariable Long allergyId,
            @RequestParam(required = false, defaultValue = "INACTIVE") AllergyStatus status) {
        return ResponseEntity.ok(allergyService.updateStatus(patientId, allergyId, status));
    }
}
