package com.hospital.service;

import com.hospital.dto.PaymentDto;
import com.hospital.dto.PaymentRequest;
import com.hospital.entity.*;
import com.hospital.exception.InvalidBillingStateException;
import com.hospital.exception.PaymentExceedsOutstandingException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.BillRepository;
import com.hospital.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final BillingService billingService;
    private final PatientLedgerService ledgerService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<PaymentDto> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsByBillId(Long billId) {
        return paymentRepository.findByBillId(billId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PaymentDto> getPaymentsByPatientId(Long patientId, Pageable pageable) {
        return paymentRepository.findByPatientId(patientId, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(Long id) {
        Payment p = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found with ID: " + id));
        return mapToDto(p);
    }

    @Transactional
    public PaymentDto recordPayment(PaymentRequest request, String username) {
        Bill bill = billRepository.findById(request.getBillId())
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + request.getBillId()));

        if (bill.getStatus() == BillStatus.DRAFT || bill.getStatus() == BillStatus.CANCELLED) {
            throw new InvalidBillingStateException("Cannot record payment for a bill with status: " + bill.getStatus());
        }

        BigDecimal payAmount = request.getAmount();
        if (payAmount.compareTo(bill.getOutstandingAmount()) > 0) {
            throw new PaymentExceedsOutstandingException("Payment amount (₹" + payAmount + ") exceeds outstanding balance (₹" + bill.getOutstandingAmount() + ")");
        }

        String paymentNumber = generatePaymentNumber();

        Payment payment = Payment.builder()
                .paymentNumber(paymentNumber)
                .bill(bill)
                .patient(bill.getPatient())
                .paymentDate(LocalDateTime.now())
                .amount(payAmount)
                .paymentMethod(request.getPaymentMethod())
                .referenceNumber(request.getReferenceNumber())
                .receivedBy(username)
                .remarks(request.getRemarks())
                .status(PaymentStatus.COMPLETED)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Update bill figures
        bill.setPaidAmount(bill.getPaidAmount().add(payAmount));
        billingService.calculateBillTotals(bill);
        billRepository.save(bill);

        // Ledger credit entry (reduces patient balance)
        ledgerService.recordEntry(bill.getPatient(), bill, savedPayment, null, null,
                LedgerEntryType.PAYMENT, BigDecimal.ZERO, payAmount,
                "Payment received (" + request.getPaymentMethod() + ") for Bill: " + bill.getBillNumber());

        auditLogService.logAction("RECORD_PAYMENT", "Payment", savedPayment.getId().toString(),
                "Recorded payment: " + savedPayment.getPaymentNumber() + " Amount: ₹" + payAmount + " for Bill: " + bill.getBillNumber());

        return mapToDto(savedPayment);
    }

    @Transactional
    public PaymentDto reversePayment(Long paymentId, String reason, String username) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found with ID: " + paymentId));

        if (payment.getStatus() == PaymentStatus.REVERSED) {
            throw new InvalidBillingStateException("Payment ID " + paymentId + " is already reversed.");
        }

        payment.setStatus(PaymentStatus.REVERSED);
        payment.setReversedBy(username);
        payment.setReversalReason(reason);
        payment.setReversalDatetime(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);

        // Revert bill paid amount
        Bill bill = payment.getBill();
        BigDecimal newPaid = bill.getPaidAmount().subtract(payment.getAmount());
        if (newPaid.compareTo(BigDecimal.ZERO) < 0) newPaid = BigDecimal.ZERO;
        bill.setPaidAmount(newPaid);

        billingService.calculateBillTotals(bill);
        billRepository.save(bill);

        // Ledger reversal entry (debit back)
        ledgerService.recordEntry(bill.getPatient(), bill, savedPayment, null, null,
                LedgerEntryType.ADJUSTMENT, payment.getAmount(), BigDecimal.ZERO,
                "Payment Reversal: " + savedPayment.getPaymentNumber() + " Reason: " + reason);

        auditLogService.logAction("REVERSE_PAYMENT", "Payment", savedPayment.getId().toString(),
                "Reversed payment: " + savedPayment.getPaymentNumber() + " Reason: " + reason);

        return mapToDto(savedPayment);
    }

    private String generatePaymentNumber() {
        long count = paymentRepository.count() + 1;
        return String.format("PAY-%d-%06d", LocalDate.now().getYear(), count);
    }

    public PaymentDto mapToDto(Payment p) {
        if (p == null) return null;
        return PaymentDto.builder()
                .id(p.getId())
                .paymentNumber(p.getPaymentNumber())
                .billId(p.getBill().getId())
                .billNumber(p.getBill().getBillNumber())
                .patientId(p.getPatient().getId())
                .patientName(p.getPatient().getFirstName() + " " + p.getPatient().getLastName())
                .paymentDate(p.getPaymentDate())
                .amount(p.getAmount())
                .paymentMethod(p.getPaymentMethod())
                .referenceNumber(p.getReferenceNumber())
                .receivedBy(p.getReceivedBy())
                .remarks(p.getRemarks())
                .status(p.getStatus())
                .reversedBy(p.getReversedBy())
                .reversalReason(p.getReversalReason())
                .reversalDatetime(p.getReversalDatetime())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
