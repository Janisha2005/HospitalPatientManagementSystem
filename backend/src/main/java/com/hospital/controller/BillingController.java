package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.BillDto;
import com.hospital.dto.BillItemDto;
import com.hospital.dto.DischargeClearanceDto;
import com.hospital.service.BillingService;
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
@RequestMapping("/api/billing/bills")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<Page<BillDto>>> getAllBills(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BillDto> result;
        if (query != null && !query.isBlank()) {
            result = billingService.searchBills(query, pageable);
        } else {
            result = billingService.getAllBills(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success("Bills retrieved successfully", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<BillDto>> getBillById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Bill retrieved successfully", billingService.getBillById(id)));
    }

    @GetMapping("/code/{billNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<BillDto>> getBillByNumber(@PathVariable String billNumber) {
        return ResponseEntity.ok(ApiResponse.success("Bill retrieved successfully", billingService.getBillByNumber(billNumber)));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<Page<BillDto>>> getBillsByPatientId(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success("Patient bills retrieved successfully", billingService.getBillsByPatientId(patientId, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BillDto>> createDraftBill(@Valid @RequestBody BillDto dto, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "BillingOfficer";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Draft bill created successfully", billingService.createDraftBill(dto, username)));
    }

    @PostMapping("/{billId}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BillDto>> addItemToBill(@PathVariable Long billId, @Valid @RequestBody BillItemDto itemDto, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "BillingOfficer";
        return ResponseEntity.ok(ApiResponse.success("Item added to bill successfully", billingService.addItemToBill(billId, itemDto, username)));
    }

    @PostMapping("/{billId}/finalize")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BillDto>> finalizeBill(@PathVariable Long billId, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "BillingOfficer";
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(ApiResponse.success("Bill finalized successfully", billingService.finalizeBill(billId, username, isAdmin)));
    }

    @PostMapping("/{billId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER')")
    public ResponseEntity<ApiResponse<BillDto>> cancelBill(@PathVariable Long billId, @RequestParam(defaultValue = "Cancelled by user") String reason, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "BillingOfficer";
        return ResponseEntity.ok(ApiResponse.success("Bill cancelled successfully", billingService.cancelBill(billId, reason, username)));
    }

    // Source Billing Endpoints

    @PostMapping("/opd/{opdVisitId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BillDto>> generateOpdBill(@PathVariable Long opdVisitId, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "BillingOfficer";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("OPD bill generated successfully", billingService.generateOpdBill(opdVisitId, username)));
    }

    @PostMapping("/pharmacy/{dispensingId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<BillDto>> generatePharmacyBill(@PathVariable Long dispensingId, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "Pharmacist";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pharmacy bill generated successfully", billingService.generatePharmacyBill(dispensingId, username)));
    }

    @PostMapping("/lab/{labOrderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER')")
    public ResponseEntity<ApiResponse<BillDto>> generateLabBill(@PathVariable Long labOrderId, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "BillingOfficer";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Laboratory bill generated successfully", billingService.generateLabBill(labOrderId, username)));
    }

    @PostMapping("/radiology/{radiologyOrderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER')")
    public ResponseEntity<ApiResponse<BillDto>> generateRadiologyBill(@PathVariable Long radiologyOrderId, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "BillingOfficer";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Radiology bill generated successfully", billingService.generateRadiologyBill(radiologyOrderId, username)));
    }

    @PostMapping("/ipd/{ipdAdmissionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER')")
    public ResponseEntity<ApiResponse<BillDto>> generateIpdBill(@PathVariable Long ipdAdmissionId, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "BillingOfficer";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("IPD final bill generated successfully", billingService.generateIpdBill(ipdAdmissionId, username)));
    }

    @PostMapping("/ipd/{ipdAdmissionId}/clearance")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER')")
    public ResponseEntity<ApiResponse<DischargeClearanceDto>> processDischargeClearance(@PathVariable Long ipdAdmissionId, @RequestParam(required = false) String remarks, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "BillingOfficer";
        return ResponseEntity.ok(ApiResponse.success("Discharge clearance processed successfully", billingService.processDischargeClearance(ipdAdmissionId, remarks, username)));
    }
}
