package com.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyAnalyticsDto {
    private long dispensingTransactionsCount;
    private long totalMedicinesDispensedCount;
    private BigDecimal pharmacyRevenue;
    private long lowStockMedicinesCount;
    private long nearExpiryBatchesCount;
    private long expiredBatchesCount;
    private long pendingPurchaseOrdersCount;
    private long patientReturnsCount;
    
    private List<TopDispensedMedicineDto> topDispensedMedicines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopDispensedMedicineDto {
        private Long medicineId;
        private String medicineName;
        private String categoryName;
        private long quantityDispensed;
        private BigDecimal totalRevenue;
    }
}
