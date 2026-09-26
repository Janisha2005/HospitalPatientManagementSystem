package com.hospital.service;

import com.hospital.dto.*;
import com.hospital.entity.BatchStatus;
import com.hospital.entity.MedicineBatch;
import com.hospital.entity.PrescriptionStatus;
import com.hospital.entity.PurchaseOrderStatus;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PharmacyDashboardService {

    private final MedicineRepository medicineRepository;
    private final MedicineCategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final MedicineBatchRepository batchRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PharmacyDispensingRepository dispensingRepository;
    private final PharmacyReturnRepository returnRepository;
    private final PrescriptionRepository prescriptionRepository;

    private final MedicineService medicineService;
    private final BatchService batchService;
    private final DispensingService dispensingService;
    private final InventoryService inventoryService;
    private final GoodsReceiptService goodsReceiptService;
    private final PharmacyReturnService returnService;

    @Transactional(readOnly = true)
    public PharmacyDashboardDto getPharmacyDashboard() {
        long totalMedicines = medicineRepository.count();
        long activeMedicines = medicineRepository.findByIsActiveTrue().size();

        List<MedicineBatch> allBatches = batchRepository.findAll();
        long totalInventoryQuantity = allBatches.stream().mapToLong(MedicineBatch::getQuantityAvailable).sum();

        List<MedicineResponse> allMedicineResponses = medicineRepository.findAll().stream()
                .map(medicineService::mapToResponse)
                .collect(Collectors.toList());

        List<MedicineResponse> lowStockList = allMedicineResponses.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsLowStock()))
                .collect(Collectors.toList());

        long lowStockCount = lowStockList.size();

        List<MedicineBatchDto> nearExpiryList = batchService.getNearExpiryBatches();
        long nearExpiryCount = nearExpiryList.size();

        List<MedicineBatchDto> expiredList = batchService.getExpiredBatches();
        long expiredCount = expiredList.size();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        long todayPrescriptionCount = prescriptionRepository.countByPrescriptionDate(today);
        long pendingPrescriptionCount = prescriptionRepository.countByStatus(PrescriptionStatus.ISSUED);
        long todayDispensingCount = dispensingRepository.countDispensingsForDay(startOfDay, endOfDay);
        long todayPurchaseReceiptCount = goodsReceiptRepository.countByReceiptDate(today);
        long pendingPurchaseOrderCount = purchaseOrderRepository.countByStatus(PurchaseOrderStatus.PLACED)
                + purchaseOrderRepository.countByStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        long activeSupplierCount = supplierRepository.findByIsActiveTrue().size();

        List<PharmacyDispensingDto> recentDispensings = dispensingRepository.findAll(PageRequest.of(0, 5))
                .getContent().stream().map(dispensingService::mapToDto).collect(Collectors.toList());

        return PharmacyDashboardDto.builder()
                .totalMedicines(totalMedicines)
                .activeMedicines(activeMedicines)
                .totalInventoryQuantity(totalInventoryQuantity)
                .lowStockCount(lowStockCount)
                .nearExpiryCount(nearExpiryCount)
                .expiredCount(expiredCount)
                .todayPrescriptionCount(todayPrescriptionCount)
                .pendingPrescriptionCount(pendingPrescriptionCount)
                .partiallyDispensedCount(0)
                .todayDispensingCount(todayDispensingCount)
                .todayPurchaseReceiptCount(todayPurchaseReceiptCount)
                .pendingPurchaseOrderCount(pendingPurchaseOrderCount)
                .activeSupplierCount(activeSupplierCount)
                .lowStockMedicines(lowStockList.stream().limit(5).collect(Collectors.toList()))
                .nearExpiryBatches(nearExpiryList.stream().limit(5).collect(Collectors.toList()))
                .recentDispensings(recentDispensings)
                .build();
    }

    @Transactional(readOnly = true)
    public InventoryDashboardDto getInventoryDashboard() {
        List<MedicineBatch> allBatches = batchRepository.findAll();
        long totalStockItems = allBatches.stream().mapToLong(MedicineBatch::getQuantityAvailable).sum();

        BigDecimal totalStockValue = allBatches.stream()
                .filter(b -> b.getStatus() != BatchStatus.EXPIRED)
                .map(b -> {
                    BigDecimal rate = b.getPurchaseRate() != null ? b.getPurchaseRate() : BigDecimal.ZERO;
                    return rate.multiply(BigDecimal.valueOf(b.getQuantityAvailable()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expiredStockValue = allBatches.stream()
                .filter(b -> b.getStatus() == BatchStatus.EXPIRED || b.getExpiryDate().isBefore(LocalDate.now()))
                .map(b -> {
                    BigDecimal rate = b.getPurchaseRate() != null ? b.getPurchaseRate() : BigDecimal.ZERO;
                    return rate.multiply(BigDecimal.valueOf(b.getQuantityAvailable() + b.getQuantityExpired()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<MedicineResponse> allMedicineResponses = medicineRepository.findAll().stream()
                .map(medicineService::mapToResponse)
                .collect(Collectors.toList());
        long lowStockCount = allMedicineResponses.stream().filter(m -> Boolean.TRUE.equals(m.getIsLowStock())).count();

        long nearExpiryCount = batchService.getNearExpiryBatches().size();

        List<InventoryTransactionDto> recentMovements = transactionRepository.findTop10ByOrderByTransactionDatetimeDesc()
                .stream().map(inventoryService::mapToTransactionDto).collect(Collectors.toList());

        List<GoodsReceiptDto> recentReceipts = goodsReceiptRepository.findAll(PageRequest.of(0, 5))
                .getContent().stream().map(goodsReceiptService::mapToDto).collect(Collectors.toList());

        List<PharmacyDispensingDto> recentDispensings = dispensingRepository.findAll(PageRequest.of(0, 5))
                .getContent().stream().map(dispensingService::mapToDto).collect(Collectors.toList());

        List<PharmacyReturnDto> recentReturns = returnRepository.findAll(PageRequest.of(0, 5))
                .getContent().stream().map(returnService::mapToDto).collect(Collectors.toList());

        return InventoryDashboardDto.builder()
                .totalStockItems(totalStockItems)
                .totalStockValue(totalStockValue)
                .lowStockItemsCount(lowStockCount)
                .expiredStockValue(expiredStockValue)
                .nearExpiryItemsCount(nearExpiryCount)
                .recentMovements(recentMovements)
                .recentReceipts(recentReceipts)
                .recentDispensings(recentDispensings)
                .recentReturns(recentReturns)
                .build();
    }
}
