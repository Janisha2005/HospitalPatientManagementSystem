package com.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabTestRequest {
    @NotBlank(message = "Test code is required")
    private String testCode;
    @NotBlank(message = "Test name is required")
    private String testName;
    private String category;
    private String sampleType;
    private String description;
    private String normalRangeDescription;
    private String unit;
    private Boolean isActive;
}
