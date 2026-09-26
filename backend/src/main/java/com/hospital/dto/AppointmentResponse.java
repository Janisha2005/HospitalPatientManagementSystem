package com.hospital.dto;

import com.hospital.entity.AppointmentStatus;
import com.hospital.entity.AppointmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {

    private Long id;
    private String appointmentId;

    private Long patientId;
    private String patientCode;
    private String patientName;
    private String patientPhone;

    private Long doctorId;
    private String doctorCode;
    private String doctorName;
    private String doctorSpecialization;

    private Long departmentId;
    private String departmentName;

    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private AppointmentType appointmentType;
    private String reasonForVisit;
    private AppointmentStatus status;
    private String notes;

    private Long createdById;
    private String createdByName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
