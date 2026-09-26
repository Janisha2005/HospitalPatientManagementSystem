package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.MedicineCategoryDto;
import com.hospital.service.MedicineCategoryService;
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
@RequestMapping("/api/pharmacy/categories")
@RequiredArgsConstructor
public class MedicineCategoryController {

    private final MedicineCategoryService categoryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<Page<MedicineCategoryDto>>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "categoryName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Medicine categories retrieved successfully", categoryService.getAllCategories(pageable)));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<List<MedicineCategoryDto>>> getActiveCategories() {
        return ResponseEntity.ok(ApiResponse.success("Active medicine categories retrieved successfully", categoryService.getActiveCategories()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<MedicineCategoryDto>> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", categoryService.getCategoryById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<MedicineCategoryDto>> createCategory(@Valid @RequestBody MedicineCategoryDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", categoryService.createCategory(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<MedicineCategoryDto>> updateCategory(@PathVariable Long id, @Valid @RequestBody MedicineCategoryDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", categoryService.updateCategory(id, dto)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<MedicineCategoryDto>> updateStatus(@PathVariable Long id, @RequestParam Boolean isActive) {
        return ResponseEntity.ok(ApiResponse.success("Category status updated successfully", categoryService.updateStatus(id, isActive)));
    }
}
