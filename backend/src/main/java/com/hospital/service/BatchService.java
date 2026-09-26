package com.hospital.service;

import com.hospital.dto.MedicineBatchDto;
import com.hospital.entity.BatchStatus;
import com.hospital.entity.Medicine;
import com.hospital.entity.MedicineBatch;
import com.hospital.entity.Supplier;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.MedicineBatchRepository;
import com.hospital.repository.MedicineRepository;
import com.hospital.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BatchService {

    public static final int NEAR_EXPIRY_THRESHOLD_DAYS = 30;

    private final MedicineBatchRepository batchRepository;
    private final MedicineRepository medicineRepository;
    private final SupplierRepository supplierRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<MedicineBatchDto> getAllBatches(Pageable pageable) {
        return batchRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<MedicineBatchDto> searchBatches(String query, Pageable pageable) {
        return batchRepository.searchBatches(query, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<MedicineBatchDto> getBatchesByMedicineId(Long medicineId) {
        return batchRepository.findByMedicineId(medicineId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MedicineBatchDto getBatchById(Long id) {
        MedicineBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine Batch not found with ID: " + id));
        return mapToDto(batch);
    }

    @Transactional(readOnly = true)
    public List<MedicineBatchDto> getNearExpiryBatches() {
        LocalDate today = LocalDate.now();
        LocalDate nearExpiryLimit = today.plusDays(NEAR_EXPIRY_THRESHOLD_DAYS);
        return batchRepository.findBatchesExpiringBetween(today, nearExpiryLimit)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineBatchDto> getExpiredBatches() {
        return batchRepository.findExpiredBatches(LocalDate.now())
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public MedicineBatchDto createBatch(MedicineBatchDto dto) {
        if (dto.getExpiryDate() != null && dto.getManufacturingDate() != null
                && dto.getExpiryDate().isBefore(dto.getManufacturingDate())) {
            throw new BadRequestException("Expiry date cannot be before manufacturing date");
        }

        Medicine medicine = medicineRepository.findById(dto.getMedicineId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with ID: " + dto.getMedicineId()));

        Supplier supplier = null;
        if (dto.getSupplierId() != null) {
            supplier = supplierRepository.findById(dto.getSupplierId()).orElse(null);
        }

        String batchId = generateBatchId();
        int initialQty = dto.getQuantityReceived() != null ? dto.getQuantityReceived() : (dto.getQuantityAvailable() != null ? dto.getQuantityAvailable() : 0);

        BatchStatus initialStatus = evaluateBatchStatus(dto.getExpiryDate(), initialQty);

        MedicineBatch batch = MedicineBatch.builder()
                .batchId(batchId)
                .medicine(medicine)
                .supplier(supplier)
                .batchNumber(dto.getBatchNumber())
                .manufacturingDate(dto.getManufacturingDate())
                .expiryDate(dto.getExpiryDate())
                .purchaseRate(dto.getPurchaseRate())
                .mrp(dto.getMrp())
                .sellingRate(dto.getSellingRate())
                .quantityReceived(initialQty)
                .quantityAvailable(initialQty)
                .quantityReserved(0)
                .quantityDamaged(0)
                .quantityExpired(0)
                .storageLocation(dto.getStorageLocation())
                .status(initialStatus)
                .build();

        MedicineBatch saved = batchRepository.save(batch);

        auditLogService.logAction("CREATE_BATCH", "MedicineBatch", saved.getId().toString(),
                "Created batch: " + saved.getBatchNumber() + " for Medicine: " + medicine.getMedicineName());

        return mapToDto(saved);
    }

    @Transactional
    public MedicineBatchDto updateBatchStatus(Long id, BatchStatus newStatus) {
        MedicineBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with ID: " + id));

        batch.setStatus(newStatus);
        MedicineBatch saved = batchRepository.save(batch);

        auditLogService.logAction("UPDATE_BATCH_STATUS", "MedicineBatch", saved.getId().toString(),
                "Updated status to " + newStatus + " for Batch: " + saved.getBatchNumber());

        return mapToDto(saved);
    }

    public BatchStatus evaluateBatchStatus(LocalDate expiryDate, int availableQty) {
        if (availableQty <= 0) {
            return BatchStatus.DEPLETED;
        }
        LocalDate today = LocalDate.now();
        if (expiryDate.isBefore(today)) {
            return BatchStatus.EXPIRED;
        }
        if (!expiryDate.isAfter(today.plusDays(NEAR_EXPIRY_THRESHOLD_DAYS))) {
            return BatchStatus.NEAR_EXPIRY;
        }
        return BatchStatus.AVAILABLE;
    }

    private String generateBatchId() {
        long count = batchRepository.count() + 1;
        return String.format("BAT-%d-%06d", LocalDate.now().getYear(), count);
    }

    public MedicineBatchDto mapToDto(MedicineBatch b) {
        if (b == null) return null;
        return MedicineBatchDto.builder()
                .id(b.getId())
                .batchId(b.getBatchId())
                .medicineId(b.getMedicine().getId())
                .medicineCode(b.getMedicine().getMedicineCode())
                .medicineName(b.getMedicine().getMedicineName())
                .supplierId(b.getSupplier() != null ? b.getSupplier().getId() : null)
                .supplierName(b.getSupplier() != null ? b.getSupplier().getSupplierName() : null)
                .batchNumber(b.getBatchNumber())
                .manufacturingDate(b.getManufacturingDate())
                .expiryDate(b.getExpiryDate())
                .purchaseRate(b.getPurchaseRate())
                .mrp(b.getMrp())
                .sellingRate(b.getSellingRate())
                .quantityReceived(b.getQuantityReceived())
                .quantityAvailable(b.getQuantityAvailable())
                .quantityReserved(b.getQuantityReserved())
                .quantityDamaged(b.getQuantityDamaged())
                .quantityExpired(b.getQuantityExpired())
                .storageLocation(b.getStorageLocation())
                .status(b.getStatus())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
