package com.hospital.dto;

import com.hospital.entity.AllergySeverity;
import com.hospital.entity.AllergyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllergyRequest {
    @NotBlank(message = "Allergen is required")
    private String allergen;
    private String allergyType;
    private String reaction;
    @NotNull(message = "Severity is required")
    private AllergySeverity severity;
    private AllergyStatus status;
}
