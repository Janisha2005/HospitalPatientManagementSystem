package com.hospital.dto;

import com.hospital.entity.BillSourceType;
import com.hospital.entity.CreditNoteStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditNoteDto {
    private Long id;
    private String creditNoteNumber;
    private Long billId;
    private String billNumber;
    private Long patientId;
    private String patientName;
    private BigDecimal amount;
    private String reason;
    private BillSourceType sourceType;
    private Long sourceId;
    private CreditNoteStatus status;
    private String createdBy;
    private String approvedBy;
    private LocalDateTime createdAt;
}
