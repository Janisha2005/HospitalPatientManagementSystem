package com.hospital.repository;

import com.hospital.entity.Prescription;
import com.hospital.entity.PrescriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Optional<Prescription> findByPrescriptionId(String prescriptionId);
    List<Prescription> findByPatientIdOrderByPrescriptionDateDesc(Long patientId);

    @Query("SELECT p FROM Prescription p WHERE " +
           "(:patientId IS NULL OR p.patient.id = :patientId) AND " +
           "(:doctorId IS NULL OR p.doctor.id = :doctorId) AND " +
           "(:date IS NULL OR p.prescriptionDate = :date) AND " +
           "(:status IS NULL OR p.status = :status)")
    Page<Prescription> findPrescriptionsWithFilters(
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("status") PrescriptionStatus status,
            Pageable pageable
    );

    long countByPrescriptionDate(LocalDate date);
    long countByStatus(PrescriptionStatus status);
}
