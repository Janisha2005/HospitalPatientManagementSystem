package com.hospital.dto;

import com.hospital.entity.BillStatus;
import com.hospital.entity.BillType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillDto {
    private Long id;
    private String billNumber;

    @NotNull(message = "Patient ID is required")
    private Long patientId;
    private String patientName;
    private String patientCode;
    private Long billingAccountId;

    private Long opdVisitId;
    private Long ipdAdmissionId;

    @NotNull(message = "Bill type is required")
    private BillType billType;

    private LocalDate billDate;
    private LocalDate dueDate;

    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private String discountReason;
    private String discountApprovedBy;
    private BigDecimal taxAmount;
    private BigDecimal roundOff;
    private BigDecimal grandTotal;
    private BigDecimal paidAmount;
    private BigDecimal refundedAmount;
    private BigDecimal outstandingAmount;

    private BillStatus status;
    private String createdBy;
    private String notes;

    private List<BillItemDto> items;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
