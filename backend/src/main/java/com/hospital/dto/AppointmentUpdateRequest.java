package com.hospital.dto;

import com.hospital.entity.AppointmentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentUpdateRequest {

    private Long doctorId;
    private Long departmentId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private AppointmentType appointmentType;

    @Size(max = 500, message = "Reason for visit cannot exceed 500 characters")
    private String reasonForVisit;

    private String notes;
}
