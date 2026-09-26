package com.hospital.service;

import com.hospital.dto.MedicineCategoryDto;
import com.hospital.dto.MedicineRequest;
import com.hospital.dto.MedicineResponse;
import com.hospital.entity.Medicine;
import com.hospital.entity.MedicineCategory;
import com.hospital.exception.DuplicateResourceException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.MedicineBatchRepository;
import com.hospital.repository.MedicineCategoryRepository;
import com.hospital.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final MedicineCategoryRepository categoryRepository;
    private final MedicineBatchRepository batchRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<MedicineResponse> getAllMedicines(Pageable pageable) {
        return medicineRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<MedicineResponse> getActiveMedicines() {
        return medicineRepository.findByIsActiveTrue().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MedicineResponse getMedicineById(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with ID: " + id));
        return mapToResponse(medicine);
    }

    @Transactional
    public MedicineResponse createMedicine(MedicineRequest request) {
        if (medicineRepository.existsByMedicineCode(request.getMedicineCode())) {
            throw new DuplicateResourceException("Medicine code already exists: " + request.getMedicineCode());
        }

        MedicineCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));
        }

        Medicine medicine = Medicine.builder()
                .medicineCode(request.getMedicineCode())
                .medicineName(request.getMedicineName())
                .genericName(request.getGenericName())
                .strength(request.getStrength())
                .dosageForm(request.getDosageForm())
                .manufacturer(request.getManufacturer())
                .category(category)
                .unit(request.getUnit() != null ? request.getUnit() : "Tablet")
                .reorderLevel(request.getReorderLevel() != null ? request.getReorderLevel() : 50)
                .maximumStockLevel(request.getMaximumStockLevel() != null ? request.getMaximumStockLevel() : 500)
                .isPrescriptionRequired(request.getIsPrescriptionRequired() != null ? request.getIsPrescriptionRequired() : true)
                .isControlled(request.getIsControlled() != null ? request.getIsControlled() : false)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Medicine saved = medicineRepository.save(medicine);

        auditLogService.logAction("CREATE_MEDICINE", "Medicine", saved.getId().toString(),
                "Added medicine to catalog: " + saved.getMedicineName() + " (" + saved.getMedicineCode() + ")");

        return mapToResponse(saved);
    }

    @Transactional
    public MedicineResponse updateMedicine(Long id, MedicineRequest request) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with ID: " + id));

        if (request.getMedicineName() != null) medicine.setMedicineName(request.getMedicineName());
        if (request.getGenericName() != null) medicine.setGenericName(request.getGenericName());
        if (request.getStrength() != null) medicine.setStrength(request.getStrength());
        if (request.getDosageForm() != null) medicine.setDosageForm(request.getDosageForm());
        if (request.getManufacturer() != null) medicine.setManufacturer(request.getManufacturer());
        if (request.getUnit() != null) medicine.setUnit(request.getUnit());
        if (request.getReorderLevel() != null) medicine.setReorderLevel(request.getReorderLevel());
        if (request.getMaximumStockLevel() != null) medicine.setMaximumStockLevel(request.getMaximumStockLevel());
        if (request.getIsPrescriptionRequired() != null) medicine.setIsPrescriptionRequired(request.getIsPrescriptionRequired());
        if (request.getIsControlled() != null) medicine.setIsControlled(request.getIsControlled());
        if (request.getIsActive() != null) medicine.setIsActive(request.getIsActive());

        if (request.getCategoryId() != null) {
            MedicineCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));
            medicine.setCategory(category);
        }

        Medicine saved = medicineRepository.save(medicine);

        auditLogService.logAction("UPDATE_MEDICINE", "Medicine", saved.getId().toString(),
                "Updated medicine details for Code: " + saved.getMedicineCode());

        return mapToResponse(saved);
    }

    @Transactional
    public MedicineResponse updateStatus(Long id, Boolean isActive) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with ID: " + id));

        medicine.setIsActive(isActive);
        Medicine saved = medicineRepository.save(medicine);

        auditLogService.logAction("UPDATE_MEDICINE_STATUS", "Medicine", saved.getId().toString(),
                "Updated active status to " + isActive + " for Medicine Code: " + saved.getMedicineCode());

        return mapToResponse(saved);
    }

    public MedicineResponse mapToResponse(Medicine m) {
        if (m == null) return null;

        MedicineCategoryDto catDto = null;
        if (m.getCategory() != null) {
            catDto = MedicineCategoryDto.builder()
                    .id(m.getCategory().getId())
                    .categoryCode(m.getCategory().getCategoryCode())
                    .categoryName(m.getCategory().getCategoryName())
                    .description(m.getCategory().getDescription())
                    .isActive(m.getCategory().getIsActive())
                    .build();
        }

        Integer availableStock = batchRepository.getTotalAvailableQuantityForMedicine(m.getId());
        if (availableStock == null) availableStock = 0;
        int reorder = m.getReorderLevel() != null ? m.getReorderLevel() : 50;

        return MedicineResponse.builder()
                .id(m.getId())
                .medicineCode(m.getMedicineCode())
                .medicineName(m.getMedicineName())
                .genericName(m.getGenericName())
                .strength(m.getStrength())
                .dosageForm(m.getDosageForm())
                .manufacturer(m.getManufacturer())
                .category(catDto)
                .unit(m.getUnit() != null ? m.getUnit() : "Tablet")
                .reorderLevel(reorder)
                .maximumStockLevel(m.getMaximumStockLevel() != null ? m.getMaximumStockLevel() : 500)
                .isPrescriptionRequired(m.getIsPrescriptionRequired() != null ? m.getIsPrescriptionRequired() : true)
                .isControlled(m.getIsControlled() != null ? m.getIsControlled() : false)
                .availableStock(availableStock)
                .isLowStock(availableStock <= reorder)
                .isActive(m.getIsActive())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
