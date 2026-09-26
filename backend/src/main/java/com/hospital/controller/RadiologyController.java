package com.hospital.controller;

import com.hospital.dto.RadiologyDashboardDto;
import com.hospital.dto.RadiologyOrderRequest;
import com.hospital.dto.RadiologyOrderResponse;
import com.hospital.dto.RadiologyReportRequest;
import com.hospital.dto.RadiologyReportResponse;
import com.hospital.dto.RadiologyTestRequest;
import com.hospital.dto.RadiologyTestResponse;
import com.hospital.entity.RadiologyOrderStatus;
import com.hospital.service.RadiologyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/radiology")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RadiologyController {

    @Autowired
    private RadiologyService radiologyService;

    // --- Catalog ---

    @GetMapping("/tests")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('PATIENT')")
    public ResponseEntity<List<RadiologyTestResponse>> getAllRadiologyTests(
            @RequestParam(required = false) Boolean activeOnly) {
        return ResponseEntity.ok(radiologyService.getAllRadiologyTests(activeOnly));
    }

    @GetMapping("/tests/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<RadiologyTestResponse> getRadiologyTestById(@PathVariable Long id) {
        return ResponseEntity.ok(radiologyService.getRadiologyTestById(id));
    }

    @PostMapping("/tests")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<RadiologyTestResponse> createRadiologyTest(@Valid @RequestBody RadiologyTestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(radiologyService.createRadiologyTest(request));
    }

    @PutMapping("/tests/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<RadiologyTestResponse> updateRadiologyTest(
            @PathVariable Long id,
            @Valid @RequestBody RadiologyTestRequest request) {
        return ResponseEntity.ok(radiologyService.updateRadiologyTest(id, request));
    }

    // --- Orders ---

    @GetMapping("/orders")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<List<RadiologyOrderResponse>> getAllOrders(
            @RequestParam(required = false) RadiologyOrderStatus status) {
        return ResponseEntity.ok(radiologyService.getAllOrders(status));
    }

    @GetMapping("/patients/{patientId}/orders")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or (hasRole('PATIENT') and @userSecurity.isPatient(#patientId))")
    public ResponseEntity<List<RadiologyOrderResponse>> getPatientOrders(@PathVariable Long patientId) {
        return ResponseEntity.ok(radiologyService.getPatientOrders(patientId));
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('PATIENT')")
    public ResponseEntity<RadiologyOrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(radiologyService.getOrderById(id));
    }

    @PostMapping("/orders")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<RadiologyOrderResponse> createOrder(@Valid @RequestBody RadiologyOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(radiologyService.createOrder(request));
    }

    @PutMapping("/orders/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<RadiologyOrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam RadiologyOrderStatus status) {
        return ResponseEntity.ok(radiologyService.updateOrderStatus(id, status));
    }

    // --- Reports ---

    @GetMapping("/orders/{id}/report")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('PATIENT')")
    public ResponseEntity<RadiologyReportResponse> getReportByOrderId(@PathVariable Long id) {
        return ResponseEntity.ok(radiologyService.getReportByOrderId(id));
    }

    @PostMapping("/orders/{id}/report")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<RadiologyReportResponse> createOrUpdateReport(
            @PathVariable Long id,
            @Valid @RequestBody RadiologyReportRequest request) {
        return ResponseEntity.ok(radiologyService.createOrUpdateReport(id, request));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<RadiologyDashboardDto> getDashboardStats() {
        return ResponseEntity.ok(radiologyService.getDashboardStats());
    }
}
