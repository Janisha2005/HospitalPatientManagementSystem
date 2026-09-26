package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.PatientReturnRequest;
import com.hospital.dto.PharmacyReturnDto;
import com.hospital.dto.SupplierReturnRequest;
import com.hospital.service.PharmacyReturnService;
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
@RequestMapping("/api/pharmacy/returns")
@RequiredArgsConstructor
public class PharmacyReturnController {

    private final PharmacyReturnService returnService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<Page<PharmacyReturnDto>>> getAllReturns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Pharmacy returns retrieved successfully", returnService.getAllReturns(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<PharmacyReturnDto>> getReturnById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Return record retrieved successfully", returnService.getReturnById(id)));
    }

    @PostMapping("/patient")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<PharmacyReturnDto>> processPatientReturn(@Valid @RequestBody PatientReturnRequest request, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "Pharmacist";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Patient return processed successfully", returnService.processPatientReturn(request, username)));
    }

    @PostMapping("/supplier")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<PharmacyReturnDto>> processSupplierReturn(@Valid @RequestBody SupplierReturnRequest request, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "Pharmacist";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Supplier return processed successfully", returnService.processSupplierReturn(request, username)));
    }
}
