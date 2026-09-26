package com.hospital.dto;

import com.hospital.entity.PaymentMethod;
import com.hospital.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {
    private Long id;
    private String paymentNumber;
    private Long billId;
    private String billNumber;
    private Long patientId;
    private String patientName;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String referenceNumber;
    private String receivedBy;
    private String remarks;
    private PaymentStatus status;
    private String reversedBy;
    private String reversalReason;
    private LocalDateTime reversalDatetime;
    private LocalDateTime createdAt;
}
