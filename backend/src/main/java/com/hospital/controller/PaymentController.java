package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.PaymentDto;
import com.hospital.dto.PaymentRequest;
import com.hospital.service.PaymentService;
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

import java.util.List;

@RestController
@RequestMapping("/api/billing/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Page<PaymentDto>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", paymentService.getAllPayments(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", paymentService.getPaymentById(id)));
    }

    @GetMapping("/bill/{billId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getPaymentsByBillId(@PathVariable Long billId) {
        return ResponseEntity.ok(ApiResponse.success("Bill payments retrieved successfully", paymentService.getPaymentsByBillId(billId)));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<Page<PaymentDto>>> getPaymentsByPatientId(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success("Patient payments retrieved successfully", paymentService.getPaymentsByPatientId(patientId, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PaymentDto>> recordPayment(@Valid @RequestBody PaymentRequest request, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "BillingOfficer";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment recorded successfully", paymentService.recordPayment(request, username)));
    }

    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER')")
    public ResponseEntity<ApiResponse<PaymentDto>> reversePayment(@PathVariable Long id, @RequestParam(defaultValue = "Payment reversal requested") String reason, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "BillingOfficer";
        return ResponseEntity.ok(ApiResponse.success("Payment reversed successfully", paymentService.reversePayment(id, reason, username)));
    }
}
