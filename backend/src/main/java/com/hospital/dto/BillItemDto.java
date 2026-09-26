package com.hospital.dto;

import com.hospital.entity.BillSourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillItemDto {
    private Long id;
    private String chargeCode;

    @NotBlank(message = "Item description is required")
    private String description;

    @NotNull(message = "Source type is required")
    private BillSourceType sourceType;

    private Long sourceId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Unit rate is required")
    private BigDecimal unitRate;

    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal lineTotal;
    private LocalDateTime createdAt;
}
