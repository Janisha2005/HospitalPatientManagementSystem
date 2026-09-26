package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.DispenseRequest;
import com.hospital.dto.PharmacyDispensingDto;
import com.hospital.service.DispensingService;
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
@RequestMapping("/api/pharmacy")
@RequiredArgsConstructor
public class DispensingController {

    private final DispensingService dispensingService;

    @GetMapping("/dispensing")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<Page<PharmacyDispensingDto>>> getAllDispensings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dispensedDatetime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Dispensing records retrieved successfully", dispensingService.getAllDispensings(pageable)));
    }

    @GetMapping("/dispensing/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<PharmacyDispensingDto>> getDispensingById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Dispensing record retrieved successfully", dispensingService.getDispensingById(id)));
    }

    @GetMapping("/prescriptions/{prescriptionId}/dispensing")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'DOCTOR', 'NURSE', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<PharmacyDispensingDto>>> getDispensingsByPrescription(@PathVariable Long prescriptionId) {
        return ResponseEntity.ok(ApiResponse.success("Prescription dispensing history retrieved successfully", dispensingService.getDispensingsByPrescriptionId(prescriptionId)));
    }

    @GetMapping("/patients/{patientId}/dispensing")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'DOCTOR', 'NURSE', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<PharmacyDispensingDto>>> getDispensingsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success("Patient dispensing history retrieved successfully", dispensingService.getDispensingsByPatientId(patientId)));
    }

    @PostMapping("/dispensing")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<List<PharmacyDispensingDto>>> dispenseMedicine(@Valid @RequestBody DispenseRequest request, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "Pharmacist";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Medicine dispensed successfully using FEFO batch selection", dispensingService.dispenseMedicine(request, username)));
    }
}
