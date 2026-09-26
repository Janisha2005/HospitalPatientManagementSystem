package com.hospital.dto;

import com.hospital.entity.LabItemStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabOrderItemResponse {
    private Long id;
    private Long labTestId;
    private String testCode;
    private String testName;
    private String sampleType;
    private LabItemStatus status;
    private String resultValue;
    private String resultUnit;
    private String referenceRange;
    private String resultComment;
    private String verifiedByName;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
