package com.hospital.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispenseRequest {
    private Long prescriptionId;
    private Long prescriptionItemId;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    private Long doctorId;

    @NotNull(message = "Medicine ID is required")
    private Long medicineId;

    private Long batchId; // Optional: if null, FEFO is used automatically

    private Long ipdAdmissionId;

    @NotNull(message = "Prescribed quantity is required")
    @Min(value = 1, message = "Prescribed quantity must be positive")
    private Integer prescribedQuantity;

    @NotNull(message = "Dispensed quantity is required")
    @Min(value = 1, message = "Dispensed quantity must be positive")
    private Integer dispensedQuantity;

    private String remarks;
}
