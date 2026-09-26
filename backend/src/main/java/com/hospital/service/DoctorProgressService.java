package com.hospital.service;

import com.hospital.dto.InpatientProgressNoteRequest;
import com.hospital.dto.InpatientProgressNoteResponse;
import com.hospital.entity.Doctor;
import com.hospital.entity.InpatientProgressNote;
import com.hospital.entity.IpdAdmission;
import com.hospital.entity.Patient;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.InpatientProgressNoteRepository;
import com.hospital.repository.IpdAdmissionRepository;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorProgressService {

    private final InpatientProgressNoteRepository progressNoteRepository;
    private final IpdAdmissionRepository ipdAdmissionRepository;
    private final DoctorRepository doctorRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<InpatientProgressNoteResponse> getProgressNotesForAdmission(Long admissionId) {
        return progressNoteRepository.findByAdmissionIdOrderByNoteDatetimeDesc(admissionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public InpatientProgressNoteResponse createProgressNote(Long admissionId, InpatientProgressNoteRequest request) {
        IpdAdmission admission = ipdAdmissionRepository.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found with ID: " + admissionId));

        Doctor doctor = getCurrentDoctor();
        if (doctor == null) {
            doctor = admission.getAdmittingDoctor();
        }

        String noteId = generateProgressNoteId();

        InpatientProgressNote note = InpatientProgressNote.builder()
                .progressNoteId(noteId)
                .admission(admission)
                .patient(admission.getPatient())
                .doctor(doctor)
                .noteDatetime(request.getNoteDatetime() != null ? request.getNoteDatetime() : LocalDateTime.now())
                .clinicalAssessment(request.getClinicalAssessment())
                .progressSummary(request.getProgressSummary())
                .diagnosisUpdate(request.getDiagnosisUpdate())
                .treatmentUpdate(request.getTreatmentUpdate())
                .followUpPlan(request.getFollowUpPlan())
                .build();

        InpatientProgressNote saved = progressNoteRepository.save(note);
        auditLogService.logAction("CREATE_PROGRESS_NOTE", "PROGRESS_NOTE", saved.getProgressNoteId(), "Recorded doctor progress note for admission " + admission.getAdmissionId());
        return mapToResponse(saved);
    }

    @Transactional
    public InpatientProgressNoteResponse updateProgressNote(Long id, InpatientProgressNoteRequest request) {
        InpatientProgressNote note = progressNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Progress Note not found with ID: " + id));

        note.setClinicalAssessment(request.getClinicalAssessment());
        note.setProgressSummary(request.getProgressSummary());
        note.setDiagnosisUpdate(request.getDiagnosisUpdate());
        note.setTreatmentUpdate(request.getTreatmentUpdate());
        note.setFollowUpPlan(request.getFollowUpPlan());
        if (request.getNoteDatetime() != null) note.setNoteDatetime(request.getNoteDatetime());

        InpatientProgressNote updated = progressNoteRepository.save(note);
        auditLogService.logAction("UPDATE_PROGRESS_NOTE", "PROGRESS_NOTE", updated.getProgressNoteId(), "Updated progress note");
        return mapToResponse(updated);
    }

    private Doctor getCurrentDoctor() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
                return doctorRepository.findByEmail(principal.getEmail()).orElse(null);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String generateProgressNoteId() {
        int year = LocalDate.now().getYear();
        long count = progressNoteRepository.count() + 1;
        String code = String.format("PN-%d-%06d", year, count);
        while (progressNoteRepository.existsByProgressNoteId(code)) {
            count++;
            code = String.format("PN-%d-%06d", year, count);
        }
        return code;
    }

    public InpatientProgressNoteResponse mapToResponse(InpatientProgressNote note) {
        Patient p = note.getPatient();
        Doctor d = note.getDoctor();

        return InpatientProgressNoteResponse.builder()
                .id(note.getId())
                .progressNoteId(note.getProgressNoteId())
                .admissionId(note.getAdmission().getId())
                .patientId(p.getId())
                .patientName(p.getFirstName() + " " + p.getLastName())
                .doctorId(d.getId())
                .doctorName(d.getFirstName() + " " + d.getLastName())
                .noteDatetime(note.getNoteDatetime())
                .clinicalAssessment(note.getClinicalAssessment())
                .progressSummary(note.getProgressSummary())
                .diagnosisUpdate(note.getDiagnosisUpdate())
                .treatmentUpdate(note.getTreatmentUpdate())
                .followUpPlan(note.getFollowUpPlan())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
