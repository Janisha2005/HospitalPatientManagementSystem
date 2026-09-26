package com.hospital.repository;

import com.hospital.entity.NursingNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NursingNoteRepository extends JpaRepository<NursingNote, Long> {
    Optional<NursingNote> findByNoteId(String noteId);
    boolean existsByNoteId(String noteId);
    List<NursingNote> findByAdmissionIdOrderByNoteDatetimeDesc(Long admissionId);
    List<NursingNote> findByPatientIdOrderByNoteDatetimeDesc(Long patientId);
    Optional<NursingNote> findFirstByAdmissionIdOrderByNoteDatetimeDesc(Long admissionId);
}
