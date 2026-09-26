package com.hospital.service;

import com.hospital.dto.PatientLedgerEntryDto;
import com.hospital.entity.*;
import com.hospital.repository.PatientLedgerEntryRepository;
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
public class PatientLedgerService {

    private final PatientLedgerEntryRepository ledgerRepository;

    @Transactional(readOnly = true)
    public List<PatientLedgerEntryDto> getLedgerForPatient(Long patientId) {
        return ledgerRepository.findByPatientIdOrderByEntryDateAsc(patientId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PatientLedgerEntryDto> getLedgerPageForPatient(Long patientId, Pageable pageable) {
        return ledgerRepository.findByPatientId(patientId, pageable).map(this::mapToDto);
    }

    @Transactional
    public PatientLedgerEntry recordEntry(Patient patient, Bill bill, Payment payment, Refund refund,
                                         CreditNote creditNote, LedgerEntryType entryType,
                                         BigDecimal debitAmount, BigDecimal creditAmount, String description) {
        BigDecimal debit = debitAmount != null ? debitAmount : BigDecimal.ZERO;
        BigDecimal credit = creditAmount != null ? creditAmount : BigDecimal.ZERO;

        List<PatientLedgerEntry> existing = ledgerRepository.findByPatientIdOrderByEntryDateAsc(patient.getId());
        BigDecimal currentBalance = existing.isEmpty() ? BigDecimal.ZERO : existing.get(existing.size() - 1).getBalanceAfter();

        // Debit increases patient balance (they owe us more); Credit decreases patient balance (they paid/were credited)
        BigDecimal balanceAfter = currentBalance.add(debit).subtract(credit);

        String ledgerNumber = generateLedgerNumber();

        PatientLedgerEntry entry = PatientLedgerEntry.builder()
                .ledgerNumber(ledgerNumber)
                .patient(patient)
                .bill(bill)
                .payment(payment)
                .refund(refund)
                .creditNote(creditNote)
                .entryType(entryType)
                .debitAmount(debit)
                .creditAmount(credit)
                .balanceAfter(balanceAfter)
                .description(description)
                .entryDate(LocalDateTime.now())
                .build();

        return ledgerRepository.save(entry);
    }

    private String generateLedgerNumber() {
        long count = ledgerRepository.count() + 1;
        return String.format("LEG-%d-%06d", LocalDate.now().getYear(), count);
    }

    public PatientLedgerEntryDto mapToDto(PatientLedgerEntry e) {
        if (e == null) return null;
        return PatientLedgerEntryDto.builder()
                .id(e.getId())
                .ledgerNumber(e.getLedgerNumber())
                .patientId(e.getPatient().getId())
                .patientName(e.getPatient().getFirstName() + " " + e.getPatient().getLastName())
                .billId(e.getBill() != null ? e.getBill().getId() : null)
                .billNumber(e.getBill() != null ? e.getBill().getBillNumber() : null)
                .paymentId(e.getPayment() != null ? e.getPayment().getId() : null)
                .refundId(e.getRefund() != null ? e.getRefund().getId() : null)
                .creditNoteId(e.getCreditNote() != null ? e.getCreditNote().getId() : null)
                .entryType(e.getEntryType())
                .debitAmount(e.getDebitAmount())
                .creditAmount(e.getCreditAmount())
                .balanceAfter(e.getBalanceAfter())
                .description(e.getDescription())
                .entryDate(e.getEntryDate())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
