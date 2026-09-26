package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.MedicineBatchDto;
import com.hospital.entity.BatchStatus;
import com.hospital.service.BatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacy/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<Page<MedicineBatchDto>>> getAllBatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "expiryDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Batches retrieved successfully", batchService.getAllBatches(pageable)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<Page<MedicineBatchDto>>> searchBatches(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("expiryDate").ascending());
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved successfully", batchService.searchBatches(query, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<MedicineBatchDto>> getBatchById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Batch retrieved successfully", batchService.getBatchById(id)));
    }

    @GetMapping("/medicine/{medicineId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<List<MedicineBatchDto>>> getBatchesByMedicine(@PathVariable Long medicineId) {
        return ResponseEntity.ok(ApiResponse.success("Medicine batches retrieved successfully", batchService.getBatchesByMedicineId(medicineId)));
    }

    @GetMapping("/near-expiry")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<List<MedicineBatchDto>>> getNearExpiryBatches() {
        return ResponseEntity.ok(ApiResponse.success("Near-expiry batches retrieved successfully", batchService.getNearExpiryBatches()));
    }

    @GetMapping("/expired")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<List<MedicineBatchDto>>> getExpiredBatches() {
        return ResponseEntity.ok(ApiResponse.success("Expired batches retrieved successfully", batchService.getExpiredBatches()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<MedicineBatchDto>> createBatch(@Valid @RequestBody MedicineBatchDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Batch created successfully", batchService.createBatch(dto)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<MedicineBatchDto>> updateBatchStatus(@PathVariable Long id, @RequestParam BatchStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Batch status updated successfully", batchService.updateBatchStatus(id, status)));
    }
}
