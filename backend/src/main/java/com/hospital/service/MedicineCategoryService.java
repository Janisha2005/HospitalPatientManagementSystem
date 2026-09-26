package com.hospital.service;

import com.hospital.dto.MedicineCategoryDto;
import com.hospital.entity.MedicineCategory;
import com.hospital.exception.DuplicateResourceException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.MedicineCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicineCategoryService {

    private final MedicineCategoryRepository categoryRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<MedicineCategoryDto> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<MedicineCategoryDto> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MedicineCategoryDto getCategoryById(Long id) {
        MedicineCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return mapToDto(cat);
    }

    @Transactional
    public MedicineCategoryDto createCategory(MedicineCategoryDto dto) {
        if (categoryRepository.existsByCategoryCode(dto.getCategoryCode())) {
            throw new DuplicateResourceException("Category code already exists: " + dto.getCategoryCode());
        }

        MedicineCategory category = MedicineCategory.builder()
                .categoryCode(dto.getCategoryCode())
                .categoryName(dto.getCategoryName())
                .description(dto.getDescription())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        MedicineCategory saved = categoryRepository.save(category);

        auditLogService.logAction("CREATE_MEDICINE_CATEGORY", "MedicineCategory", saved.getId().toString(),
                "Created category: " + saved.getCategoryName() + " (" + saved.getCategoryCode() + ")");

        return mapToDto(saved);
    }

    @Transactional
    public MedicineCategoryDto updateCategory(Long id, MedicineCategoryDto dto) {
        MedicineCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        if (dto.getCategoryName() != null) category.setCategoryName(dto.getCategoryName());
        if (dto.getDescription() != null) category.setDescription(dto.getDescription());
        if (dto.getIsActive() != null) category.setIsActive(dto.getIsActive());

        MedicineCategory saved = categoryRepository.save(category);

        auditLogService.logAction("UPDATE_MEDICINE_CATEGORY", "MedicineCategory", saved.getId().toString(),
                "Updated category Code: " + saved.getCategoryCode());

        return mapToDto(saved);
    }

    @Transactional
    public MedicineCategoryDto updateStatus(Long id, Boolean isActive) {
        MedicineCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        category.setIsActive(isActive);
        MedicineCategory saved = categoryRepository.save(category);

        auditLogService.logAction("UPDATE_MEDICINE_CATEGORY_STATUS", "MedicineCategory", saved.getId().toString(),
                "Updated active status to " + isActive + " for Category Code: " + saved.getCategoryCode());

        return mapToDto(saved);
    }

    public MedicineCategoryDto mapToDto(MedicineCategory cat) {
        if (cat == null) return null;
        return MedicineCategoryDto.builder()
                .id(cat.getId())
                .categoryCode(cat.getCategoryCode())
                .categoryName(cat.getCategoryName())
                .description(cat.getDescription())
                .isActive(cat.getIsActive())
                .createdAt(cat.getCreatedAt())
                .updatedAt(cat.getUpdatedAt())
                .build();
    }
}
