package com.hospital.dto;

import com.hospital.entity.BillSourceType;
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
public class CreditNoteRequest {

    @NotNull(message = "Bill ID is required")
    private Long billId;

    @NotNull(message = "Credit amount is required")
    @DecimalMin(value = "0.01", message = "Credit amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Reason is required")
    private String reason;

    private BillSourceType sourceType;
    private Long sourceId;
}
