package com.hospital.dto;

import com.hospital.entity.RadiologyReportStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RadiologyReportRequest {
    @NotBlank(message = "Findings are required")
    private String findings;
    private String impression;
    private String recommendations;
    private RadiologyReportStatus status;
    private Boolean isFinal;
}
