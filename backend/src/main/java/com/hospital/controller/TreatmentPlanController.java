package com.hospital.controller;

import com.hospital.dto.TreatmentPlanRequest;
import com.hospital.dto.TreatmentPlanResponse;
import com.hospital.service.TreatmentPlanService;
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
public class TreatmentPlanController {

    @Autowired
    private TreatmentPlanService treatmentPlanService;

    @GetMapping("/patients/{patientId}/treatment-plans")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or (hasRole('PATIENT') and @userSecurity.isPatient(#patientId))")
    public ResponseEntity<List<TreatmentPlanResponse>> getPatientTreatmentPlans(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<TreatmentPlanResponse> paged = treatmentPlanService.getPatientTreatmentPlans(patientId, PageRequest.of(page, size));
        return ResponseEntity.ok(paged.getContent());
    }

    @GetMapping("/opd/visits/{opdVisitId}/treatment-plans")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<List<TreatmentPlanResponse>> getVisitTreatmentPlans(@PathVariable Long opdVisitId) {
        return ResponseEntity.ok(treatmentPlanService.getOpdVisitTreatmentPlans(opdVisitId));
    }

    @PostMapping("/opd/visits/{opdVisitId}/treatment-plans")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<TreatmentPlanResponse> createTreatmentPlan(
            @PathVariable Long opdVisitId,
            @Valid @RequestBody TreatmentPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(treatmentPlanService.createTreatmentPlanForOpd(opdVisitId, request));
    }

    @PutMapping("/treatment-plans/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<TreatmentPlanResponse> updateTreatmentPlan(
            @PathVariable Long id,
            @Valid @RequestBody TreatmentPlanRequest request) {
        return ResponseEntity.ok(treatmentPlanService.updateTreatmentPlan(id, request));
    }
}
