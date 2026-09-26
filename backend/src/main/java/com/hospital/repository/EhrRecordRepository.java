package com.hospital.repository;

import com.hospital.entity.EhrRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EhrRecordRepository extends JpaRepository<EhrRecord, Long> {
    Optional<EhrRecord> findByEhrRecordId(String ehrRecordId);
    Page<EhrRecord> findByPatientId(Long patientId, Pageable pageable);
    List<EhrRecord> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    @Query("SELECT e FROM EhrRecord e WHERE " +
           "(:patientId IS NULL OR e.patient.id = :patientId) AND " +
           "(:doctorId IS NULL OR e.doctor.id = :doctorId) AND " +
           "(:departmentId IS NULL OR e.department.id = :departmentId)")
    Page<EhrRecord> findRecordsWithFilters(
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId,
            @Param("departmentId") Long departmentId,
            Pageable pageable
    );
}
