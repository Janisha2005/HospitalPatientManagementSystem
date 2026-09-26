package com.hospital.repository;

import com.hospital.entity.InpatientVital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InpatientVitalRepository extends JpaRepository<InpatientVital, Long> {
    List<InpatientVital> findByAdmissionIdOrderByRecordedAtDesc(Long admissionId);
    List<InpatientVital> findByPatientIdOrderByRecordedAtDesc(Long patientId);
    Optional<InpatientVital> findFirstByAdmissionIdOrderByRecordedAtDesc(Long admissionId);
}
