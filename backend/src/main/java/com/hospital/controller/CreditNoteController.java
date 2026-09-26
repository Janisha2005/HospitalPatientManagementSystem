package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.CreditNoteDto;
import com.hospital.dto.CreditNoteRequest;
import com.hospital.service.CreditNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing/credit-notes")
@RequiredArgsConstructor
public class CreditNoteController {

    private final CreditNoteService creditNoteService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER')")
    public ResponseEntity<ApiResponse<Page<CreditNoteDto>>> getAllCreditNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Credit notes retrieved successfully", creditNoteService.getAllCreditNotes(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'PATIENT')")
    public ResponseEntity<ApiResponse<CreditNoteDto>> getCreditNoteById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Credit note retrieved successfully", creditNoteService.getCreditNoteById(id)));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'PATIENT')")
    public ResponseEntity<ApiResponse<Page<CreditNoteDto>>> getCreditNotesByPatientId(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success("Patient credit notes retrieved successfully", creditNoteService.getCreditNotesByPatientId(patientId, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER')")
    public ResponseEntity<ApiResponse<CreditNoteDto>> createCreditNote(@Valid @RequestBody CreditNoteRequest request, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "BillingOfficer";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Credit note created successfully", creditNoteService.createCreditNote(request, username)));
    }
}
