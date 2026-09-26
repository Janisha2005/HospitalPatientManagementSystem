package com.hospital.repository;

import com.hospital.entity.BedAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BedAllocationRepository extends JpaRepository<BedAllocation, Long> {
    List<BedAllocation> findByAdmissionIdOrderByStartDatetimeDesc(Long admissionId);
    List<BedAllocation> findByPatientIdOrderByStartDatetimeDesc(Long patientId);
    Optional<BedAllocation> findFirstByAdmissionIdAndEndDatetimeIsNullOrderByStartDatetimeDesc(Long admissionId);
}
