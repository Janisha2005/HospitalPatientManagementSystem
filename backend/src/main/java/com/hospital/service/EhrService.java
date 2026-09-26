package com.hospital.service;

import com.hospital.dto.*;
import com.hospital.entity.*;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.*;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EhrService {

    private final EhrRecordRepository ehrRecordRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final OpdVisitRepository opdVisitRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClinicalDiagnosisRepository diagnosisRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final LabOrderRepository labOrderRepository;
    private final RadiologyOrderRepository radiologyOrderRepository;
    private final PatientAllergyRepository allergyRepository;
    private final PatientConditionRepository conditionRepository;
    private final IpdAdmissionRepository ipdAdmissionRepository;
    private final InpatientVitalRepository inpatientVitalRepository;
    private final NursingNoteRepository nursingNoteRepository;
    private final InpatientProgressNoteRepository progressNoteRepository;
    private final DischargePlanRepository dischargePlanRepository;
    private final DischargeSummaryRepository dischargeSummaryRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<EhrRecordResponse> getRecords(Long patientId, Long doctorId, Long departmentId, Pageable pageable) {
        checkPatientOwnershipById(patientId);
        Page<EhrRecord> page = ehrRecordRepository.findRecordsWithFilters(patientId, doctorId, departmentId, pageable);
        return page.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public EhrRecordResponse getRecordById(Long id) {
        EhrRecord record = ehrRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EHR Record not found with ID: " + id));
        checkPatientOwnershipById(record.getPatient().getId());
        return mapToResponse(record);
    }

    @Transactional
    public EhrRecordResponse createRecord(EhrRecordRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + request.getPatientId()));
        if (!patient.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create EHR record for inactive patient");
        }

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + request.getDoctorId()));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        OpdVisit opdVisit = null;
        if (request.getOpdVisitId() != null) {
            opdVisit = opdVisitRepository.findById(request.getOpdVisitId()).orElse(null);
        }

        String code = generateEhrCode();

        EhrRecord record = EhrRecord.builder()
                .ehrRecordId(code)
                .patient(patient)
                .doctor(doctor)
                .department(department)
                .opdVisit(opdVisit)
                .recordType(request.getRecordType())
                .clinicalSummary(request.getClinicalSummary())
                .diagnosisSummary(request.getDiagnosisSummary())
                .treatmentSummary(request.getTreatmentSummary())
                .build();

        EhrRecord saved = ehrRecordRepository.save(record);

        auditLogService.logAction("CREATE_EHR_RECORD", "EhrRecord", saved.getId().toString(),
                "Created EHR record " + saved.getEhrRecordId() + " for patient " + patient.getPatientId());

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public ClinicalTimelineResponse getPatientTimeline(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + patientId));
        checkPatientOwnershipById(patientId);

        List<ClinicalTimelineEventDto> events = new ArrayList<>();

        // 1. Appointments
        appointmentRepository.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patientId).forEach(a -> {
            events.add(ClinicalTimelineEventDto.builder()
                    .eventType("APPOINTMENT")
                    .eventId(a.getAppointmentId())
                    .title("Appointment - " + a.getAppointmentType().name().replace('_', ' '))
                    .summary("Reason: " + (a.getReasonForVisit() != null ? a.getReasonForVisit() : "N/A"))
                    .status(a.getStatus().name())
                    .doctorName("Dr. " + a.getDoctor().getFirstName() + " " + a.getDoctor().getLastName())
                    .eventTimestamp(a.getAppointmentDate().atTime(a.getStartTime()))
                    .build());
        });

        // 2. OPD Visits
        opdVisitRepository.findByPatientIdOrderByVisitDateDescCreatedAtDesc(patientId).forEach(o -> {
            events.add(ClinicalTimelineEventDto.builder()
                    .eventType("OPD_VISIT")
                    .eventId(o.getOpdVisitId())
                    .title("OPD Visit #" + o.getQueueNumber())
                    .summary("Chief Complaint: " + (o.getChiefComplaint() != null ? o.getChiefComplaint() : "N/A"))
                    .status(o.getVisitStatus().name())
                    .doctorName("Dr. " + o.getDoctor().getFirstName() + " " + o.getDoctor().getLastName())
                    .eventTimestamp(o.getVisitDate().atStartOfDay())
                    .build());
        });

        // 3. Diagnoses
        diagnosisRepository.findByPatientIdOrderByDiagnosedAtDesc(patientId).forEach(d -> {
            events.add(ClinicalTimelineEventDto.builder()
                    .eventType("DIAGNOSIS")
                    .eventId(d.getDiagnosisId())
                    .title("Diagnosis: " + d.getDiagnosisName())
                    .summary("Type: " + d.getDiagnosisType() + " | Desc: " + (d.getDiagnosisDescription() != null ? d.getDiagnosisDescription() : ""))
                    .status(d.getStatus().name())
                    .doctorName("Dr. " + d.getDoctor().getFirstName() + " " + d.getDoctor().getLastName())
                    .eventTimestamp(d.getDiagnosedAt())
                    .build());
        });

        // 4. Treatment Plans
        treatmentPlanRepository.findByPatientIdOrderByCreatedAtDesc(patientId).forEach(t -> {
            events.add(ClinicalTimelineEventDto.builder()
                    .eventType("TREATMENT_PLAN")
                    .eventId(t.getTreatmentPlanId())
                    .title("Treatment Plan")
                    .summary(t.getPlanDetails())
                    .status(t.getStatus().name())
                    .doctorName("Dr. " + t.getDoctor().getFirstName() + " " + t.getDoctor().getLastName())
                    .eventTimestamp(t.getCreatedAt())
                    .build());
        });

        // 5. Prescriptions
        prescriptionRepository.findByPatientIdOrderByPrescriptionDateDesc(patientId).forEach(p -> {
            events.add(ClinicalTimelineEventDto.builder()
                    .eventType("PRESCRIPTION")
                    .eventId(p.getPrescriptionId())
                    .title("Prescription (" + p.getItems().size() + " items)")
                    .summary(p.getItems().stream().map(PrescriptionItem::getMedicineName).collect(Collectors.joining(", ")))
                    .status(p.getStatus().name())
                    .doctorName("Dr. " + p.getDoctor().getFirstName() + " " + p.getDoctor().getLastName())
                    .eventTimestamp(p.getPrescriptionDate().atStartOfDay())
                    .build());
        });

        // 6. Lab Orders
        labOrderRepository.findByPatientIdOrderByOrderDateDesc(patientId).forEach(l -> {
            events.add(ClinicalTimelineEventDto.builder()
                    .eventType("LAB_ORDER")
                    .eventId(l.getLabOrderId())
                    .title("Lab Order (" + l.getPriority() + ")")
                    .summary("Tests: " + l.getItems().stream().map(i -> i.getLabTest().getTestName()).collect(Collectors.joining(", ")))
                    .status(l.getStatus().name())
                    .doctorName("Dr. " + l.getDoctor().getFirstName() + " " + l.getDoctor().getLastName())
                    .eventTimestamp(l.getOrderDate().atStartOfDay())
                    .build());
        });

        // 7. Radiology Orders
        radiologyOrderRepository.findByPatientIdOrderByOrderDateDesc(patientId).forEach(r -> {
            events.add(ClinicalTimelineEventDto.builder()
                    .eventType("RADIOLOGY_ORDER")
                    .eventId(r.getRadiologyOrderId())
                    .title("Radiology: " + r.getRadiologyTest().getTestName() + " (" + r.getRadiologyTest().getModality() + ")")
                    .summary("Indication: " + (r.getClinicalIndication() != null ? r.getClinicalIndication() : "N/A"))
                    .status(r.getStatus().name())
                    .doctorName("Dr. " + r.getDoctor().getFirstName() + " " + r.getDoctor().getLastName())
                    .eventTimestamp(r.getOrderDate().atStartOfDay())
                    .build());
        });

        // 8. Allergies
        allergyRepository.findByPatientIdOrderByRecordedAtDesc(patientId).forEach(al -> {
            events.add(ClinicalTimelineEventDto.builder()
                    .eventType("ALLERGY")
                    .eventId("ALLERGY-" + al.getId())
                    .title("Allergy: " + al.getAllergen())
                    .summary("Severity: " + al.getSeverity() + " | Reaction: " + (al.getReaction() != null ? al.getReaction() : "N/A"))
                    .status(al.getStatus().name())
                    .doctorName(al.getRecordedBy() != null ? al.getRecordedBy().getFullName() : "Clinical Staff")
                    .eventTimestamp(al.getRecordedAt())
                    .build());
        });

        // 9. Chronic Conditions
        conditionRepository.findByPatientIdOrderByCreatedAtDesc(patientId).forEach(c -> {
            events.add(ClinicalTimelineEventDto.builder()
                    .eventType("CONDITION")
                    .eventId("COND-" + c.getId())
                    .title("Condition: " + c.getConditionName())
                    .summary(c.getDescription() != null ? c.getDescription() : "Diagnosed: " + c.getDiagnosedDate())
                    .status(c.getStatus().name())
                    .doctorName(c.getRecordedBy() != null ? c.getRecordedBy().getFullName() : "Clinical Staff")
                    .eventTimestamp(c.getCreatedAt())
                    .build());
        });

        // 10. IPD Admissions
        ipdAdmissionRepository.findByPatientIdOrderByAdmissionDateDescAdmissionTimeDesc(patientId).forEach(adm -> {
            events.add(ClinicalTimelineEventDto.builder()
                    .eventType("IPD_ADMISSION")
                    .eventId(adm.getAdmissionId())
                    .title("IPD Admission: " + adm.getAdmissionType())
                    .summary("Ward: " + (adm.getWard() != null ? adm.getWard().getWardName() : "Unassigned") + " | Reason: " + adm.getReasonForAdmission())
                    .status(adm.getStatus().name())
                    .doctorName("Dr. " + adm.getAdmittingDoctor().getFirstName() + " " + adm.getAdmittingDoctor().getLastName())
                    .eventTimestamp(adm.getAdmissionDate().atTime(adm.getAdmissionTime()))
                    .build());
        });

        // 11. Inpatient Vitals
        inpatientVitalRepository.findByPatientIdOrderByRecordedAtDesc(patientId).forEach(v -> {
            events.add(ClinicalTimelineEventDto.builder()
                    .eventType("INPATIENT_VITAL")
                    .eventId("VITAL-" + v.getId())
                    .title("Inpatient Vitals: BP " + (v.getBloodPressure() != null ? v.getBloodPressure() : "N/A") + ", Temp " + (v.getTemperature() != null ? v.getTemperature() : "N/A"))
                    .summary("Pulse: " + v.getPulse() + " bpm | SpO2: " + v.getOxygenSaturation() + "%")
                    .status("RECORDED")
                    .doctorName(v.getRecordedBy() != null ? v.getRecordedBy() : "Nursing Staff")
                    .eventTimestamp(v.getRecordedAt())
                    .build());
        });

        // 12. Nursing Notes
        nursingNoteRepository.findByPatientIdOrderByNoteDatetimeDesc(patientId).forEach(nn -> {
            events.add(ClinicalTimelineEventDto.builder()
                    .eventType("NURSING_NOTE")
                    .eventId(nn.getNoteId())
                    .title("Nursing Note: " + nn.getNoteType())
                    .summary(nn.getNoteText())
                    .status("COMPLETED")
                    .doctorName(nn.getNurse() != null ? nn.getNurse().getFullName() : "Nursing Staff")
                    .eventTimestamp(nn.getNoteDatetime())
                    .build());
        });

        // 13. Doctor Progress Notes
        progressNoteRepository.findByPatientIdOrderByNoteDatetimeDesc(patientId).forEach(pn -> {
            events.add(ClinicalTimelineEventDto.builder()
                    .eventType("PROGRESS_NOTE")
                    .eventId(pn.getProgressNoteId())
                    .title("Doctor Progress Note")
                    .summary(pn.getProgressSummary())
                    .status("RECORDED")
                    .doctorName("Dr. " + pn.getDoctor().getFirstName() + " " + pn.getDoctor().getLastName())
                    .eventTimestamp(pn.getNoteDatetime())
                    .build());
        });

        // 14. Discharge Summary
        ipdAdmissionRepository.findByPatientIdOrderByAdmissionDateDescAdmissionTimeDesc(patientId).forEach(adm -> {
            dischargeSummaryRepository.findByAdmissionId(adm.getId()).ifPresent(ds -> {
                events.add(ClinicalTimelineEventDto.builder()
                        .eventType("DISCHARGE_SUMMARY")
                        .eventId(ds.getDischargeSummaryId())
                        .title("Discharge Summary")
                        .summary("Condition at Discharge: " + (ds.getConditionAtDischarge() != null ? ds.getConditionAtDischarge() : "N/A"))
                        .status("FINAL")
                        .doctorName("Dr. " + ds.getDoctor().getFirstName() + " " + ds.getDoctor().getLastName())
                        .eventTimestamp(ds.getDischargeDate().atStartOfDay())
                        .build());
            });
        });

        // Sort descending by event timestamp
        events.sort(Comparator.comparing(ClinicalTimelineEventDto::getEventTimestamp, Comparator.nullsLast(Comparator.reverseOrder())));

        return ClinicalTimelineResponse.builder()
                .patientId(patient.getId())
                .patientCode(patient.getPatientId())
                .patientName(patient.getFirstName() + " " + patient.getLastName())
                .totalEvents(events.size())
                .events(events)
                .build();
    }

    private String generateEhrCode() {
        int year = LocalDate.now().getYear();
        long count = ehrRecordRepository.count() + 1;
        return String.format("EHR-%d-%06d", year, count);
    }

    private void checkPatientOwnershipById(Long patientId) {
        if (patientId == null) return;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userDetails) {
            boolean isPatient = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
            if (isPatient) {
                Patient patient = patientRepository.findById(patientId).orElse(null);
                if (patient != null && !userDetails.getUsername().equalsIgnoreCase(patient.getEmail())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Patient can only access their own clinical records");
                }
            }
        }
    }

    private EhrRecordResponse mapToResponse(EhrRecord r) {
        return EhrRecordResponse.builder()
                .id(r.getId())
                .ehrRecordId(r.getEhrRecordId())
                .patientId(r.getPatient().getId())
                .patientCode(r.getPatient().getPatientId())
                .patientName(r.getPatient().getFirstName() + " " + r.getPatient().getLastName())
                .opdVisitId(r.getOpdVisit() != null ? r.getOpdVisit().getId() : null)
                .opdVisitCode(r.getOpdVisit() != null ? r.getOpdVisit().getOpdVisitId() : null)
                .doctorId(r.getDoctor().getId())
                .doctorName("Dr. " + r.getDoctor().getFirstName() + " " + r.getDoctor().getLastName())
                .departmentId(r.getDepartment().getId())
                .departmentName(r.getDepartment().getDepartmentName())
                .recordType(r.getRecordType())
                .clinicalSummary(r.getClinicalSummary())
                .diagnosisSummary(r.getDiagnosisSummary())
                .treatmentSummary(r.getTreatmentSummary())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
