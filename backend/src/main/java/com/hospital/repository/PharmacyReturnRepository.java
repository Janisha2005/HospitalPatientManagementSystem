package com.hospital.repository;

import com.hospital.entity.PharmacyReturn;
import com.hospital.entity.ReturnType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PharmacyReturnRepository extends JpaRepository<PharmacyReturn, Long> {
    Optional<PharmacyReturn> findByReturnId(String returnId);
    Page<PharmacyReturn> findByReturnType(ReturnType returnType, Pageable pageable);
    Page<PharmacyReturn> findByPatientId(Long patientId, Pageable pageable);
    Page<PharmacyReturn> findBySupplierId(Long supplierId, Pageable pageable);
}
