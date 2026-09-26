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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAppointments(Long doctorId, Long patientId, Long departmentId,
                                                    LocalDate date, AppointmentStatus status,
                                                    AppointmentType appointmentType, Pageable pageable) {
        // Ownership check if logged in user is PATIENT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userDetails) {
            boolean isPatient = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));
            if (isPatient) {
                Patient patient = patientRepository.findByEmail(userDetails.getUsername())
                        .orElse(null);
                if (patient != null) {
                    patientId = patient.getId();
                }
            }
        }

        Page<Appointment> page = appointmentRepository.findAppointmentsWithFilters(
                doctorId, patientId, departmentId, date, status, appointmentType, pageable
        );
        return page.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + id));
        checkPatientOwnership(appointment.getPatient().getEmail());
        return mapToResponse(appointment);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentByCode(String appointmentId) {
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with Code: " + appointmentId));
        checkPatientOwnership(appointment.getPatient().getEmail());
        return mapToResponse(appointment);
    }

    @Transactional
    public AppointmentResponse createAppointment(AppointmentCreateRequest request) {
        // Patient Validation
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + request.getPatientId()));
        if (!patient.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inactive patients cannot receive new appointments");
        }

        // Doctor Validation
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + request.getDoctorId()));
        if (!doctor.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inactive doctors cannot receive new appointments");
        }

        // Department Validation
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));
        if (!department.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inactive department selected");
        }

        // Date & Time Validation
        if (request.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment date cannot be in the past");
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time must be strictly before end time");
        }

        // Doctor Schedule Validation
        validateDoctorAvailability(doctor.getId(), request.getAppointmentDate(), request.getStartTime(), request.getEndTime());

        // Double Booking Prevention (Doctor Overlap)
        List<AppointmentStatus> inactiveStatuses = Arrays.asList(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW);
        List<Appointment> docOverlaps = appointmentRepository.findDoctorOverlappingAppointments(
                doctor.getId(), request.getAppointmentDate(), request.getStartTime(), request.getEndTime(), inactiveStatuses, null
        );
        if (!docOverlaps.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor already has an active appointment during this time");
        }

        // Patient Overlap Check
        if (request.getAppointmentType() != AppointmentType.EMERGENCY) {
            List<Appointment> patOverlaps = appointmentRepository.findPatientOverlappingAppointments(
                    patient.getId(), request.getAppointmentDate(), request.getStartTime(), request.getEndTime(), inactiveStatuses, null
            );
            if (!patOverlaps.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient already has an active appointment during this time");
            }
        }

        // Generate visible appointment code APT-YYYY-000001
        String appointmentCode = generateAppointmentCode();

        User currentUser = getCurrentUser();

        Appointment appointment = Appointment.builder()
                .appointmentId(appointmentCode)
                .patient(patient)
                .doctor(doctor)
                .department(department)
                .appointmentDate(request.getAppointmentDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .appointmentType(request.getAppointmentType())
                .reasonForVisit(request.getReasonForVisit())
                .status(AppointmentStatus.SCHEDULED)
                .notes(request.getNotes())
                .createdBy(currentUser)
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.logAction("CREATE_APPOINTMENT", "Appointment", saved.getId().toString(),
                "Created appointment " + saved.getAppointmentId() + " for patient " + patient.getPatientId() + " with Dr. " + doctor.getFirstName() + " " + doctor.getLastName());

        return mapToResponse(saved);
    }

    @Transactional
    public AppointmentResponse updateAppointment(Long id, AppointmentUpdateRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + id));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot edit completed or cancelled appointment");
        }

        if (request.getDoctorId() != null) {
            Doctor doctor = doctorRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + request.getDoctorId()));
            if (!doctor.getIsActive()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected doctor is inactive");
            }
            appointment.setDoctor(doctor);
        }

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));
            appointment.setDepartment(department);
        }

        if (request.getAppointmentDate() != null) {
            appointment.setAppointmentDate(request.getAppointmentDate());
        }
        if (request.getStartTime() != null) {
            appointment.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            appointment.setEndTime(request.getEndTime());
        }
        if (request.getAppointmentType() != null) {
            appointment.setAppointmentType(request.getAppointmentType());
        }
        if (request.getReasonForVisit() != null) {
            appointment.setReasonForVisit(request.getReasonForVisit());
        }
        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        // Revalidate overlap
        List<AppointmentStatus> inactiveStatuses = Arrays.asList(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW);
        List<Appointment> docOverlaps = appointmentRepository.findDoctorOverlappingAppointments(
                appointment.getDoctor().getId(), appointment.getAppointmentDate(), appointment.getStartTime(), appointment.getEndTime(), inactiveStatuses, appointment.getId()
        );
        if (!docOverlaps.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor already has an active appointment during this updated time");
        }

        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.logAction("UPDATE_APPOINTMENT", "Appointment", saved.getId().toString(),
                "Updated appointment details for " + saved.getAppointmentId());

        return mapToResponse(saved);
    }

    @Transactional
    public AppointmentResponse updateStatus(Long id, AppointmentStatus targetStatus, String notes) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + id));

        validateStatusTransition(appointment.getStatus(), targetStatus);

        appointment.setStatus(targetStatus);
        if (notes != null && !notes.isBlank()) {
            appointment.setNotes(notes);
        }

        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.logAction("UPDATE_APPOINTMENT_STATUS", "Appointment", saved.getId().toString(),
                "Updated appointment " + saved.getAppointmentId() + " status to " + targetStatus);

        return mapToResponse(saved);
    }

    @Transactional
    public AppointmentResponse confirmAppointment(Long id) {
        return updateStatus(id, AppointmentStatus.CONFIRMED, "Appointment confirmed");
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long id, String reason) {
        return updateStatus(id, AppointmentStatus.CANCELLED, reason);
    }

    @Transactional
    public AppointmentResponse rescheduleAppointment(Long id, LocalDate newDate, LocalTime newStart, LocalTime newEnd) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + id));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot reschedule completed or cancelled appointment");
        }

        if (newDate.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rescheduled date cannot be in the past");
        }
        if (!newStart.isBefore(newEnd)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time must be strictly before end time");
        }

        validateDoctorAvailability(appointment.getDoctor().getId(), newDate, newStart, newEnd);

        List<AppointmentStatus> inactiveStatuses = Arrays.asList(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW);
        List<Appointment> docOverlaps = appointmentRepository.findDoctorOverlappingAppointments(
                appointment.getDoctor().getId(), newDate, newStart, newEnd, inactiveStatuses, appointment.getId()
        );
        if (!docOverlaps.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor has a time conflict on the selected reschedule slot");
        }

        appointment.setAppointmentDate(newDate);
        appointment.setStartTime(newStart);
        appointment.setEndTime(newEnd);
        appointment.setStatus(AppointmentStatus.RESCHEDULED);

        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.logAction("RESCHEDULE_APPOINTMENT", "Appointment", saved.getId().toString(),
                "Rescheduled appointment " + saved.getAppointmentId() + " to " + newDate + " " + newStart);

        return mapToResponse(saved);
    }

    private void validateDoctorAvailability(Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorAvailability> availabilities = availabilityRepository.findByDoctorIdAndDayOfWeekAndIsActiveTrue(doctorId, dayOfWeek);

        if (availabilities.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor has no active availability schedule configured for " + dayOfWeek);
        }

        boolean fitsInSchedule = availabilities.stream().anyMatch(a ->
                !startTime.isBefore(a.getStartTime()) && !endTime.isAfter(a.getEndTime())
        );

        if (!fitsInSchedule) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected appointment time falls outside doctor's configured working hours");
        }
    }

    private void validateStatusTransition(AppointmentStatus current, AppointmentStatus target) {
        if (current == target) return;

        switch (current) {
            case SCHEDULED:
                if (target == AppointmentStatus.CONFIRMED || target == AppointmentStatus.CANCELLED || target == AppointmentStatus.RESCHEDULED || target == AppointmentStatus.CHECKED_IN) {
                    return;
                }
                break;
            case CONFIRMED:
            case RESCHEDULED:
                if (target == AppointmentStatus.CHECKED_IN || target == AppointmentStatus.CANCELLED || target == AppointmentStatus.NO_SHOW) {
                    return;
                }
                break;
            case CHECKED_IN:
                if (target == AppointmentStatus.IN_CONSULTATION || target == AppointmentStatus.CANCELLED) {
                    return;
                }
                break;
            case IN_CONSULTATION:
                if (target == AppointmentStatus.COMPLETED) {
                    return;
                }
                break;
            case COMPLETED:
            case CANCELLED:
            case NO_SHOW:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot change status of a terminal appointment (" + current + ")");
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status transition from " + current + " to " + target);
    }

    private String generateAppointmentCode() {
        int year = LocalDate.now().getYear();
        long count = appointmentRepository.count() + 1;
        return String.format("APT-%d-%06d", year, count);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userDetails) {
            return userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        }
        return null;
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

    private AppointmentResponse mapToResponse(Appointment a) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .appointmentId(a.getAppointmentId())
                .patientId(a.getPatient().getId())
                .patientCode(a.getPatient().getPatientId())
                .patientName(a.getPatient().getFirstName() + " " + a.getPatient().getLastName())
                .patientPhone(a.getPatient().getPhone())
                .doctorId(a.getDoctor().getId())
                .doctorCode(a.getDoctor().getDoctorId())
                .doctorName("Dr. " + a.getDoctor().getFirstName() + " " + a.getDoctor().getLastName())
                .doctorSpecialization(a.getDoctor().getSpecialization())
                .departmentId(a.getDepartment().getId())
                .departmentName(a.getDepartment().getDepartmentName())
                .appointmentDate(a.getAppointmentDate())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .appointmentType(a.getAppointmentType())
                .reasonForVisit(a.getReasonForVisit())
                .status(a.getStatus())
                .notes(a.getNotes())
                .createdById(a.getCreatedBy() != null ? a.getCreatedBy().getId() : null)
                .createdByName(a.getCreatedBy() != null ? a.getCreatedBy().getFullName() : null)
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
