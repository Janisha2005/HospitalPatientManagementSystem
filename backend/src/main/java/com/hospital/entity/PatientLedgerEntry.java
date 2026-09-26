package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_ledger_entries", indexes = {
        @Index(name = "idx_ple_number", columnList = "ledger_number", unique = true),
        @Index(name = "idx_ple_patient", columnList = "patient_id"),
        @Index(name = "idx_ple_date", columnList = "entry_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientLedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ledger_number", nullable = false, unique = true, length = 30)
    private String ledgerNumber; // LEG-YYYY-000001

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private Bill bill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_id")
    private Refund refund;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_note_id")
    private CreditNote creditNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 30)
    private LedgerEntryType entryType;

    @Column(name = "debit_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal debitAmount = BigDecimal.ZERO; // Bill amount increases patient debt

    @Column(name = "credit_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal creditAmount = BigDecimal.ZERO; // Payment/CreditNote decreases patient debt

    @Column(name = "balance_after", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceAfter;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "entry_date", nullable = false)
    private LocalDateTime entryDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
