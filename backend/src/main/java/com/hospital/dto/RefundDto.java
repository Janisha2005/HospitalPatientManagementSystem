package com.hospital.dto;

import com.hospital.entity.PaymentMethod;
import com.hospital.entity.RefundStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundDto {
    private Long id;
    private String refundNumber;
    private Long billId;
    private String billNumber;
    private Long paymentId;
    private Long patientId;
    private String patientName;
    private BigDecimal refundAmount;
    private PaymentMethod refundMethod;
    private String reason;
    private String approvedBy;
    private String processedBy;
    private LocalDateTime refundDate;
    private RefundStatus status;
    private LocalDateTime createdAt;
}
