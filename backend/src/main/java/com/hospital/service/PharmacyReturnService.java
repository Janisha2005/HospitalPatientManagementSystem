package com.hospital.service;

import com.hospital.dto.*;
import com.hospital.entity.*;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.InsufficientStockException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PharmacyReturnService {

    private final PharmacyReturnRepository returnRepository;
    private final PharmacyReturnItemRepository returnItemRepository;
    private final PharmacyDispensingRepository dispensingRepository;
    private final PatientRepository patientRepository;
    private final SupplierRepository supplierRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository batchRepository;
    private final InventoryService inventoryService;
    private final BatchService batchService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<PharmacyReturnDto> getAllReturns(Pageable pageable) {
        return returnRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public PharmacyReturnDto getReturnById(Long id) {
        PharmacyReturn ret = returnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return record not found with ID: " + id));
        return mapToDto(ret);
    }

    @Transactional
    public PharmacyReturnDto processPatientReturn(PatientReturnRequest request, String username) {
        PharmacyDispensing dispensing = dispensingRepository.findById(request.getDispensingId())
                .orElseThrow(() -> new ResourceNotFoundException("Dispensing record not found with ID: " + request.getDispensingId()));

        if (request.getQuantityReturned() > dispensing.getDispensedQuantity()) {
            throw new BadRequestException("Returned quantity (" + request.getQuantityReturned() + ") cannot exceed dispensed quantity (" + dispensing.getDispensedQuantity() + ")");
        }

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + request.getPatientId()));

        MedicineBatch batch = batchRepository.findByIdForUpdate(request.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with ID: " + request.getBatchId()));

        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with ID: " + request.getMedicineId()));

        boolean restock = Boolean.TRUE.equals(request.getIsRestocked());

        if (restock) {
            batch.setQuantityAvailable(batch.getQuantityAvailable() + request.getQuantityReturned());
            batch.setStatus(batchService.evaluateBatchStatus(batch.getExpiryDate(), batch.getQuantityAvailable()));
            batchRepository.save(batch);

            inventoryService.recordTransaction(medicine, batch, InventoryTransactionType.RETURN_FROM_PATIENT,
                    request.getQuantityReturned(), batch.getSellingRate(), "PATIENT_RETURN",
                    dispensing.getDispensingId(), request.getReason(), username);
        } else {
            batch.setQuantityDamaged(batch.getQuantityDamaged() + request.getQuantityReturned());
            batchRepository.save(batch);

            inventoryService.recordTransaction(medicine, batch, InventoryTransactionType.DAMAGE,
                    request.getQuantityReturned(), batch.getSellingRate(), "PATIENT_RETURN_DAMAGED",
                    dispensing.getDispensingId(), request.getReason(), username);
        }

        BigDecimal unitRate = batch.getSellingRate() != null ? batch.getSellingRate() : BigDecimal.ZERO;
        BigDecimal refundAmount = unitRate.multiply(BigDecimal.valueOf(request.getQuantityReturned()));

        String returnId = generateReturnId();
        PharmacyReturn ret = PharmacyReturn.builder()
                .returnId(returnId)
                .returnType(ReturnType.PATIENT_RETURN)
                .patient(patient)
                .dispensing(dispensing)
                .returnDate(LocalDate.now())
                .reason(request.getReason())
                .refundAmount(refundAmount)
                .processedBy(username)
                .remarks(request.getRemarks())
                .build();

        PharmacyReturn savedRet = returnRepository.save(ret);

        PharmacyReturnItem item = PharmacyReturnItem.builder()
                .pharmacyReturn(savedRet)
                .medicine(medicine)
                .batch(batch)
                .quantityReturned(request.getQuantityReturned())
                .unitRate(unitRate)
                .isRestocked(restock)
                .lineTotal(refundAmount)
                .build();

        returnItemRepository.save(item);

        auditLogService.logAction("RETURN_FROM_PATIENT", "PharmacyReturn", savedRet.getId().toString(),
                "Processed patient return: " + request.getQuantityReturned() + " units for Patient: " + patient.getPatientId());

        return mapToDto(savedRet);
    }

    @Transactional
    public PharmacyReturnDto processSupplierReturn(SupplierReturnRequest request, String username) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + request.getSupplierId()));

        MedicineBatch batch = batchRepository.findByIdForUpdate(request.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with ID: " + request.getBatchId()));

        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with ID: " + request.getMedicineId()));

        if (batch.getQuantityAvailable() < request.getQuantityReturned()) {
            throw new InsufficientStockException("Cannot return " + request.getQuantityReturned() + " units to supplier. Available stock in batch is " + batch.getQuantityAvailable());
        }

        batch.setQuantityAvailable(batch.getQuantityAvailable() - request.getQuantityReturned());
        batch.setStatus(batchService.evaluateBatchStatus(batch.getExpiryDate(), batch.getQuantityAvailable()));
        batchRepository.save(batch);

        BigDecimal unitRate = batch.getPurchaseRate() != null ? batch.getPurchaseRate() : BigDecimal.ZERO;
        BigDecimal refundAmount = unitRate.multiply(BigDecimal.valueOf(request.getQuantityReturned()));

        inventoryService.recordTransaction(medicine, batch, InventoryTransactionType.RETURN_TO_SUPPLIER,
                request.getQuantityReturned(), unitRate, "SUPPLIER_RETURN",
                supplier.getSupplierCode(), request.getReason(), username);

        String returnId = generateReturnId();
        PharmacyReturn ret = PharmacyReturn.builder()
                .returnId(returnId)
                .returnType(ReturnType.SUPPLIER_RETURN)
                .supplier(supplier)
                .returnDate(LocalDate.now())
                .reason(request.getReason())
                .refundAmount(refundAmount)
                .processedBy(username)
                .remarks(request.getRemarks())
                .build();

        PharmacyReturn savedRet = returnRepository.save(ret);

        PharmacyReturnItem item = PharmacyReturnItem.builder()
                .pharmacyReturn(savedRet)
                .medicine(medicine)
                .batch(batch)
                .quantityReturned(request.getQuantityReturned())
                .unitRate(unitRate)
                .isRestocked(false)
                .lineTotal(refundAmount)
                .build();

        returnItemRepository.save(item);

        auditLogService.logAction("RETURN_TO_SUPPLIER", "PharmacyReturn", savedRet.getId().toString(),
                "Processed supplier return: " + request.getQuantityReturned() + " units for Supplier: " + supplier.getSupplierName());

        return mapToDto(savedRet);
    }

    private String generateReturnId() {
        long count = returnRepository.count() + 1;
        return String.format("RET-%d-%06d", LocalDate.now().getYear(), count);
    }

    public PharmacyReturnDto mapToDto(PharmacyReturn r) {
        if (r == null) return null;

        List<PharmacyReturnItem> items = returnItemRepository.findByPharmacyReturnId(r.getId());
        List<PharmacyReturnItemDto> itemDtos = items.stream().map(i -> PharmacyReturnItemDto.builder()
                .id(i.getId())
                .medicineId(i.getMedicine().getId())
                .medicineName(i.getMedicine().getMedicineName())
                .batchId(i.getBatch().getId())
                .batchNumber(i.getBatch().getBatchNumber())
                .quantityReturned(i.getQuantityReturned())
                .unitRate(i.getUnitRate())
                .isRestocked(i.getIsRestocked())
                .lineTotal(i.getLineTotal())
                .build()).collect(Collectors.toList());

        return PharmacyReturnDto.builder()
                .id(r.getId())
                .returnId(r.getReturnId())
                .returnType(r.getReturnType())
                .patientId(r.getPatient() != null ? r.getPatient().getId() : null)
                .patientName(r.getPatient() != null ? r.getPatient().getFirstName() + " " + r.getPatient().getLastName() : null)
                .supplierId(r.getSupplier() != null ? r.getSupplier().getId() : null)
                .supplierName(r.getSupplier() != null ? r.getSupplier().getSupplierName() : null)
                .dispensingId(r.getDispensing() != null ? r.getDispensing().getId() : null)
                .returnDate(r.getReturnDate())
                .reason(r.getReason())
                .refundAmount(r.getRefundAmount())
                .processedBy(r.getProcessedBy())
                .remarks(r.getRemarks())
                .items(itemDtos)
                .createdAt(r.getCreatedAt())
                .build();
    }
}
