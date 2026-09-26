package com.hospital.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabTestResponse {
    private Long id;
    private String testCode;
    private String testName;
    private String category;
    private String sampleType;
    private String description;
    private String normalRangeDescription;
    private String unit;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
