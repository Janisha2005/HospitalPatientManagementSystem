package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.ChargeMasterDto;
import com.hospital.entity.ChargeCategory;
import com.hospital.service.ChargeMasterService;
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
@RequestMapping("/api/billing/charges")
@RequiredArgsConstructor
public class ChargeMasterController {

    private final ChargeMasterService chargeMasterService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<Page<ChargeMasterDto>>> getAllCharges(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "chargeName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ChargeMasterDto> result;
        if (query != null && !query.isBlank()) {
            result = chargeMasterService.searchCharges(query, pageable);
        } else {
            result = chargeMasterService.getAllCharges(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success("Charge master entries retrieved successfully", result));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<List<ChargeMasterDto>>> getActiveChargesByCategory(@PathVariable ChargeCategory category) {
        return ResponseEntity.ok(ApiResponse.success("Active charges retrieved successfully", chargeMasterService.getActiveChargesByCategory(category)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<ChargeMasterDto>> getChargeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Charge entry retrieved successfully", chargeMasterService.getChargeById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER')")
    public ResponseEntity<ApiResponse<ChargeMasterDto>> createCharge(@Valid @RequestBody ChargeMasterDto dto, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "BillingOfficer";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Charge entry created successfully", chargeMasterService.createCharge(dto, username)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER')")
    public ResponseEntity<ApiResponse<ChargeMasterDto>> updateCharge(@PathVariable Long id, @Valid @RequestBody ChargeMasterDto dto, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "BillingOfficer";
        return ResponseEntity.ok(ApiResponse.success("Charge entry updated successfully", chargeMasterService.updateCharge(id, dto, username)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLING_OFFICER')")
    public ResponseEntity<ApiResponse<ChargeMasterDto>> updateChargeStatus(@PathVariable Long id, @RequestParam boolean active, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "BillingOfficer";
        return ResponseEntity.ok(ApiResponse.success("Charge entry status updated successfully", chargeMasterService.updateChargeStatus(id, active, username)));
    }
}
