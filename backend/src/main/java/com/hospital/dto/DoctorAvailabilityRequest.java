package com.hospital.dto;

import java.time.DayOfWeek;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorAvailabilityRequest {

    @NotNull(message = "Day of week is required")
    private java.time.DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Slot duration in minutes is required")
    @Positive(message = "Slot duration must be positive")
    private Integer slotDurationMinutes;

    private Boolean isActive;
}
