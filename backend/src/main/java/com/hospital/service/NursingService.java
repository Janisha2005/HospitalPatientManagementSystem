package com.hospital.service;

import com.hospital.dto.*;
import com.hospital.entity.*;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NursingService {

    private final InpatientVitalRepository inpatientVitalRepository;
    private final NursingNoteRepository nursingNoteRepository;
    private final IpdAdmissionRepository ipdAdmissionRepository;
    private final BedRepository bedRepository;
    private final WardTransferRepository wardTransferRepository;
    private final UserRepository userRepository;
    private final IpdAdmissionService ipdAdmissionService;
    private final AuditLogService auditLogService;

    private static final List<AdmissionStatus> ACTIVE_STATUSES = Arrays.asList(
            AdmissionStatus.REQUESTED, AdmissionStatus.APPROVED, AdmissionStatus.ADMITTED,
            AdmissionStatus.ON_LEAVE, AdmissionStatus.DISCHARGE_PLANNED
    );

    @Transactional(readOnly = true)
    public NursingDashboardDto getNursingDashboard() {
        List<IpdAdmission> admittedList = ipdAdmissionRepository.findAll().stream()
                .filter(a -> a.getStatus() == AdmissionStatus.ADMITTED || a.getStatus() == AdmissionStatus.DISCHARGE_PLANNED)
                .collect(Collectors.toList());

        long currentInpatientsCount = admittedList.size();
        long newAdmissionsToday = ipdAdmissionRepository.countByAdmissionDate(LocalDate.now());
        long pendingTransfersCount = wardTransferRepository.findAll().stream()
                .filter(t -> t.getStatus() == TransferStatus.REQUESTED)
                .count();
        long availableBedsCount = bedRepository.countByStatus(BedStatus.AVAILABLE);
        long dischargePlannedCount = ipdAdmissionRepository.countByStatus(AdmissionStatus.DISCHARGE_PLANNED);

        List<IpdAdmissionResponse> activeResponses = admittedList.stream()
                .map(ipdAdmissionService::mapToResponse)
                .collect(Collectors.toList());

        return NursingDashboardDto.builder()
                .currentInpatientsCount(currentInpatientsCount)
                .newAdmissionsToday(newAdmissionsToday)
                .pendingTransfersCount(pendingTransfersCount)
                .patientsDueForVitalsCount(currentInpatientsCount) // Inpatients requiring vitals
                .criticalPatientsCount(0)
                .dischargePlannedCount(dischargePlannedCount)
                .availableBedsCount(availableBedsCount)
                .activeInpatients(activeResponses)
                .build();
    }

    @Transactional(readOnly = true)
    public List<InpatientVitalResponse> getVitalsForAdmission(Long admissionId) {
        return inpatientVitalRepository.findByAdmissionIdOrderByRecordedAtDesc(admissionId).stream()
                .map(this::mapToVitalResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public InpatientVitalResponse recordVitals(Long admissionId, InpatientVitalRequest request) {
        IpdAdmission admission = ipdAdmissionRepository.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found with ID: " + admissionId));

        String currentUsername = getCurrentUsername();

        InpatientVital vital = InpatientVital.builder()
                .admission(admission)
                .patient(admission.getPatient())
                .recordedBy(currentUsername)
                .recordedAt(request.getRecordedAt() != null ? request.getRecordedAt() : LocalDateTime.now())
                .temperature(request.getTemperature())
                .pulse(request.getPulse())
                .bloodPressure(request.getBloodPressure())
                .respiratoryRate(request.getRespiratoryRate())
                .oxygenSaturation(request.getOxygenSaturation())
                .heightCm(request.getHeightCm())
                .weightKg(request.getWeightKg())
                .painScore(request.getPainScore())
                .notes(request.getNotes())
                .build();

        InpatientVital saved = inpatientVitalRepository.save(vital);
        auditLogService.logAction("RECORD_INPATIENT_VITAL", "INPATIENT_VITAL", saved.getId().toString(), "Recorded vitals for admission " + admission.getAdmissionId());
        return mapToVitalResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<NursingNoteResponse> getNursingNotesForAdmission(Long admissionId) {
        return nursingNoteRepository.findByAdmissionIdOrderByNoteDatetimeDesc(admissionId).stream()
                .map(this::mapToNoteResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public NursingNoteResponse createNursingNote(Long admissionId, NursingNoteRequest request) {
        IpdAdmission admission = ipdAdmissionRepository.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found with ID: " + admissionId));

        User nurse = getCurrentUser();
        String noteId = generateNoteId();

        NursingNote note = NursingNote.builder()
                .noteId(noteId)
                .admission(admission)
                .patient(admission.getPatient())
                .nurse(nurse)
                .noteDatetime(request.getNoteDatetime() != null ? request.getNoteDatetime() : LocalDateTime.now())
                .noteType(request.getNoteType())
                .noteText(request.getNoteText())
                .build();

        NursingNote saved = nursingNoteRepository.save(note);
        auditLogService.logAction("CREATE_NURSING_NOTE", "NURSING_NOTE", saved.getNoteId(), "Created nursing note for admission " + admission.getAdmissionId());
        return mapToNoteResponse(saved);
    }

    @Transactional
    public NursingNoteResponse updateNursingNote(Long noteId, NursingNoteRequest request) {
        NursingNote note = nursingNoteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Nursing Note not found with ID: " + noteId));

        note.setNoteType(request.getNoteType());
        note.setNoteText(request.getNoteText());
        if (request.getNoteDatetime() != null) note.setNoteDatetime(request.getNoteDatetime());

        NursingNote updated = nursingNoteRepository.save(note);
        auditLogService.logAction("UPDATE_NURSING_NOTE", "NURSING_NOTE", updated.getNoteId(), "Updated nursing note");
        return mapToNoteResponse(updated);
    }

    private String generateNoteId() {
        int year = LocalDate.now().getYear();
        long count = nursingNoteRepository.count() + 1;
        String code = String.format("NN-%d-%06d", year, count);
        while (nursingNoteRepository.existsByNoteId(code)) {
            count++;
            code = String.format("NN-%d-%06d", year, count);
        }
        return code;
    }

    private User getCurrentUser() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
                return userRepository.findById(principal.getId()).orElse(null);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String getCurrentUsername() {
        User user = getCurrentUser();
        return user != null ? user.getFullName() : "Nurse";
    }

    public InpatientVitalResponse mapToVitalResponse(InpatientVital vital) {
        Patient p = vital.getPatient();
        return InpatientVitalResponse.builder()
                .id(vital.getId())
                .admissionId(vital.getAdmission().getId())
                .patientId(p.getId())
                .patientName(p.getFirstName() + " " + p.getLastName())
                .recordedBy(vital.getRecordedBy())
                .recordedAt(vital.getRecordedAt())
                .temperature(vital.getTemperature())
                .pulse(vital.getPulse())
                .bloodPressure(vital.getBloodPressure())
                .respiratoryRate(vital.getRespiratoryRate())
                .oxygenSaturation(vital.getOxygenSaturation())
                .heightCm(vital.getHeightCm())
                .weightKg(vital.getWeightKg())
                .painScore(vital.getPainScore())
                .notes(vital.getNotes())
                .createdAt(vital.getCreatedAt())
                .build();
    }

    public NursingNoteResponse mapToNoteResponse(NursingNote note) {
        Patient p = note.getPatient();
        User nurse = note.getNurse();
        return NursingNoteResponse.builder()
                .id(note.getId())
                .noteId(note.getNoteId())
                .admissionId(note.getAdmission().getId())
                .patientId(p.getId())
                .patientName(p.getFirstName() + " " + p.getLastName())
                .nurseId(nurse != null ? nurse.getId() : null)
                .nurseName(nurse != null ? nurse.getFullName() : null)
                .noteDatetime(note.getNoteDatetime())
                .noteType(note.getNoteType())
                .noteText(note.getNoteText())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
