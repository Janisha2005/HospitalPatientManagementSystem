package com.hospital.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryDashboardDto {
    private long totalStockItems;
    private BigDecimal totalStockValue;
    private long lowStockItemsCount;
    private BigDecimal expiredStockValue;
    private long nearExpiryItemsCount;
    private List<InventoryTransactionDto> recentMovements;
    private List<GoodsReceiptDto> recentReceipts;
    private List<PharmacyDispensingDto> recentDispensings;
    private List<PharmacyReturnDto> recentReturns;
}
