package com.hospital.dto;

import com.hospital.entity.DischargeClearanceStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DischargeClearanceDto {
    private Long ipdAdmissionId;
    private String admissionNumber;
    private Long patientId;
    private String patientName;
    private Long finalBillId;
    private String finalBillNumber;
    private BigDecimal grandTotal;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private DischargeClearanceStatus clearanceStatus;
    private String remarks;
    private String clearedBy;
}
