package com.hospital.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceiptItemDto {
    private Long id;

    @NotNull(message = "Medicine ID is required")
    private Long medicineId;
    private String medicineName;

    private Long batchId;

    @NotBlank(message = "Batch number is required")
    private String batchNumber;

    @NotNull(message = "Quantity received is required")
    @Min(value = 1, message = "Quantity received must be positive")
    private Integer quantityReceived;

    @NotNull(message = "Purchase rate is required")
    private BigDecimal purchaseRate;

    private BigDecimal mrp;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;
}
