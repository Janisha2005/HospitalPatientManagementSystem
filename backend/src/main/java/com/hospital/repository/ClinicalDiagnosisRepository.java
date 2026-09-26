package com.hospital.repository;

import com.hospital.entity.ClinicalDiagnosis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicalDiagnosisRepository extends JpaRepository<ClinicalDiagnosis, Long> {
    Optional<ClinicalDiagnosis> findByDiagnosisId(String diagnosisId);
    List<ClinicalDiagnosis> findByPatientIdOrderByDiagnosedAtDesc(Long patientId);
    Page<ClinicalDiagnosis> findByPatientId(Long patientId, Pageable pageable);
    List<ClinicalDiagnosis> findByOpdVisitId(Long opdVisitId);
}
