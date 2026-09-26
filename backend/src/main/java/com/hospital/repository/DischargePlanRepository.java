package com.hospital.repository;

import com.hospital.entity.DischargePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DischargePlanRepository extends JpaRepository<DischargePlan, Long> {
    Optional<DischargePlan> findByDischargePlanId(String dischargePlanId);
    boolean existsByDischargePlanId(String dischargePlanId);
    Optional<DischargePlan> findByAdmissionId(Long admissionId);
    boolean existsByAdmissionId(Long admissionId);
}
