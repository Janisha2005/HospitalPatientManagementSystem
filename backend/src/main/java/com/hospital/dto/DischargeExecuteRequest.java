package com.hospital.dto;

import com.hospital.entity.DischargeType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DischargeExecuteRequest {

    @NotNull(message = "Discharge type is required")
    private DischargeType dischargeType;

    @NotNull(message = "Discharge date is required")
    private LocalDate dischargeDate;

    private String conditionAtDischarge;
    private String followUpInstructions;
}
