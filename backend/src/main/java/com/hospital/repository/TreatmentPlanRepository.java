package com.hospital.repository;

import com.hospital.entity.TreatmentPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, Long> {
    Optional<TreatmentPlan> findByTreatmentPlanId(String treatmentPlanId);
    List<TreatmentPlan> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    Page<TreatmentPlan> findByPatientId(Long patientId, Pageable pageable);
    List<TreatmentPlan> findByOpdVisitId(Long opdVisitId);
}
