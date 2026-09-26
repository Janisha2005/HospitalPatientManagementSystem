package com.hospital.repository;

import com.hospital.entity.ConditionStatus;
import com.hospital.entity.PatientCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientConditionRepository extends JpaRepository<PatientCondition, Long> {
    List<PatientCondition> findByPatientId(Long patientId);
    List<PatientCondition> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<PatientCondition> findByPatientIdAndStatus(Long patientId, ConditionStatus status);
}
