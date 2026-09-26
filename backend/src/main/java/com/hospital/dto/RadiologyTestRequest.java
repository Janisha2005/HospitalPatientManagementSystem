package com.hospital.dto;

import com.hospital.entity.RadiologyModality;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RadiologyTestRequest {
    @NotBlank(message = "Test code is required")
    private String testCode;
    @NotBlank(message = "Test name is required")
    private String testName;
    @NotNull(message = "Modality is required")
    private RadiologyModality modality;
    private String bodyPart;
    private String description;
    private Boolean isActive;
}
