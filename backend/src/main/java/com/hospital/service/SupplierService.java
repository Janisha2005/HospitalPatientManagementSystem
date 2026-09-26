package com.hospital.service;

import com.hospital.dto.SupplierDto;
import com.hospital.entity.Supplier;
import com.hospital.exception.DuplicateResourceException;
import com.hospital.exception.ResourceNotFoundException;
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
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<SupplierDto> getAllSuppliers(Pageable pageable) {
        return supplierRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<SupplierDto> searchSuppliers(String query, Pageable pageable) {
        return supplierRepository.searchSuppliers(query, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<SupplierDto> getActiveSuppliers() {
        return supplierRepository.findByIsActiveTrue().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupplierDto getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id));
        return mapToDto(supplier);
    }

    @Transactional
    public SupplierDto createSupplier(SupplierDto dto) {
        if (dto.getGstNumber() != null && !dto.getGstNumber().isBlank() && supplierRepository.existsByGstNumber(dto.getGstNumber())) {
            throw new DuplicateResourceException("GST Number already exists: " + dto.getGstNumber());
        }
        if (dto.getDrugLicenseNumber() != null && !dto.getDrugLicenseNumber().isBlank() && supplierRepository.existsByDrugLicenseNumber(dto.getDrugLicenseNumber())) {
            throw new DuplicateResourceException("Drug License Number already exists: " + dto.getDrugLicenseNumber());
        }

        String supplierCode = generateSupplierCode();

        Supplier supplier = Supplier.builder()
                .supplierCode(supplierCode)
                .supplierName(dto.getSupplierName())
                .contactPerson(dto.getContactPerson())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .addressLine1(dto.getAddressLine1())
                .addressLine2(dto.getAddressLine2())
                .city(dto.getCity())
                .district(dto.getDistrict())
                .state(dto.getState())
                .pincode(dto.getPincode())
                .gstNumber(dto.getGstNumber())
                .drugLicenseNumber(dto.getDrugLicenseNumber())
                .paymentTerms(dto.getPaymentTerms())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        Supplier saved = supplierRepository.save(supplier);

        auditLogService.logAction("CREATE_SUPPLIER", "Supplier", saved.getId().toString(),
                "Created supplier: " + saved.getSupplierName() + " (" + saved.getSupplierCode() + ")");

        return mapToDto(saved);
    }

    @Transactional
    public SupplierDto updateSupplier(Long id, SupplierDto dto) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id));

        if (dto.getSupplierName() != null) supplier.setSupplierName(dto.getSupplierName());
        if (dto.getContactPerson() != null) supplier.setContactPerson(dto.getContactPerson());
        if (dto.getPhone() != null) supplier.setPhone(dto.getPhone());
        if (dto.getEmail() != null) supplier.setEmail(dto.getEmail());
        if (dto.getAddressLine1() != null) supplier.setAddressLine1(dto.getAddressLine1());
        if (dto.getAddressLine2() != null) supplier.setAddressLine2(dto.getAddressLine2());
        if (dto.getCity() != null) supplier.setCity(dto.getCity());
        if (dto.getDistrict() != null) supplier.setDistrict(dto.getDistrict());
        if (dto.getState() != null) supplier.setState(dto.getState());
        if (dto.getPincode() != null) supplier.setPincode(dto.getPincode());
        if (dto.getGstNumber() != null) supplier.setGstNumber(dto.getGstNumber());
        if (dto.getDrugLicenseNumber() != null) supplier.setDrugLicenseNumber(dto.getDrugLicenseNumber());
        if (dto.getPaymentTerms() != null) supplier.setPaymentTerms(dto.getPaymentTerms());
        if (dto.getIsActive() != null) supplier.setIsActive(dto.getIsActive());

        Supplier saved = supplierRepository.save(supplier);

        auditLogService.logAction("UPDATE_SUPPLIER", "Supplier", saved.getId().toString(),
                "Updated supplier: " + saved.getSupplierCode());

        return mapToDto(saved);
    }

    @Transactional
    public SupplierDto updateStatus(Long id, Boolean isActive) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id));

        supplier.setIsActive(isActive);
        Supplier saved = supplierRepository.save(supplier);

        auditLogService.logAction("UPDATE_SUPPLIER_STATUS", "Supplier", saved.getId().toString(),
                "Updated active status to " + isActive + " for Supplier Code: " + saved.getSupplierCode());

        return mapToDto(saved);
    }

    private String generateSupplierCode() {
        long count = supplierRepository.count() + 1;
        return String.format("SUP-%d-%06d", LocalDate.now().getYear(), count);
    }

    public SupplierDto mapToDto(Supplier s) {
        if (s == null) return null;
        return SupplierDto.builder()
                .id(s.getId())
                .supplierCode(s.getSupplierCode())
                .supplierName(s.getSupplierName())
                .contactPerson(s.getContactPerson())
                .phone(s.getPhone())
                .email(s.getEmail())
                .addressLine1(s.getAddressLine1())
                .addressLine2(s.getAddressLine2())
                .city(s.getCity())
                .district(s.getDistrict())
                .state(s.getState())
                .pincode(s.getPincode())
                .gstNumber(s.getGstNumber())
                .drugLicenseNumber(s.getDrugLicenseNumber())
                .paymentTerms(s.getPaymentTerms())
                .isActive(s.getIsActive())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
