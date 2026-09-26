package com.hospital.service;

import com.hospital.dto.InventoryTransactionDto;
import com.hospital.dto.MedicineResponse;
import com.hospital.dto.StockAdjustmentDto;
import com.hospital.entity.*;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.InsufficientStockException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.InventoryTransactionRepository;
import com.hospital.repository.MedicineBatchRepository;
import com.hospital.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository batchRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final MedicineService medicineService;
    private final BatchService batchService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<MedicineResponse> getInventory(Pageable pageable) {
        return medicineService.getAllMedicines(pageable);
    }

    @Transactional(readOnly = true)
    public List<MedicineResponse> getLowStockInventory() {
        return medicineRepository.findAll().stream()
                .map(medicineService::mapToResponse)
                .filter(m -> Boolean.TRUE.equals(m.getIsLowStock()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<InventoryTransactionDto> getTransactionsForMedicine(Long medicineId, Pageable pageable) {
        return transactionRepository.findByMedicineIdOrderByTransactionDatetimeDesc(medicineId, pageable)
                .map(this::mapToTransactionDto);
    }

    @Transactional
    public InventoryTransaction recordTransaction(Medicine medicine, MedicineBatch batch,
                                                  InventoryTransactionType type, int quantity,
                                                  java.math.BigDecimal unitCost, String refType,
                                                  String refId, String remarks, String performedBy) {
        String txId = generateTransactionId();
        InventoryTransaction tx = InventoryTransaction.builder()
                .transactionId(txId)
                .medicine(medicine)
                .batch(batch)
                .transactionType(type)
                .quantity(quantity)
                .unitCost(unitCost)
                .referenceType(refType)
                .referenceId(refId)
                .remarks(remarks)
                .performedBy(performedBy != null ? performedBy : "System")
                .transactionDatetime(LocalDateTime.now())
                .build();

        return transactionRepository.save(tx);
    }

    @Transactional
    public InventoryTransactionDto adjustStock(StockAdjustmentDto dto, String username) {
        if (!"IN".equalsIgnoreCase(dto.getDirection()) && !"OUT".equalsIgnoreCase(dto.getDirection())) {
            throw new BadRequestException("Adjustment direction must be IN or OUT");
        }

        MedicineBatch batch = batchRepository.findByIdForUpdate(dto.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with ID: " + dto.getBatchId()));

        Medicine medicine = medicineRepository.findById(dto.getMedicineId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with ID: " + dto.getMedicineId()));

        int qty = dto.getAdjustmentQuantity();
        InventoryTransactionType txType;

        if ("IN".equalsIgnoreCase(dto.getDirection())) {
            txType = InventoryTransactionType.STOCK_ADJUSTMENT_IN;
            batch.setQuantityAvailable(batch.getQuantityAvailable() + qty);
        } else {
            txType = InventoryTransactionType.STOCK_ADJUSTMENT_OUT;
            if (batch.getQuantityAvailable() < qty) {
                throw new InsufficientStockException("Cannot adjust OUT " + qty + " units. Available stock in batch is " + batch.getQuantityAvailable());
            }
            batch.setQuantityAvailable(batch.getQuantityAvailable() - qty);
        }

        batch.setStatus(batchService.evaluateBatchStatus(batch.getExpiryDate(), batch.getQuantityAvailable()));
        batchRepository.save(batch);

        InventoryTransaction tx = recordTransaction(medicine, batch, txType, qty,
                batch.getPurchaseRate(), "STOCK_ADJUSTMENT", null,
                dto.getReason() + (dto.getRemarks() != null ? " - " + dto.getRemarks() : ""), username);

        auditLogService.logAction("STOCK_ADJUSTMENT_" + dto.getDirection().toUpperCase(), "MedicineBatch",
                batch.getId().toString(), "Stock adjusted " + dto.getDirection() + " by " + qty + " units for Batch: " + batch.getBatchNumber());

        return mapToTransactionDto(tx);
    }

    private String generateTransactionId() {
        long count = transactionRepository.count() + 1;
        return String.format("INV-%d-%06d", LocalDate.now().getYear(), count);
    }

    public InventoryTransactionDto mapToTransactionDto(InventoryTransaction tx) {
        if (tx == null) return null;
        return InventoryTransactionDto.builder()
                .id(tx.getId())
                .transactionId(tx.getTransactionId())
                .medicineId(tx.getMedicine().getId())
                .medicineName(tx.getMedicine().getMedicineName())
                .batchId(tx.getBatch() != null ? tx.getBatch().getId() : null)
                .batchNumber(tx.getBatch() != null ? tx.getBatch().getBatchNumber() : null)
                .transactionType(tx.getTransactionType())
                .quantity(tx.getQuantity())
                .unitCost(tx.getUnitCost())
                .referenceType(tx.getReferenceType())
                .referenceId(tx.getReferenceId())
                .remarks(tx.getRemarks())
                .performedBy(tx.getPerformedBy())
                .transactionDatetime(tx.getTransactionDatetime())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
