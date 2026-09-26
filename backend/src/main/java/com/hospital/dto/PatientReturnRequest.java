package com.hospital.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientReturnRequest {
    @NotNull(message = "Dispensing ID is required")
    private Long dispensingId;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Medicine ID is required")
    private Long medicineId;

    @NotNull(message = "Batch ID is required")
    private Long batchId;

    @NotNull(message = "Return quantity is required")
    @Min(value = 1, message = "Return quantity must be positive")
    private Integer quantityReturned;

    private Boolean isRestocked; // true if stock should be added back to available inventory
    private String reason;
    private String remarks;
}
