package com.hospital.controller;

import com.hospital.dto.LabDashboardDto;
import com.hospital.dto.LabOrderItemRequest;
import com.hospital.dto.LabOrderRequest;
import com.hospital.dto.LabOrderResponse;
import com.hospital.dto.LabTestRequest;
import com.hospital.dto.LabTestResponse;
import com.hospital.entity.LabOrderStatus;
import com.hospital.service.LaboratoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lab")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LaboratoryController {

    @Autowired
    private LaboratoryService laboratoryService;

    // --- Catalog ---

    @GetMapping("/tests")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('PATIENT')")
    public ResponseEntity<List<LabTestResponse>> getAllLabTests(
            @RequestParam(required = false) Boolean activeOnly) {
        return ResponseEntity.ok(laboratoryService.getAllLabTests(activeOnly));
    }

    @GetMapping("/tests/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<LabTestResponse> getLabTestById(@PathVariable Long id) {
        return ResponseEntity.ok(laboratoryService.getLabTestById(id));
    }

    @PostMapping("/tests")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<LabTestResponse> createLabTest(@Valid @RequestBody LabTestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(laboratoryService.createLabTest(request));
    }

    @PutMapping("/tests/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<LabTestResponse> updateLabTest(
            @PathVariable Long id,
            @Valid @RequestBody LabTestRequest request) {
        return ResponseEntity.ok(laboratoryService.updateLabTest(id, request));
    }

    // --- Orders ---

    @GetMapping("/orders")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<List<LabOrderResponse>> getAllOrders(
            @RequestParam(required = false) LabOrderStatus status) {
        return ResponseEntity.ok(laboratoryService.getAllOrders(status));
    }

    @GetMapping("/patients/{patientId}/orders")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or (hasRole('PATIENT') and @userSecurity.isPatient(#patientId))")
    public ResponseEntity<List<LabOrderResponse>> getPatientOrders(@PathVariable Long patientId) {
        return ResponseEntity.ok(laboratoryService.getPatientOrders(patientId));
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('PATIENT')")
    public ResponseEntity<LabOrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(laboratoryService.getOrderById(id));
    }

    @PostMapping("/orders")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<LabOrderResponse> createOrder(@Valid @RequestBody LabOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(laboratoryService.createOrder(request));
    }

    @PutMapping("/orders/{id}/collect-sample")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<LabOrderResponse> collectSample(@PathVariable Long id) {
        return ResponseEntity.ok(laboratoryService.collectSample(id));
    }

    @PutMapping("/orders/{id}/items/{itemId}/result")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<LabOrderResponse> enterResult(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @Valid @RequestBody LabOrderItemRequest request) {
        return ResponseEntity.ok(laboratoryService.enterResult(id, itemId, request));
    }

    @PutMapping("/orders/{id}/verify")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<LabOrderResponse> verifyOrder(@PathVariable Long id) {
        return ResponseEntity.ok(laboratoryService.verifyOrder(id));
    }

    @PutMapping("/orders/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<LabOrderResponse> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Cancelled by doctor") String reason) {
        return ResponseEntity.ok(laboratoryService.cancelOrder(id, reason));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<LabDashboardDto> getDashboardStats() {
        return ResponseEntity.ok(laboratoryService.getDashboardStats());
    }
}
