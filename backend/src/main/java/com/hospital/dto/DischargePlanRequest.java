package com.hospital.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DischargePlanRequest {

    @NotNull(message = "Planned discharge date is required")
    private LocalDate plannedDischargeDate;

    private String dischargeCondition;
    private Boolean followUpRequired;
    private LocalDate followUpDate;
    private String followUpInstructions;
    private String homeCareInstructions;
}
