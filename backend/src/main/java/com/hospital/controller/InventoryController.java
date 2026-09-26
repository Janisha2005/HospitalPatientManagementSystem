package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.InventoryTransactionDto;
import com.hospital.dto.MedicineResponse;
import com.hospital.dto.StockAdjustmentDto;
import com.hospital.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacy/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<Page<MedicineResponse>>> getInventory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "medicineName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Inventory retrieved successfully", inventoryService.getInventory(pageable)));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getLowStockInventory() {
        return ResponseEntity.ok(ApiResponse.success("Low stock inventory retrieved successfully", inventoryService.getLowStockInventory()));
    }

    @GetMapping("/{medicineId}/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<Page<InventoryTransactionDto>>> getTransactionsForMedicine(
            @PathVariable Long medicineId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Medicine inventory transactions retrieved successfully", inventoryService.getTransactionsForMedicine(medicineId, pageable)));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<InventoryTransactionDto>> adjustStock(@Valid @RequestBody StockAdjustmentDto dto, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "Pharmacist";
        return ResponseEntity.ok(ApiResponse.success("Stock adjustment completed successfully", inventoryService.adjustStock(dto, username)));
    }
}
