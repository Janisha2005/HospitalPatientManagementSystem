package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.RefundDto;
import com.hospital.dto.RefundRequest;
import com.hospital.service.RefundService;
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
@RequestMapping("/api/billing/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER')")
    public ResponseEntity<ApiResponse<Page<RefundDto>>> getAllRefunds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Refunds retrieved successfully", refundService.getAllRefunds(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'PATIENT')")
    public ResponseEntity<ApiResponse<RefundDto>> getRefundById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Refund record retrieved successfully", refundService.getRefundById(id)));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'PATIENT')")
    public ResponseEntity<ApiResponse<Page<RefundDto>>> getRefundsByPatientId(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success("Patient refunds retrieved successfully", refundService.getRefundsByPatientId(patientId, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER')")
    public ResponseEntity<ApiResponse<RefundDto>> processRefund(@Valid @RequestBody RefundRequest request, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "BillingOfficer";
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Refund processed successfully", refundService.processRefund(request, username, isAdmin)));
    }
}
