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

@Service
@RequiredArgsConstructor
public class DischargeService {

    private final DischargePlanRepository dischargePlanRepository;
    private final DischargeSummaryRepository dischargeSummaryRepository;
    private final IpdAdmissionRepository ipdAdmissionRepository;
    private final BedRepository bedRepository;
    private final BedAllocationRepository bedAllocationRepository;
    private final DoctorRepository doctorRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public DischargePlanResponse getDischargePlan(Long admissionId) {
        DischargePlan plan = dischargePlanRepository.findByAdmissionId(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Discharge Plan not found for admission ID: " + admissionId));
        return mapToPlanResponse(plan);
    }

    @Transactional
    public DischargePlanResponse createOrUpdateDischargePlan(Long admissionId, DischargePlanRequest request) {
        IpdAdmission admission = ipdAdmissionRepository.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found with ID: " + admissionId));

        if (admission.getStatus() == AdmissionStatus.DISCHARGED || admission.getStatus() == AdmissionStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create/update discharge plan for discharged or cancelled admission");
        }

        DischargePlan plan = dischargePlanRepository.findByAdmissionId(admissionId).orElse(null);

        if (plan == null) {
            String planId = generatePlanId();
            plan = DischargePlan.builder()
                    .dischargePlanId(planId)
                    .admission(admission)
                    .patient(admission.getPatient())
                    .plannedDischargeDate(request.getPlannedDischargeDate())
                    .dischargeCondition(request.getDischargeCondition())
                    .followUpRequired(request.getFollowUpRequired() != null ? request.getFollowUpRequired() : false)
                    .followUpDate(request.getFollowUpDate())
                    .followUpInstructions(request.getFollowUpInstructions())
                    .homeCareInstructions(request.getHomeCareInstructions())
                    .createdBy(getCurrentUsername())
                    .build();
        } else {
            plan.setPlannedDischargeDate(request.getPlannedDischargeDate());
            plan.setDischargeCondition(request.getDischargeCondition());
            if (request.getFollowUpRequired() != null) plan.setFollowUpRequired(request.getFollowUpRequired());
            plan.setFollowUpDate(request.getFollowUpDate());
            plan.setFollowUpInstructions(request.getFollowUpInstructions());
            plan.setHomeCareInstructions(request.getHomeCareInstructions());
        }

        DischargePlan saved = dischargePlanRepository.save(plan);

        // Update admission status to DISCHARGE_PLANNED if currently ADMITTED
        if (admission.getStatus() == AdmissionStatus.ADMITTED) {
            admission.setStatus(AdmissionStatus.DISCHARGE_PLANNED);
            admission.setExpectedDischargeDate(request.getPlannedDischargeDate());
            ipdAdmissionRepository.save(admission);
        }

        auditLogService.logAction("CREATE_DISCHARGE_PLAN", "DISCHARGE_PLAN", saved.getDischargePlanId(), "Saved discharge plan for admission " + admission.getAdmissionId());
        return mapToPlanResponse(saved);
    }

    @Transactional(readOnly = true)
    public DischargeSummaryResponse getDischargeSummary(Long admissionId) {
        DischargeSummary summary = dischargeSummaryRepository.findByAdmissionId(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Discharge Summary not found for admission ID: " + admissionId));
        return mapToSummaryResponse(summary);
    }

    @Transactional
    public DischargeSummaryResponse createOrUpdateDischargeSummary(Long admissionId, DischargeSummaryRequest request) {
        IpdAdmission admission = ipdAdmissionRepository.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found with ID: " + admissionId));

        Doctor doctor = getCurrentDoctor();
        if (doctor == null) {
            doctor = admission.getAdmittingDoctor();
        }

        DischargeSummary summary = dischargeSummaryRepository.findByAdmissionId(admissionId).orElse(null);

        if (summary == null) {
            String summaryId = generateSummaryId();
            summary = DischargeSummary.builder()
                    .dischargeSummaryId(summaryId)
                    .admission(admission)
                    .patient(admission.getPatient())
                    .doctor(doctor)
                    .admissionSummary(request.getAdmissionSummary())
                    .clinicalCourse(request.getClinicalCourse())
                    .finalDiagnosis(request.getFinalDiagnosis())
                    .proceduresSummary(request.getProceduresSummary())
                    .investigationSummary(request.getInvestigationSummary())
                    .treatmentSummary(request.getTreatmentSummary())
                    .medicationSummary(request.getMedicationSummary())
                    .conditionAtDischarge(request.getConditionAtDischarge())
                    .followUpInstructions(request.getFollowUpInstructions())
                    .dischargeDate(request.getDischargeDate() != null ? request.getDischargeDate() : LocalDate.now())
                    .build();
        } else {
            summary.setDoctor(doctor);
            summary.setAdmissionSummary(request.getAdmissionSummary());
            summary.setClinicalCourse(request.getClinicalCourse());
            summary.setFinalDiagnosis(request.getFinalDiagnosis());
            summary.setProceduresSummary(request.getProceduresSummary());
            summary.setInvestigationSummary(request.getInvestigationSummary());
            summary.setTreatmentSummary(request.getTreatmentSummary());
            summary.setMedicationSummary(request.getMedicationSummary());
            summary.setConditionAtDischarge(request.getConditionAtDischarge());
            summary.setFollowUpInstructions(request.getFollowUpInstructions());
            if (request.getDischargeDate() != null) summary.setDischargeDate(request.getDischargeDate());
        }

        DischargeSummary saved = dischargeSummaryRepository.save(summary);
        auditLogService.logAction("CREATE_DISCHARGE_SUMMARY", "DISCHARGE_SUMMARY", saved.getDischargeSummaryId(), "Saved discharge summary for admission " + admission.getAdmissionId());
        return mapToSummaryResponse(saved);
    }

    @Transactional
    public IpdAdmissionResponse dischargePatient(Long admissionId, DischargeExecuteRequest request) {
        IpdAdmission admission = ipdAdmissionRepository.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found with ID: " + admissionId));

        if (admission.getStatus() == AdmissionStatus.DISCHARGED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient has already been discharged for this admission");
        }

        if (admission.getStatus() == AdmissionStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot discharge a cancelled admission");
        }

        // 1. Release bed & set to CLEANING
        Bed bed = admission.getBed();
        if (bed != null) {
            bed.setStatus(BedStatus.CLEANING);
            bedRepository.save(bed);

            bedAllocationRepository.findFirstByAdmissionIdAndEndDatetimeIsNullOrderByStartDatetimeDesc(admissionId)
                    .ifPresent(curr -> {
                        curr.setEndDatetime(LocalDateTime.now());
                        bedAllocationRepository.save(curr);
                    });
        }

        // 2. Update admission record
        admission.setStatus(AdmissionStatus.DISCHARGED);
        admission.setActualDischargeDate(request.getDischargeDate() != null ? request.getDischargeDate() : LocalDate.now());
        admission.setDischargeType(request.getDischargeType() != null ? request.getDischargeType() : DischargeType.NORMAL);

        IpdAdmission updated = ipdAdmissionRepository.save(admission);

        auditLogService.logAction("DISCHARGE_PATIENT", "IPD_ADMISSION", updated.getAdmissionId(), "Discharged patient from admission " + updated.getAdmissionId());
        if (bed != null) {
            auditLogService.logAction("RELEASE_BED", "BED", bed.getBedCode(), "Bed released to CLEANING upon discharge");
        }

        return mapToAdmissionResponse(updated);
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

    private String getCurrentUsername() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
                return principal.getUsername();
            }
        } catch (Exception ignored) {}
        return "system";
    }

    private String generatePlanId() {
        int year = LocalDate.now().getYear();
        long count = dischargePlanRepository.count() + 1;
        String code = String.format("DCP-%d-%06d", year, count);
        while (dischargePlanRepository.existsByDischargePlanId(code)) {
            count++;
            code = String.format("DCP-%d-%06d", year, count);
        }
        return code;
    }

    private String generateSummaryId() {
        int year = LocalDate.now().getYear();
        long count = dischargeSummaryRepository.count() + 1;
        String code = String.format("DSC-%d-%06d", year, count);
        while (dischargeSummaryRepository.existsByDischargeSummaryId(code)) {
            count++;
            code = String.format("DSC-%d-%06d", year, count);
        }
        return code;
    }

    public DischargePlanResponse mapToPlanResponse(DischargePlan plan) {
        Patient p = plan.getPatient();
        return DischargePlanResponse.builder()
                .id(plan.getId())
                .dischargePlanId(plan.getDischargePlanId())
                .admissionId(plan.getAdmission().getId())
                .admissionCode(plan.getAdmission().getAdmissionId())
                .patientId(p.getId())
                .patientName(p.getFirstName() + " " + p.getLastName())
                .plannedDischargeDate(plan.getPlannedDischargeDate())
                .dischargeCondition(plan.getDischargeCondition())
                .followUpRequired(plan.getFollowUpRequired())
                .followUpDate(plan.getFollowUpDate())
                .followUpInstructions(plan.getFollowUpInstructions())
                .homeCareInstructions(plan.getHomeCareInstructions())
                .createdBy(plan.getCreatedBy())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }

    public DischargeSummaryResponse mapToSummaryResponse(DischargeSummary summary) {
        Patient p = summary.getPatient();
        Doctor d = summary.getDoctor();
        return DischargeSummaryResponse.builder()
                .id(summary.getId())
                .dischargeSummaryId(summary.getDischargeSummaryId())
                .admissionId(summary.getAdmission().getId())
                .admissionCode(summary.getAdmission().getAdmissionId())
                .patientId(p.getId())
                .patientName(p.getFirstName() + " " + p.getLastName())
                .doctorId(d.getId())
                .doctorName(d.getFirstName() + " " + d.getLastName())
                .admissionSummary(summary.getAdmissionSummary())
                .clinicalCourse(summary.getClinicalCourse())
                .finalDiagnosis(summary.getFinalDiagnosis())
                .proceduresSummary(summary.getProceduresSummary())
                .investigationSummary(summary.getInvestigationSummary())
                .treatmentSummary(summary.getTreatmentSummary())
                .medicationSummary(summary.getMedicationSummary())
                .conditionAtDischarge(summary.getConditionAtDischarge())
                .followUpInstructions(summary.getFollowUpInstructions())
                .dischargeDate(summary.getDischargeDate())
                .createdAt(summary.getCreatedAt())
                .updatedAt(summary.getUpdatedAt())
                .build();
    }

    private IpdAdmissionResponse mapToAdmissionResponse(IpdAdmission admission) {
        Patient p = admission.getPatient();
        Doctor d = admission.getAdmittingDoctor();
        Department dept = admission.getDepartment();
        Ward w = admission.getWard();
        Bed b = admission.getBed();

        return IpdAdmissionResponse.builder()
                .id(admission.getId())
                .admissionId(admission.getAdmissionId())
                .patientId(p.getId())
                .patientName(p.getFirstName() + " " + p.getLastName())
                .patientCode(p.getPatientId())
                .patientPhone(p.getPhone())
                .patientGender(p.getGender())
                .admittingDoctorId(d.getId())
                .doctorName(d.getFirstName() + " " + d.getLastName())
                .departmentId(dept.getId())
                .departmentName(dept.getDepartmentName())
                .wardId(w != null ? w.getId() : null)
                .wardCode(w != null ? w.getWardCode() : null)
                .wardName(w != null ? w.getWardName() : null)
                .bedId(b != null ? b.getId() : null)
                .bedCode(b != null ? b.getBedCode() : null)
                .bedNumber(b != null ? b.getBedNumber() : null)
                .admissionType(admission.getAdmissionType())
                .admissionDate(admission.getAdmissionDate())
                .admissionTime(admission.getAdmissionTime())
                .reasonForAdmission(admission.getReasonForAdmission())
                .clinicalSummary(admission.getClinicalSummary())
                .status(admission.getStatus())
                .expectedDischargeDate(admission.getExpectedDischargeDate())
                .actualDischargeDate(admission.getActualDischargeDate())
                .dischargeType(admission.getDischargeType())
                .createdBy(admission.getCreatedBy())
                .createdAt(admission.getCreatedAt())
                .updatedAt(admission.getUpdatedAt())
                .build();
    }
}
