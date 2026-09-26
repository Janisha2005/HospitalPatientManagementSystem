package com.hospital.controller;

import com.hospital.dto.*;
import com.hospital.entity.OpdVisitStatus;
import com.hospital.service.OpdService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/opd")
@RequiredArgsConstructor
public class OpdController {

    private final OpdService opdService;

    @GetMapping("/visits")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE', 'PATIENT')")
    public ResponseEntity<ApiResponse<Page<OpdVisitResponse>>> getVisits(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate,
            @RequestParam(required = false) OpdVisitStatus visitStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "visitDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<OpdVisitResponse> result = opdService.getVisits(
                doctorId, patientId, departmentId, visitDate, visitStatus, PageRequest.of(page, size, sort)
        );
        return ResponseEntity.ok(ApiResponse.success("OPD visits retrieved successfully", result));
    }

    @GetMapping("/visits/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE', 'PATIENT')")
    public ResponseEntity<ApiResponse<OpdVisitResponse>> getVisitById(@PathVariable Long id) {
        OpdVisitResponse visit = opdService.getVisitById(id);
        return ResponseEntity.ok(ApiResponse.success("OPD visit details retrieved successfully", visit));
    }

    @GetMapping("/visits/code/{opdVisitId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE', 'PATIENT')")
    public ResponseEntity<ApiResponse<OpdVisitResponse>> getVisitByCode(@PathVariable String opdVisitId) {
        OpdVisitResponse visit = opdService.getVisitByCode(opdVisitId);
        return ResponseEntity.ok(ApiResponse.success("OPD visit details retrieved successfully", visit));
    }

    @PutMapping("/visits/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<OpdVisitResponse>> updateVisitDetails(
            @PathVariable Long id,
            @Valid @RequestBody OpdVisitUpdateRequest request) {
        OpdVisitResponse updated = opdService.updateVisitDetails(id, request);
        return ResponseEntity.ok(ApiResponse.success("OPD visit updated successfully", updated));
    }

    @PatchMapping("/visits/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<OpdVisitResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OpdStatusRequest request) {
        OpdVisitUpdateRequest updateReq = OpdVisitUpdateRequest.builder()
                .visitStatus(request.getStatus())
                .build();
        OpdVisitResponse updated = opdService.updateVisitDetails(id, updateReq);
        return ResponseEntity.ok(ApiResponse.success("OPD visit status updated successfully", updated));
    }

    @GetMapping("/queue")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE')")
    public ResponseEntity<ApiResponse<List<OpdVisitResponse>>> getQueue(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long departmentId) {
        List<OpdVisitResponse> queue = opdService.getTodayQueue(doctorId, departmentId);
        return ResponseEntity.ok(ApiResponse.success("OPD queue retrieved successfully", queue));
    }

    @GetMapping("/queue/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE')")
    public ResponseEntity<ApiResponse<List<OpdVisitResponse>>> getTodayQueue(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long departmentId) {
        List<OpdVisitResponse> queue = opdService.getTodayQueue(doctorId, departmentId);
        return ResponseEntity.ok(ApiResponse.success("Today's OPD queue retrieved successfully", queue));
    }

    @PostMapping("/queue/{id}/call")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<OpdVisitResponse>> callQueuePatient(@PathVariable Long id) {
        OpdVisitResponse visit = opdService.callQueuePatient(id);
        return ResponseEntity.ok(ApiResponse.success("Patient called successfully", visit));
    }

    @PostMapping("/queue/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<OpdVisitResponse>> startConsultation(@PathVariable Long id) {
        OpdVisitResponse visit = opdService.startConsultation(id);
        return ResponseEntity.ok(ApiResponse.success("Consultation started successfully", visit));
    }

    @PostMapping("/queue/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<OpdVisitResponse>> completeConsultation(
            @PathVariable Long id,
            @RequestBody(required = false) OpdVisitUpdateRequest request) {
        OpdVisitResponse visit = opdService.completeConsultation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Consultation completed successfully", visit));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE')")
    public ResponseEntity<ApiResponse<OpdDashboardDto>> getDashboardStats(@RequestParam(required = false) Long doctorId) {
        OpdDashboardDto stats = opdService.getDashboardStats(doctorId);
        return ResponseEntity.ok(ApiResponse.success("OPD dashboard statistics retrieved successfully", stats));
    }
}
