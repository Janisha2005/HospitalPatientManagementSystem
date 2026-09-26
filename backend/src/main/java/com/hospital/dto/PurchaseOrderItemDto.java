package com.hospital.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderItemDto {
    private Long id;

    @NotNull(message = "Medicine ID is required")
    private Long medicineId;
    private String medicineName;
    private String medicineCode;

    @NotNull(message = "Ordered quantity is required")
    @Min(value = 1, message = "Ordered quantity must be at least 1")
    private Integer orderedQuantity;

    private Integer receivedQuantity;

    @NotNull(message = "Unit cost is required")
    private BigDecimal unitCost;

    private BigDecimal taxPercentage;
    private BigDecimal discountAmount;
    private BigDecimal lineTotal;
}
