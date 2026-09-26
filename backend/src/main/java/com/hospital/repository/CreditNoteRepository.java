package com.hospital.repository;

import com.hospital.entity.CreditNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreditNoteRepository extends JpaRepository<CreditNote, Long> {
    Optional<CreditNote> findByCreditNoteNumber(String creditNoteNumber);
    List<CreditNote> findByBillId(Long billId);
    List<CreditNote> findByPatientId(Long patientId);
    Page<CreditNote> findByPatientId(Long patientId, Pageable pageable);
}
