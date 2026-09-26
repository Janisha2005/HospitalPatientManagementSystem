package com.hospital.controller;

import com.hospital.dto.ConditionRequest;
import com.hospital.dto.ConditionResponse;
import com.hospital.entity.ConditionStatus;
import com.hospital.service.ConditionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/{patientId}/conditions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ConditionController {

    @Autowired
    private ConditionService conditionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or (hasRole('PATIENT') and @userSecurity.isPatient(#patientId))")
    public ResponseEntity<List<ConditionResponse>> getPatientConditions(@PathVariable Long patientId) {
        return ResponseEntity.ok(conditionService.getPatientConditions(patientId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<ConditionResponse> addCondition(
            @PathVariable Long patientId,
            @Valid @RequestBody ConditionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(conditionService.addCondition(patientId, request));
    }

    @PutMapping("/{conditionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<ConditionResponse> updateCondition(
            @PathVariable Long patientId,
            @PathVariable Long conditionId,
            @Valid @RequestBody ConditionRequest request) {
        return ResponseEntity.ok(conditionService.updateCondition(patientId, conditionId, request));
    }

    @PatchMapping("/{conditionId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<ConditionResponse> toggleConditionStatus(
            @PathVariable Long patientId,
            @PathVariable Long conditionId,
            @RequestParam(required = false, defaultValue = "RESOLVED") ConditionStatus status) {
        return ResponseEntity.ok(conditionService.updateStatus(patientId, conditionId, status));
    }
}
