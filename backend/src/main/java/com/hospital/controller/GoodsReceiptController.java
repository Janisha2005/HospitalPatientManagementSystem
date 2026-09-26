package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.GoodsReceiptDto;
import com.hospital.service.GoodsReceiptService;
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
@RequestMapping("/api/pharmacy/goods-receipts")
@RequiredArgsConstructor
public class GoodsReceiptController {

    private final GoodsReceiptService goodsReceiptService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<Page<GoodsReceiptDto>>> getAllGoodsReceipts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Goods receipts retrieved successfully", goodsReceiptService.getAllGoodsReceipts(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<GoodsReceiptDto>> getGoodsReceiptById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Goods receipt retrieved successfully", goodsReceiptService.getGoodsReceiptById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<GoodsReceiptDto>> createGoodsReceipt(@Valid @RequestBody GoodsReceiptDto dto, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "Pharmacist";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Goods receipt created successfully and stock updated", goodsReceiptService.createGoodsReceipt(dto, username)));
    }
}
