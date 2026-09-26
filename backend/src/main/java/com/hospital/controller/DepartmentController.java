package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.DepartmentRequestDto;
import com.hospital.dto.DepartmentResponseDto;
import com.hospital.security.UserPrincipal;
import com.hospital.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<Page<DepartmentResponseDto>>> getAllDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "departmentName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DepartmentResponseDto> departments = departmentService.getAllDepartments(pageable);
        return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully", departments));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<DepartmentResponseDto>>> getAllActiveDepartments() {
        List<DepartmentResponseDto> departments = departmentService.getAllActiveDepartments();
        return ResponseEntity.ok(ApiResponse.success("Active departments retrieved successfully", departments));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> getDepartmentById(@PathVariable Long id) {
        DepartmentResponseDto department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Department retrieved successfully", department));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> createDepartment(
            @Valid @RequestBody DepartmentRequestDto request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        DepartmentResponseDto created = departmentService.createDepartment(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Department created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequestDto request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        DepartmentResponseDto updated = departmentService.updateDepartment(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> updateDepartmentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> statusMap,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Boolean isActive = statusMap.getOrDefault("isActive", true);
        DepartmentResponseDto updated = departmentService.updateDepartmentStatus(id, isActive, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Department status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        departmentService.deleteDepartment(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Department deleted successfully"));
    }
}
