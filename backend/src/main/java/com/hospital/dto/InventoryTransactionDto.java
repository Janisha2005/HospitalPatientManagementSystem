package com.hospital.dto;

import com.hospital.entity.InventoryTransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransactionDto {
    private Long id;
    private String transactionId;
    private Long medicineId;
    private String medicineName;
    private Long batchId;
    private String batchNumber;
    private InventoryTransactionType transactionType;
    private Integer quantity;
    private BigDecimal unitCost;
    private String referenceType;
    private String referenceId;
    private String remarks;
    private String performedBy;
    private LocalDateTime transactionDatetime;
    private LocalDateTime createdAt;
}
