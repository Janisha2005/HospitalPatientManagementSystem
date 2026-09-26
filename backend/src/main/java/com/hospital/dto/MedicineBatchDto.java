package com.hospital.dto;

import com.hospital.entity.BatchStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineBatchDto {
    private Long id;
    private String batchId;

    @NotNull(message = "Medicine ID is required")
    private Long medicineId;
    private String medicineCode;
    private String medicineName;

    private Long supplierId;
    private String supplierName;

    @NotBlank(message = "Batch number is required")
    private String batchNumber;

    private LocalDate manufacturingDate;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    private BigDecimal purchaseRate;
    private BigDecimal mrp;
    private BigDecimal sellingRate;
    private Integer quantityReceived;
    private Integer quantityAvailable;
    private Integer quantityReserved;
    private Integer quantityDamaged;
    private Integer quantityExpired;
    private String storageLocation;
    private BatchStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
