package com.hospital.dto;

import com.hospital.entity.ConditionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConditionRequest {
    @NotBlank(message = "Condition name is required")
    private String conditionName;
    private String description;
    private LocalDate diagnosedDate;
    @NotNull(message = "Condition status is required")
    private ConditionStatus status;
}
