package com.hospital.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyReturnItemDto {
    private Long id;
    private Long medicineId;
    private String medicineName;
    private Long batchId;
    private String batchNumber;
    private Integer quantityReturned;
    private BigDecimal unitRate;
    private Boolean isRestocked;
    private BigDecimal lineTotal;
}
