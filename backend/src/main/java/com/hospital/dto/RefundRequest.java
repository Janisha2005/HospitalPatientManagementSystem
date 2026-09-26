package com.hospital.dto;

import com.hospital.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequest {

    @NotNull(message = "Bill ID is required")
    private Long billId;

    private Long paymentId;

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be positive")
    private BigDecimal refundAmount;

    @NotNull(message = "Refund method is required")
    private PaymentMethod refundMethod;

    @NotBlank(message = "Refund reason is required")
    private String reason;
}
