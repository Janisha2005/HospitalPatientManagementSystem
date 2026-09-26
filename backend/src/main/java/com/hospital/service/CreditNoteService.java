package com.hospital.service;

import com.hospital.dto.CreditNoteDto;
import com.hospital.dto.CreditNoteRequest;
import com.hospital.entity.*;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.BillRepository;
import com.hospital.repository.CreditNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CreditNoteService {

    private final CreditNoteRepository creditNoteRepository;
    private final BillRepository billRepository;
    private final PatientLedgerService ledgerService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<CreditNoteDto> getAllCreditNotes(Pageable pageable) {
        return creditNoteRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<CreditNoteDto> getCreditNotesByPatientId(Long patientId, Pageable pageable) {
        return creditNoteRepository.findByPatientId(patientId, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public CreditNoteDto getCreditNoteById(Long id) {
        CreditNote cn = creditNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credit Note not found with ID: " + id));
        return mapToDto(cn);
    }

    @Transactional
    public CreditNoteDto createCreditNote(CreditNoteRequest request, String username) {
        Bill bill = billRepository.findById(request.getBillId())
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + request.getBillId()));

        if (request.getAmount().compareTo(bill.getGrandTotal()) > 0) {
            throw new BadRequestException("Credit note amount (₹" + request.getAmount() + ") cannot exceed bill grand total (₹" + bill.getGrandTotal() + ")");
        }

        String creditNoteNumber = generateCreditNoteNumber();

        CreditNote cn = CreditNote.builder()
                .creditNoteNumber(creditNoteNumber)
                .bill(bill)
                .patient(bill.getPatient())
                .amount(request.getAmount())
                .reason(request.getReason())
                .sourceType(request.getSourceType())
                .sourceId(request.getSourceId())
                .status(CreditNoteStatus.APPROVED)
                .createdBy(username)
                .approvedBy(username)
                .build();

        CreditNote saved = creditNoteRepository.save(cn);

        // Record credit entry in patient ledger (decreases debt)
        ledgerService.recordEntry(bill.getPatient(), bill, null, null, saved,
                LedgerEntryType.CREDIT_NOTE, BigDecimal.ZERO, request.getAmount(),
                "Credit Note issued: " + saved.getCreditNoteNumber() + " Reason: " + request.getReason());

        auditLogService.logAction("CREATE_CREDIT_NOTE", "CreditNote", saved.getId().toString(),
                "Issued credit note: " + saved.getCreditNoteNumber() + " Amount: ₹" + request.getAmount());

        return mapToDto(saved);
    }

    private String generateCreditNoteNumber() {
        long count = creditNoteRepository.count() + 1;
        return String.format("CN-%d-%06d", LocalDate.now().getYear(), count);
    }

    public CreditNoteDto mapToDto(CreditNote cn) {
        if (cn == null) return null;
        return CreditNoteDto.builder()
                .id(cn.getId())
                .creditNoteNumber(cn.getCreditNoteNumber())
                .billId(cn.getBill().getId())
                .billNumber(cn.getBill().getBillNumber())
                .patientId(cn.getPatient().getId())
                .patientName(cn.getPatient().getFirstName() + " " + cn.getPatient().getLastName())
                .amount(cn.getAmount())
                .reason(cn.getReason())
                .sourceType(cn.getSourceType())
                .sourceId(cn.getSourceId())
                .status(cn.getStatus())
                .createdBy(cn.getCreatedBy())
                .approvedBy(cn.getApprovedBy())
                .createdAt(cn.getCreatedAt())
                .build();
    }
}
