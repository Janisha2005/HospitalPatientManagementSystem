package com.hospital.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyDashboardDto {
    private long totalMedicines;
    private long activeMedicines;
    private long totalInventoryQuantity;
    private long lowStockCount;
    private long nearExpiryCount;
    private long expiredCount;
    private long todayPrescriptionCount;
    private long pendingPrescriptionCount;
    private long partiallyDispensedCount;
    private long todayDispensingCount;
    private long todayPurchaseReceiptCount;
    private long pendingPurchaseOrderCount;
    private long activeSupplierCount;

    private List<MedicineResponse> lowStockMedicines;
    private List<MedicineBatchDto> nearExpiryBatches;
    private List<PharmacyDispensingDto> recentDispensings;
}
