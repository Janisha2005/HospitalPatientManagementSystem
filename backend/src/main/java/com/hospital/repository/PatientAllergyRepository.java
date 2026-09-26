package com.hospital.repository;

import com.hospital.entity.AllergyStatus;
import com.hospital.entity.PatientAllergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientAllergyRepository extends JpaRepository<PatientAllergy, Long> {
    List<PatientAllergy> findByPatientId(Long patientId);
    List<PatientAllergy> findByPatientIdOrderByRecordedAtDesc(Long patientId);
    List<PatientAllergy> findByPatientIdAndStatus(Long patientId, AllergyStatus status);
}
