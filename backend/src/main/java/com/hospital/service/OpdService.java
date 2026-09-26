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
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpdService {

    private final OpdVisitRepository opdVisitRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public OpdVisitResponse checkInPatient(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        if (opdVisitRepository.existsByAppointmentId(appointmentId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient has already been checked in for this appointment");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED || appointment.getStatus() == AppointmentStatus.NO_SHOW || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot check in an appointment with status: " + appointment.getStatus());
        }

        // Atomic Queue Number Generation
        LocalDate visitDate = appointment.getAppointmentDate();
        Integer maxQueue = opdVisitRepository.findMaxQueueNumberForDoctorAndDate(appointment.getDoctor().getId(), visitDate);
        int nextQueue = (maxQueue != null) ? maxQueue + 1 : 1;

        // Update Appointment Status to CHECKED_IN
        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        appointmentRepository.save(appointment);

        String opdCode = generateOpdCode();

        OpdVisit opdVisit = OpdVisit.builder()
                .opdVisitId(opdCode)
                .appointment(appointment)
                .patient(appointment.getPatient())
                .doctor(appointment.getDoctor())
                .department(appointment.getDepartment())
                .visitDate(visitDate)
                .queueNumber(nextQueue)
                .chiefComplaint(appointment.getReasonForVisit())
                .visitStatus(OpdVisitStatus.WAITING)
                .build();

        OpdVisit saved = opdVisitRepository.save(opdVisit);

        auditLogService.logAction("CHECK_IN_PATIENT", "OpdVisit", saved.getId().toString(),
                "Checked in patient " + appointment.getPatient().getPatientId() + " for appointment " + appointment.getAppointmentId() + ". Assigned Queue #" + nextQueue);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<OpdVisitResponse> getVisits(Long doctorId, Long patientId, Long departmentId,
                                           LocalDate visitDate, OpdVisitStatus status, Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userDetails) {
            boolean isPatient = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
            if (isPatient) {
                Patient patient = patientRepository.findByEmail(userDetails.getUsername()).orElse(null);
                if (patient != null) {
                    patientId = patient.getId();
                }
            }
        }

        Page<OpdVisit> page = opdVisitRepository.findVisitsWithFilters(doctorId, patientId, departmentId, visitDate, status, pageable);
        return page.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public OpdVisitResponse getVisitById(Long id) {
        OpdVisit visit = opdVisitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OPD Visit not found with ID: " + id));
        checkPatientOwnership(visit.getPatient().getEmail());
        return mapToResponse(visit);
    }

    @Transactional(readOnly = true)
    public OpdVisitResponse getVisitByCode(String opdVisitId) {
        OpdVisit visit = opdVisitRepository.findByOpdVisitId(opdVisitId)
                .orElseThrow(() -> new ResourceNotFoundException("OPD Visit not found with Code: " + opdVisitId));
        checkPatientOwnership(visit.getPatient().getEmail());
        return mapToResponse(visit);
    }

    @Transactional
    public OpdVisitResponse updateVisitDetails(Long id, OpdVisitUpdateRequest request) {
        OpdVisit visit = opdVisitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OPD Visit not found with ID: " + id));

        if (visit.getVisitStatus() == OpdVisitStatus.COMPLETED || visit.getVisitStatus() == OpdVisitStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot edit completed or cancelled OPD visit");
        }

        if (request.getChiefComplaint() != null) visit.setChiefComplaint(request.getChiefComplaint());
        if (request.getClinicalNotes() != null) visit.setClinicalNotes(request.getClinicalNotes());
        if (request.getDiagnosis() != null) visit.setDiagnosis(request.getDiagnosis());
        if (request.getTreatmentPlan() != null) visit.setTreatmentPlan(request.getTreatmentPlan());

        if (request.getVitalTemperature() != null) visit.setVitalTemperature(request.getVitalTemperature());
        if (request.getVitalPulse() != null) visit.setVitalPulse(request.getVitalPulse());
        if (request.getVitalBloodPressure() != null) visit.setVitalBloodPressure(request.getVitalBloodPressure());
        if (request.getVitalRespiratoryRate() != null) visit.setVitalRespiratoryRate(request.getVitalRespiratoryRate());
        if (request.getVitalOxygenSaturation() != null) visit.setVitalOxygenSaturation(request.getVitalOxygenSaturation());
        if (request.getHeightCm() != null) visit.setHeightCm(request.getHeightCm());
        if (request.getWeightKg() != null) visit.setWeightKg(request.getWeightKg());

        if (request.getVisitStatus() != null) {
            visit.setVisitStatus(request.getVisitStatus());
        }

        OpdVisit saved = opdVisitRepository.save(visit);

        auditLogService.logAction("UPDATE_OPD_VISIT", "OpdVisit", saved.getId().toString(),
                "Updated OPD clinical details / vitals for OPD ID: " + saved.getOpdVisitId());

        return mapToResponse(saved);
    }

    @Transactional
    public OpdVisitResponse callQueuePatient(Long id) {
        OpdVisit visit = opdVisitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OPD Visit not found with ID: " + id));
        visit.setVisitStatus(OpdVisitStatus.CALLED);
        OpdVisit saved = opdVisitRepository.save(visit);

        auditLogService.logAction("CALL_QUEUE_PATIENT", "OpdVisit", saved.getId().toString(),
                "Called patient Queue #" + saved.getQueueNumber() + " for OPD Visit " + saved.getOpdVisitId());

        return mapToResponse(saved);
    }

    @Transactional
    public OpdVisitResponse startConsultation(Long id) {
        OpdVisit visit = opdVisitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OPD Visit not found with ID: " + id));

        visit.setVisitStatus(OpdVisitStatus.IN_CONSULTATION);
        visit.getAppointment().setStatus(AppointmentStatus.IN_CONSULTATION);
        appointmentRepository.save(visit.getAppointment());

        OpdVisit saved = opdVisitRepository.save(visit);

        auditLogService.logAction("START_CONSULTATION", "OpdVisit", saved.getId().toString(),
                "Started consultation for OPD Visit " + saved.getOpdVisitId());

        return mapToResponse(saved);
    }

    @Transactional
    public OpdVisitResponse completeConsultation(Long id, OpdVisitUpdateRequest request) {
        OpdVisit visit = opdVisitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OPD Visit not found with ID: " + id));

        if (request != null) {
            updateVisitDetails(id, request);
        }

        visit.setVisitStatus(OpdVisitStatus.COMPLETED);
        visit.getAppointment().setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(visit.getAppointment());

        OpdVisit saved = opdVisitRepository.save(visit);

        auditLogService.logAction("COMPLETE_CONSULTATION", "OpdVisit", saved.getId().toString(),
                "Completed consultation for OPD Visit " + saved.getOpdVisitId());

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OpdVisitResponse> getTodayQueue(Long doctorId, Long departmentId) {
        LocalDate today = LocalDate.now();
        List<OpdVisit> visits;
        if (doctorId != null) {
            visits = opdVisitRepository.findByDoctorIdAndVisitDateOrderByQueueNumberAsc(doctorId, today);
        } else if (departmentId != null) {
            visits = opdVisitRepository.findByDepartmentIdAndVisitDateOrderByQueueNumberAsc(departmentId, today);
        } else {
            visits = opdVisitRepository.findVisitsWithFilters(null, null, null, today, null, Pageable.unpaged()).getContent();
        }
        return visits.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OpdDashboardDto getDashboardStats(Long doctorId) {
        LocalDate today = LocalDate.now();

        long todaysAppointments = appointmentRepository.countByAppointmentDate(today);
        long checkedInPatients = appointmentRepository.countByAppointmentDateAndStatus(today, AppointmentStatus.CHECKED_IN);
        long waitingPatients = opdVisitRepository.countByVisitDateAndVisitStatus(today, OpdVisitStatus.WAITING);
        long inConsultationPatients = opdVisitRepository.countByVisitDateAndVisitStatus(today, OpdVisitStatus.IN_CONSULTATION);
        long completedVisits = opdVisitRepository.countByVisitDateAndVisitStatus(today, OpdVisitStatus.COMPLETED);
        long cancelledAppointments = appointmentRepository.countByAppointmentDateAndStatus(today, AppointmentStatus.CANCELLED);
        long noShowAppointments = appointmentRepository.countByAppointmentDateAndStatus(today, AppointmentStatus.NO_SHOW);

        OpdDashboardDto.OpdDashboardDtoBuilder builder = OpdDashboardDto.builder()
                .todaysAppointments(todaysAppointments)
                .checkedInPatients(checkedInPatients)
                .waitingPatients(waitingPatients)
                .inConsultationPatients(inConsultationPatients)
                .completedVisits(completedVisits)
                .cancelledAppointments(cancelledAppointments)
                .noShowAppointments(noShowAppointments);

        if (doctorId != null) {
            builder.doctorId(doctorId)
                    .myAppointmentsToday(appointmentRepository.findByDoctorIdAndAppointmentDateOrderByStartTimeAsc(doctorId, today).size())
                    .myWaitingPatients(opdVisitRepository.countByDoctorIdAndVisitDateAndVisitStatus(doctorId, today, OpdVisitStatus.WAITING))
                    .myInConsultationPatients(opdVisitRepository.countByDoctorIdAndVisitDateAndVisitStatus(doctorId, today, OpdVisitStatus.IN_CONSULTATION))
                    .myCompletedVisits(opdVisitRepository.countByDoctorIdAndVisitDateAndVisitStatus(doctorId, today, OpdVisitStatus.COMPLETED));
        }

        return builder.build();
    }

    private String generateOpdCode() {
        int year = LocalDate.now().getYear();
        long count = opdVisitRepository.count() + 1;
        return String.format("OPD-%d-%06d", year, count);
    }

    private void checkPatientOwnership(String patientEmail) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userDetails) {
            boolean isPatient = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
            if (isPatient && !userDetails.getUsername().equalsIgnoreCase(patientEmail)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Patient can only access their own records");
            }
        }
    }

    private OpdVisitResponse mapToResponse(OpdVisit o) {
        int age = 0;
        if (o.getPatient().getDateOfBirth() != null) {
            age = Period.between(o.getPatient().getDateOfBirth(), LocalDate.now()).getYears();
        }

        return OpdVisitResponse.builder()
                .id(o.getId())
                .opdVisitId(o.getOpdVisitId())
                .appointmentId(o.getAppointment().getId())
                .appointmentCode(o.getAppointment().getAppointmentId())
                .patientId(o.getPatient().getId())
                .patientCode(o.getPatient().getPatientId())
                .patientName(o.getPatient().getFirstName() + " " + o.getPatient().getLastName())
                .patientPhone(o.getPatient().getPhone())
                .patientAge(age)
                .patientGender(o.getPatient().getGender())
                .doctorId(o.getDoctor().getId())
                .doctorCode(o.getDoctor().getDoctorId())
                .doctorName("Dr. " + o.getDoctor().getFirstName() + " " + o.getDoctor().getLastName())
                .doctorSpecialization(o.getDoctor().getSpecialization())
                .departmentId(o.getDepartment().getId())
                .departmentName(o.getDepartment().getDepartmentName())
                .visitDate(o.getVisitDate())
                .queueNumber(o.getQueueNumber())
                .chiefComplaint(o.getChiefComplaint())
                .clinicalNotes(o.getClinicalNotes())
                .diagnosis(o.getDiagnosis())
                .treatmentPlan(o.getTreatmentPlan())
                .vitalTemperature(o.getVitalTemperature())
                .vitalPulse(o.getVitalPulse())
                .vitalBloodPressure(o.getVitalBloodPressure())
                .vitalRespiratoryRate(o.getVitalRespiratoryRate())
                .vitalOxygenSaturation(o.getVitalOxygenSaturation())
                .heightCm(o.getHeightCm())
                .weightKg(o.getWeightKg())
                .visitStatus(o.getVisitStatus())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}
