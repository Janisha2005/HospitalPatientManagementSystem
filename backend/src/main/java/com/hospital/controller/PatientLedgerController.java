package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.PatientLedgerEntryDto;
import com.hospital.service.PatientLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing/patients")
@RequiredArgsConstructor
public class PatientLedgerController {

    private final PatientLedgerService ledgerService;

    @GetMapping("/{patientId}/ledger")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<PatientLedgerEntryDto>>> getLedgerForPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success("Patient financial ledger retrieved successfully", ledgerService.getLedgerForPatient(patientId)));
    }
}
