package com.hospital.repository;

import com.hospital.entity.PatientLedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientLedgerEntryRepository extends JpaRepository<PatientLedgerEntry, Long> {
    Optional<PatientLedgerEntry> findByLedgerNumber(String ledgerNumber);
    List<PatientLedgerEntry> findByPatientIdOrderByEntryDateAsc(Long patientId);
    Page<PatientLedgerEntry> findByPatientId(Long patientId, Pageable pageable);
}
