package com.hospital.service;

import com.hospital.dto.RefundDto;
import com.hospital.dto.RefundRequest;
import com.hospital.entity.*;
import com.hospital.exception.InvalidBillingStateException;
import com.hospital.exception.InvalidRefundException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.BillRepository;
import com.hospital.repository.PaymentRepository;
import com.hospital.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final BillingService billingService;
    private final PatientLedgerService ledgerService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<RefundDto> getAllRefunds(Pageable pageable) {
        return refundRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<RefundDto> getRefundsByPatientId(Long patientId, Pageable pageable) {
        return refundRepository.findByPatientId(patientId, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public RefundDto getRefundById(Long id) {
        Refund r = refundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Refund record not found with ID: " + id));
        return mapToDto(r);
    }

    @Transactional
    public RefundDto processRefund(RefundRequest request, String username, boolean isAdmin) {
        Bill bill = billRepository.findById(request.getBillId())
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + request.getBillId()));

        if (bill.getStatus() == BillStatus.DRAFT || bill.getStatus() == BillStatus.CANCELLED) {
            throw new InvalidBillingStateException("Cannot process refund for bill with status: " + bill.getStatus());
        }

        BigDecimal maxRefundable = bill.getPaidAmount().subtract(bill.getRefundedAmount());
        if (maxRefundable.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRefundException("No refundable balance available for Bill: " + bill.getBillNumber());
        }

        if (request.getRefundAmount().compareTo(maxRefundable) > 0) {
            throw new InvalidRefundException("Requested refund amount (₹" + request.getRefundAmount() + ") exceeds maximum refundable balance (₹" + maxRefundable + ")");
        }

        Payment payment = null;
        if (request.getPaymentId() != null) {
            payment = paymentRepository.findById(request.getPaymentId()).orElse(null);
        }

        String refundNumber = generateRefundNumber();

        Refund refund = Refund.builder()
                .refundNumber(refundNumber)
                .bill(bill)
                .payment(payment)
                .patient(bill.getPatient())
                .refundAmount(request.getRefundAmount())
                .refundMethod(request.getRefundMethod())
                .reason(request.getReason())
                .approvedBy(isAdmin ? username : "System Admin")
                .processedBy(username)
                .refundDate(LocalDateTime.now())
                .status(RefundStatus.PROCESSED)
                .build();

        Refund savedRefund = refundRepository.save(refund);

        // Update bill refunded amount
        bill.setRefundedAmount(bill.getRefundedAmount().add(request.getRefundAmount()));
        if (bill.getRefundedAmount().compareTo(bill.getPaidAmount()) >= 0) {
            bill.setStatus(BillStatus.REFUNDED);
        } else {
            bill.setStatus(BillStatus.PARTIALLY_REFUNDED);
        }
        billingService.calculateBillTotals(bill);
        billRepository.save(bill);

        // Record patient ledger entry for refund
        ledgerService.recordEntry(bill.getPatient(), bill, null, savedRefund, null,
                LedgerEntryType.REFUND, request.getRefundAmount(), BigDecimal.ZERO,
                "Financial refund issued (" + request.getRefundMethod() + ") for Bill: " + bill.getBillNumber());

        auditLogService.logAction("PROCESS_REFUND", "Refund", savedRefund.getId().toString(),
                "Processed refund: " + savedRefund.getRefundNumber() + " Amount: ₹" + request.getRefundAmount());

        return mapToDto(savedRefund);
    }

    private String generateRefundNumber() {
        long count = refundRepository.count() + 1;
        return String.format("REF-%d-%06d", LocalDate.now().getYear(), count);
    }

    public RefundDto mapToDto(Refund r) {
        if (r == null) return null;
        return RefundDto.builder()
                .id(r.getId())
                .refundNumber(r.getRefundNumber())
                .billId(r.getBill().getId())
                .billNumber(r.getBill().getBillNumber())
                .paymentId(r.getPayment() != null ? r.getPayment().getId() : null)
                .patientId(r.getPatient().getId())
                .patientName(r.getPatient().getFirstName() + " " + r.getPatient().getLastName())
                .refundAmount(r.getRefundAmount())
                .refundMethod(r.getRefundMethod())
                .reason(r.getReason())
                .approvedBy(r.getApprovedBy())
                .processedBy(r.getProcessedBy())
                .refundDate(r.getRefundDate())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
