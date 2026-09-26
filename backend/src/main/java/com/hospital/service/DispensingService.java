package com.hospital.service;

import com.hospital.dto.DispenseRequest;
import com.hospital.dto.PharmacyDispensingDto;
import com.hospital.entity.*;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ExpiredBatchException;
import com.hospital.exception.InsufficientStockException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DispensingService {

    private final PharmacyDispensingRepository dispensingRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository batchRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final IpdAdmissionRepository ipdAdmissionRepository;
    private final InventoryService inventoryService;
    private final BatchService batchService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<PharmacyDispensingDto> getAllDispensings(Pageable pageable) {
        return dispensingRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public PharmacyDispensingDto getDispensingById(Long id) {
        PharmacyDispensing d = dispensingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispensing record not found with ID: " + id));
        return mapToDto(d);
    }

    @Transactional(readOnly = true)
    public List<PharmacyDispensingDto> getDispensingsByPrescriptionId(Long prescriptionId) {
        return dispensingRepository.findByPrescriptionId(prescriptionId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PharmacyDispensingDto> getDispensingsByPatientId(Long patientId) {
        return dispensingRepository.findByPatientId(patientId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public List<PharmacyDispensingDto> dispenseMedicine(DispenseRequest request, String username) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + request.getPatientId()));

        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with ID: " + request.getMedicineId()));

        Prescription prescription = null;
        if (request.getPrescriptionId() != null) {
            prescription = prescriptionRepository.findById(request.getPrescriptionId()).orElse(null);
        }

        PrescriptionItem prescriptionItem = null;
        if (prescription != null && request.getPrescriptionItemId() != null && prescription.getItems() != null) {
            prescriptionItem = prescription.getItems().stream()
                    .filter(item -> item.getId().equals(request.getPrescriptionItemId()))
                    .findFirst().orElse(null);
        }

        Doctor doctor = null;
        if (request.getDoctorId() != null) {
            doctor = doctorRepository.findById(request.getDoctorId()).orElse(null);
        } else if (prescription != null) {
            doctor = prescription.getDoctor();
        }

        IpdAdmission ipdAdmission = null;
        if (request.getIpdAdmissionId() != null) {
            ipdAdmission = ipdAdmissionRepository.findById(request.getIpdAdmissionId()).orElse(null);
        }

        int qtyToDispense = request.getDispensedQuantity();
        int prescribedQty = request.getPrescribedQuantity();

        if (qtyToDispense <= 0) {
            throw new BadRequestException("Dispensed quantity must be positive");
        }

        List<PharmacyDispensing> createdDispensings = new ArrayList<>();

        if (request.getBatchId() != null) {
            // Specific batch selected by pharmacist
            MedicineBatch batch = batchRepository.findByIdForUpdate(request.getBatchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Batch not found with ID: " + request.getBatchId()));

            validateBatchForDispensing(batch);

            if (batch.getQuantityAvailable() < qtyToDispense) {
                throw new InsufficientStockException("Insufficient stock in batch " + batch.getBatchNumber() + ". Requested: " + qtyToDispense + ", Available: " + batch.getQuantityAvailable());
            }

            // Consume from batch
            batch.setQuantityAvailable(batch.getQuantityAvailable() - qtyToDispense);
            batch.setStatus(batchService.evaluateBatchStatus(batch.getExpiryDate(), batch.getQuantityAvailable()));
            batchRepository.save(batch);

            // Record inventory transaction
            inventoryService.recordTransaction(medicine, batch, InventoryTransactionType.DISPENSING,
                    qtyToDispense, batch.getSellingRate(), "PHARMACY_DISPENSING",
                    prescription != null ? prescription.getPrescriptionId() : "DIRECT_DSP",
                    request.getRemarks(), username);

            int remaining = Math.max(0, prescribedQty - qtyToDispense);
            DispensingStatus status = remaining == 0 ? DispensingStatus.FULLY_DISPENSED : DispensingStatus.PARTIALLY_DISPENSED;

            PharmacyDispensing dispensing = PharmacyDispensing.builder()
                    .dispensingId(generateDispensingId())
                    .prescription(prescription)
                    .prescriptionItem(prescriptionItem)
                    .patient(patient)
                    .doctor(doctor)
                    .medicine(medicine)
                    .batch(batch)
                    .ipdAdmission(ipdAdmission)
                    .prescribedQuantity(prescribedQty)
                    .dispensedQuantity(qtyToDispense)
                    .remainingQuantity(remaining)
                    .dispensedBy(username)
                    .dispensedDatetime(LocalDateTime.now())
                    .status(status)
                    .remarks(request.getRemarks())
                    .build();

            createdDispensings.add(dispensingRepository.save(dispensing));
        } else {
            // FEFO Algorithm — First Expiry, First Out
            LocalDate today = LocalDate.now();
            List<MedicineBatch> fefoBatches = batchRepository.findFefoBatchesForMedicine(medicine.getId(), today);

            int totalAvailable = fefoBatches.stream().mapToInt(MedicineBatch::getQuantityAvailable).sum();
            if (totalAvailable < qtyToDispense) {
                throw new InsufficientStockException("Insufficient total available stock for " + medicine.getMedicineName() + ". Requested: " + qtyToDispense + ", Available: " + totalAvailable);
            }

            int remainingToFulfill = qtyToDispense;

            for (MedicineBatch fefoBatch : fefoBatches) {
                if (remainingToFulfill <= 0) break;

                MedicineBatch lockedBatch = batchRepository.findByIdForUpdate(fefoBatch.getId()).orElse(fefoBatch);
                validateBatchForDispensing(lockedBatch);

                int takeFromBatch = Math.min(lockedBatch.getQuantityAvailable(), remainingToFulfill);
                if (takeFromBatch <= 0) continue;

                lockedBatch.setQuantityAvailable(lockedBatch.getQuantityAvailable() - takeFromBatch);
                lockedBatch.setStatus(batchService.evaluateBatchStatus(lockedBatch.getExpiryDate(), lockedBatch.getQuantityAvailable()));
                batchRepository.save(lockedBatch);

                inventoryService.recordTransaction(medicine, lockedBatch, InventoryTransactionType.DISPENSING,
                        takeFromBatch, lockedBatch.getSellingRate(), "PHARMACY_DISPENSING",
                        prescription != null ? prescription.getPrescriptionId() : "FEFO_DSP",
                        request.getRemarks(), username);

                remainingToFulfill -= takeFromBatch;
                int remainingForPrescription = Math.max(0, prescribedQty - (qtyToDispense - remainingToFulfill));
                DispensingStatus status = remainingForPrescription == 0 ? DispensingStatus.FULLY_DISPENSED : DispensingStatus.PARTIALLY_DISPENSED;

                PharmacyDispensing dispensing = PharmacyDispensing.builder()
                        .dispensingId(generateDispensingId())
                        .prescription(prescription)
                        .prescriptionItem(prescriptionItem)
                        .patient(patient)
                        .doctor(doctor)
                        .medicine(medicine)
                        .batch(lockedBatch)
                        .ipdAdmission(ipdAdmission)
                        .prescribedQuantity(prescribedQty)
                        .dispensedQuantity(takeFromBatch)
                        .remainingQuantity(remainingForPrescription)
                        .dispensedBy(username)
                        .dispensedDatetime(LocalDateTime.now())
                        .status(status)
                        .remarks(request.getRemarks())
                        .build();

                createdDispensings.add(dispensingRepository.save(dispensing));
            }
        }

        // Update Prescription Status if all items dispensed
        if (prescription != null) {
            prescription.setStatus(PrescriptionStatus.COMPLETED);
            prescriptionRepository.save(prescription);
        }

        auditLogService.logAction("DISPENSE_MEDICINE", "PharmacyDispensing",
                createdDispensings.get(0).getId().toString(),
                "Dispensed " + qtyToDispense + " units of " + medicine.getMedicineName() + " for Patient: " + patient.getPatientId());

        return createdDispensings.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private void validateBatchForDispensing(MedicineBatch batch) {
        if (batch.getExpiryDate().isBefore(LocalDate.now()) || batch.getStatus() == BatchStatus.EXPIRED) {
            throw new ExpiredBatchException("Cannot dispense from expired batch: " + batch.getBatchNumber() + " (Expired: " + batch.getExpiryDate() + ")");
        }
        if (batch.getStatus() == BatchStatus.BLOCKED) {
            throw new BadRequestException("Cannot dispense from blocked batch: " + batch.getBatchNumber());
        }
        if (batch.getStatus() == BatchStatus.DEPLETED || batch.getQuantityAvailable() <= 0) {
            throw new InsufficientStockException("Batch " + batch.getBatchNumber() + " is depleted.");
        }
    }

    private String generateDispensingId() {
        long count = dispensingRepository.count() + 1;
        return String.format("DSP-%d-%06d", LocalDate.now().getYear(), count);
    }

    public PharmacyDispensingDto mapToDto(PharmacyDispensing d) {
        if (d == null) return null;
        return PharmacyDispensingDto.builder()
                .id(d.getId())
                .dispensingId(d.getDispensingId())
                .prescriptionId(d.getPrescription() != null ? d.getPrescription().getId() : null)
                .prescriptionItemId(d.getPrescriptionItem() != null ? d.getPrescriptionItem().getId() : null)
                .patientId(d.getPatient().getId())
                .patientName(d.getPatient().getFirstName() + " " + d.getPatient().getLastName())
                .patientIdCode(d.getPatient().getPatientId())
                .doctorId(d.getDoctor() != null ? d.getDoctor().getId() : null)
                .doctorName(d.getDoctor() != null ? "Dr. " + d.getDoctor().getFirstName() + " " + d.getDoctor().getLastName() : null)
                .medicineId(d.getMedicine().getId())
                .medicineName(d.getMedicine().getMedicineName())
                .batchId(d.getBatch().getId())
                .batchNumber(d.getBatch().getBatchNumber())
                .ipdAdmissionId(d.getIpdAdmission() != null ? d.getIpdAdmission().getId() : null)
                .prescribedQuantity(d.getPrescribedQuantity())
                .dispensedQuantity(d.getDispensedQuantity())
                .remainingQuantity(d.getRemainingQuantity())
                .dispensedBy(d.getDispensedBy())
                .dispensedDatetime(d.getDispensedDatetime())
                .status(d.getStatus())
                .remarks(d.getRemarks())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
