package com.hospital.repository;

import com.hospital.entity.DispensingStatus;
import com.hospital.entity.PharmacyDispensing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PharmacyDispensingRepository extends JpaRepository<PharmacyDispensing, Long> {
    Optional<PharmacyDispensing> findByDispensingId(String dispensingId);
    List<PharmacyDispensing> findByPrescriptionId(Long prescriptionId);
    List<PharmacyDispensing> findByPatientId(Long patientId);
    List<PharmacyDispensing> findByIpdAdmissionId(Long ipdAdmissionId);
    Page<PharmacyDispensing> findByStatus(DispensingStatus status, Pageable pageable);

    @Query("SELECT COUNT(d) FROM PharmacyDispensing d WHERE d.dispensedDatetime >= :startOfDay AND d.dispensedDatetime <= :endOfDay")
    long countDispensingsForDay(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
}
