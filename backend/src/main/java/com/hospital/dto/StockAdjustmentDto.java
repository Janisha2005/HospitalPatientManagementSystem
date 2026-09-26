package com.hospital.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAdjustmentDto {
    @NotNull(message = "Medicine ID is required")
    private Long medicineId;

    @NotNull(message = "Batch ID is required")
    private Long batchId;

    @NotNull(message = "Adjustment quantity is required")
    @Min(value = 1, message = "Adjustment quantity must be positive")
    private Integer adjustmentQuantity;

    @NotBlank(message = "Adjustment direction (IN or OUT) is required")
    private String direction; // "IN" or "OUT"

    @NotBlank(message = "Reason is required")
    private String reason;

    private String remarks;
}
