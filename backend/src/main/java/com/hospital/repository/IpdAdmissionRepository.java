package com.hospital.repository;

import com.hospital.entity.AdmissionStatus;
import com.hospital.entity.IpdAdmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface IpdAdmissionRepository extends JpaRepository<IpdAdmission, Long>, JpaSpecificationExecutor<IpdAdmission> {
    Optional<IpdAdmission> findByAdmissionId(String admissionId);
    boolean existsByAdmissionId(String admissionId);

    List<IpdAdmission> findByPatientIdOrderByAdmissionDateDescAdmissionTimeDesc(Long patientId);
    List<IpdAdmission> findByAdmittingDoctorIdOrderByAdmissionDateDesc(Long doctorId);
    List<IpdAdmission> findByWardIdAndStatus(Long wardId, AdmissionStatus status);

    @Query("SELECT a FROM IpdAdmission a WHERE a.patient.id = :patientId AND a.status IN :activeStatuses")
    List<IpdAdmission> findActiveAdmissionsByPatientId(
            @Param("patientId") Long patientId,
            @Param("activeStatuses") Collection<AdmissionStatus> activeStatuses
    );

    boolean existsByWardId(Long wardId);
    boolean existsByBedId(Long bedId);

    long countByStatus(AdmissionStatus status);
    long countByStatusIn(Collection<AdmissionStatus> statuses);
    long countByAdmissionDate(LocalDate date);
    long countByActualDischargeDate(LocalDate date);
}
