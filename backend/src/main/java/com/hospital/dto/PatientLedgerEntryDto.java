package com.hospital.dto;

import com.hospital.entity.LedgerEntryType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientLedgerEntryDto {
    private Long id;
    private String ledgerNumber;
    private Long patientId;
    private String patientName;
    private Long billId;
    private String billNumber;
    private Long paymentId;
    private Long refundId;
    private Long creditNoteId;
    private LedgerEntryType entryType;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private BigDecimal balanceAfter;
    private String description;
    private LocalDateTime entryDate;
    private LocalDateTime createdAt;
}
