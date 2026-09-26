package com.hospital.repository;

import com.hospital.entity.RadiologyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RadiologyReportRepository extends JpaRepository<RadiologyReport, Long> {
    Optional<RadiologyReport> findByReportId(String reportId);
    Optional<RadiologyReport> findByRadiologyOrderId(Long radiologyOrderId);
    List<RadiologyReport> findByPatientIdOrderByReportedAtDesc(Long patientId);
    long countByStatus(com.hospital.entity.RadiologyReportStatus status);
}
