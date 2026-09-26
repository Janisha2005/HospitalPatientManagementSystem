package com.hospital.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabOrderItemRequest {
    private Long labTestId;
    private String sampleType;
    private String resultValue;
    private String resultUnit;
    private String referenceRange;
    private Boolean isAbnormal;
    private String remarks;
}
