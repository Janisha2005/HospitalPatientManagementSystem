package com.hospital.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierReturnRequest {
    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotNull(message = "Medicine ID is required")
    private Long medicineId;

    @NotNull(message = "Batch ID is required")
    private Long batchId;

    @NotNull(message = "Return quantity is required")
    @Min(value = 1, message = "Return quantity must be positive")
    private Integer quantityReturned;

    private String reason;
    private String remarks;
}
