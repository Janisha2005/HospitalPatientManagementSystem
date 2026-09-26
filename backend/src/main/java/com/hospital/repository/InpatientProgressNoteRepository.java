package com.hospital.repository;

import com.hospital.entity.InpatientProgressNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InpatientProgressNoteRepository extends JpaRepository<InpatientProgressNote, Long> {
    Optional<InpatientProgressNote> findByProgressNoteId(String progressNoteId);
    boolean existsByProgressNoteId(String progressNoteId);
    List<InpatientProgressNote> findByAdmissionIdOrderByNoteDatetimeDesc(Long admissionId);
    List<InpatientProgressNote> findByPatientIdOrderByNoteDatetimeDesc(Long patientId);
    Optional<InpatientProgressNote> findFirstByAdmissionIdOrderByNoteDatetimeDesc(Long admissionId);
}
