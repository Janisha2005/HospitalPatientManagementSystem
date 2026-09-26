package com.hospital.service;

import com.hospital.dto.DoctorAvailabilityRequest;
import com.hospital.dto.DoctorAvailabilityResponse;
import com.hospital.dto.TimeSlotDto;
import com.hospital.entity.Appointment;
import com.hospital.entity.AppointmentStatus;
import com.hospital.entity.Doctor;
import com.hospital.entity.DoctorAvailability;
import com.hospital.exception.ResourceNotFoundException;

import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.DoctorAvailabilityRepository;
import com.hospital.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorAvailabilityService {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<DoctorAvailabilityResponse> getDoctorAvailability(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with ID: " + doctorId);
        }
        return availabilityRepository.findByDoctorIdAndIsActiveTrue(doctorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorAvailabilityResponse createAvailability(Long doctorId, DoctorAvailabilityRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

        if (!doctor.getIsActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot set availability for inactive doctor");
        }

        validateTimesAndDuration(request.getStartTime(), request.getEndTime(), request.getSlotDurationMinutes());

        // Overlap check
        List<DoctorAvailability> overlaps = availabilityRepository.findOverlappingAvailability(
                doctorId, request.getDayOfWeek(), request.getStartTime(), request.getEndTime(), null
        );
        if (!overlaps.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor already has overlapping availability on " + request.getDayOfWeek());
        }

        DoctorAvailability availability = DoctorAvailability.builder()
                .doctor(doctor)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .slotDurationMinutes(request.getSlotDurationMinutes())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        DoctorAvailability saved = availabilityRepository.save(availability);

        auditLogService.logAction("CREATE_DOCTOR_AVAILABILITY", "DoctorAvailability", saved.getId().toString(),
                "Configured availability for Dr. " + doctor.getFirstName() + " " + doctor.getLastName() + " on " + request.getDayOfWeek());

        return mapToResponse(saved);
    }

    @Transactional
    public DoctorAvailabilityResponse updateAvailability(Long doctorId, Long availabilityId, DoctorAvailabilityRequest request) {
        DoctorAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability record not found with ID: " + availabilityId));

        if (!availability.getDoctor().getId().equals(doctorId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Availability does not belong to doctor ID: " + doctorId);
        }

        validateTimesAndDuration(request.getStartTime(), request.getEndTime(), request.getSlotDurationMinutes());

        List<DoctorAvailability> overlaps = availabilityRepository.findOverlappingAvailability(
                doctorId, request.getDayOfWeek(), request.getStartTime(), request.getEndTime(), availabilityId
        );
        if (!overlaps.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor already has overlapping availability on " + request.getDayOfWeek());
        }

        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setSlotDurationMinutes(request.getSlotDurationMinutes());
        if (request.getIsActive() != null) {
            availability.setIsActive(request.getIsActive());
        }

        DoctorAvailability saved = availabilityRepository.save(availability);

        auditLogService.logAction("UPDATE_DOCTOR_AVAILABILITY", "DoctorAvailability", saved.getId().toString(),
                "Updated availability for doctor ID: " + doctorId);

        return mapToResponse(saved);
    }

    @Transactional
    public void deleteAvailability(Long doctorId, Long availabilityId) {
        DoctorAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability record not found with ID: " + availabilityId));

        if (!availability.getDoctor().getId().equals(doctorId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Availability does not belong to doctor ID: " + doctorId);
        }

        availabilityRepository.delete(availability);

        auditLogService.logAction("DELETE_DOCTOR_AVAILABILITY", "DoctorAvailability", availabilityId.toString(),
                "Deleted availability record ID: " + availabilityId);
    }

    @Transactional(readOnly = true)
    public List<TimeSlotDto> getAvailableSlots(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

        if (!doctor.getIsActive()) {
            return new ArrayList<>();
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorAvailability> availabilities = availabilityRepository.findByDoctorIdAndDayOfWeekAndIsActiveTrue(doctorId, dayOfWeek);

        if (availabilities.isEmpty()) {
            return new ArrayList<>();
        }

        // Fetch existing non-cancelled/non-noshow appointments for doctor on date
        List<AppointmentStatus> inactiveStatuses = Arrays.asList(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW);
        List<Appointment> existingAppointments = appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusNotIn(
                doctorId, date, inactiveStatuses
        );

        List<TimeSlotDto> slots = new ArrayList<>();

        for (DoctorAvailability avail : availabilities) {
            LocalTime current = avail.getStartTime();
            LocalTime end = avail.getEndTime();
            int duration = avail.getSlotDurationMinutes();

            while (current.plusMinutes(duration).isBefore(end) || current.plusMinutes(duration).equals(end)) {
                LocalTime slotStart = current;
                LocalTime slotEnd = current.plusMinutes(duration);

                // Check overlap with existing appointments
                boolean isBooked = existingAppointments.stream().anyMatch(apt ->
                        (apt.getStartTime().isBefore(slotEnd) && apt.getEndTime().isAfter(slotStart))
                );

                slots.add(TimeSlotDto.builder()
                        .startTime(slotStart)
                        .endTime(slotEnd)
                        .isAvailable(!isBooked)
                        .build());

                current = slotEnd;
            }
        }

        return slots;
    }

    private void validateTimesAndDuration(LocalTime start, LocalTime end, Integer durationMinutes) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time must be strictly before end time");
        }
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slot duration must be positive");
        }
    }

    private DoctorAvailabilityResponse mapToResponse(DoctorAvailability da) {
        return DoctorAvailabilityResponse.builder()
                .id(da.getId())
                .doctorId(da.getDoctor().getId())
                .doctorName("Dr. " + da.getDoctor().getFirstName() + " " + da.getDoctor().getLastName())
                .dayOfWeek(da.getDayOfWeek())
                .startTime(da.getStartTime())
                .endTime(da.getEndTime())
                .slotDurationMinutes(da.getSlotDurationMinutes())
                .isActive(da.getIsActive())
                .createdAt(da.getCreatedAt())
                .updatedAt(da.getUpdatedAt())
                .build();
    }
}
