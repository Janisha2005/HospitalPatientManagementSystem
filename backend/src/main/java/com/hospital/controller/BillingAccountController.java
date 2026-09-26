package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.BillingAccountDto;
import com.hospital.service.BillingAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing/accounts")
@RequiredArgsConstructor
public class BillingAccountController {

    private final BillingAccountService accountService;

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<BillingAccountDto>> getAccountByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success("Billing account retrieved successfully", accountService.getAccountByPatientId(patientId)));
    }
}
