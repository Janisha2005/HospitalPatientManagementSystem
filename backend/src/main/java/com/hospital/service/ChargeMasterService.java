package com.hospital.service;

import com.hospital.dto.ChargeMasterDto;
import com.hospital.entity.ChargeCategory;
import com.hospital.entity.ChargeMaster;
import com.hospital.entity.Department;
import com.hospital.exception.DuplicateResourceException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.ChargeMasterRepository;
import com.hospital.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChargeMasterService {

    private final ChargeMasterRepository chargeMasterRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<ChargeMasterDto> getAllCharges(Pageable pageable) {
        return chargeMasterRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<ChargeMasterDto> searchCharges(String query, Pageable pageable) {
        return chargeMasterRepository.searchCharges(query, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<ChargeMasterDto> getActiveChargesByCategory(ChargeCategory category) {
        return chargeMasterRepository.findByChargeCategoryAndIsActiveTrue(category)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChargeMasterDto getChargeById(Long id) {
        ChargeMaster charge = chargeMasterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Charge master entry not found with ID: " + id));
        return mapToDto(charge);
    }

    @Transactional
    public ChargeMasterDto createCharge(ChargeMasterDto dto, String username) {
        String code = dto.getChargeCode();
        if (code == null || code.isBlank()) {
            code = generateChargeCode();
        } else if (chargeMasterRepository.existsByChargeCode(code)) {
            throw new DuplicateResourceException("Charge code already exists: " + code);
        }

        Department dept = null;
        if (dto.getDepartmentId() != null) {
            dept = departmentRepository.findById(dto.getDepartmentId()).orElse(null);
        }

        ChargeMaster charge = ChargeMaster.builder()
                .chargeCode(code)
                .chargeName(dto.getChargeName())
                .chargeCategory(dto.getChargeCategory())
                .description(dto.getDescription())
                .department(dept)
                .unit(dto.getUnit() != null ? dto.getUnit() : "unit")
                .baseRate(dto.getBaseRate())
                .taxPercentage(dto.getTaxPercentage() != null ? dto.getTaxPercentage() : BigDecimal.ZERO)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        ChargeMaster saved = chargeMasterRepository.save(charge);
        auditLogService.logAction("CREATE_CHARGE_MASTER", "ChargeMaster", saved.getId().toString(),
                "Created charge entry: " + saved.getChargeName() + " (" + saved.getChargeCode() + ")");

        return mapToDto(saved);
    }

    @Transactional
    public ChargeMasterDto updateCharge(Long id, ChargeMasterDto dto, String username) {
        ChargeMaster charge = chargeMasterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Charge master entry not found with ID: " + id));

        Department dept = null;
        if (dto.getDepartmentId() != null) {
            dept = departmentRepository.findById(dto.getDepartmentId()).orElse(null);
        }

        charge.setChargeName(dto.getChargeName());
        charge.setChargeCategory(dto.getChargeCategory());
        charge.setDescription(dto.getDescription());
        charge.setDepartment(dept);
        if (dto.getUnit() != null) charge.setUnit(dto.getUnit());
        charge.setBaseRate(dto.getBaseRate());
        if (dto.getTaxPercentage() != null) charge.setTaxPercentage(dto.getTaxPercentage());
        if (dto.getIsActive() != null) charge.setIsActive(dto.getIsActive());

        ChargeMaster saved = chargeMasterRepository.save(charge);
        auditLogService.logAction("UPDATE_CHARGE_MASTER", "ChargeMaster", saved.getId().toString(),
                "Updated charge entry: " + saved.getChargeName());

        return mapToDto(saved);
    }

    @Transactional
    public ChargeMasterDto updateChargeStatus(Long id, boolean active, String username) {
        ChargeMaster charge = chargeMasterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Charge master entry not found with ID: " + id));
        charge.setIsActive(active);
        ChargeMaster saved = chargeMasterRepository.save(charge);
        auditLogService.logAction("UPDATE_CHARGE_MASTER_STATUS", "ChargeMaster", saved.getId().toString(),
                "Updated status to " + active + " for charge: " + saved.getChargeCode());
        return mapToDto(saved);
    }

    private String generateChargeCode() {
        long count = chargeMasterRepository.count() + 1;
        return String.format("CHG-%d-%06d", LocalDate.now().getYear(), count);
    }

    public ChargeMasterDto mapToDto(ChargeMaster c) {
        if (c == null) return null;
        return ChargeMasterDto.builder()
                .id(c.getId())
                .chargeCode(c.getChargeCode())
                .chargeName(c.getChargeName())
                .chargeCategory(c.getChargeCategory())
                .description(c.getDescription())
                .departmentId(c.getDepartment() != null ? c.getDepartment().getId() : null)
                .departmentName(c.getDepartment() != null ? c.getDepartment().getDepartmentName() : null)
                .unit(c.getUnit())
                .baseRate(c.getBaseRate())
                .taxPercentage(c.getTaxPercentage())
                .isActive(c.getIsActive())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
