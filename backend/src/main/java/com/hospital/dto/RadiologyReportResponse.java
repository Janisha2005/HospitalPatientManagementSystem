package com.hospital.dto;

import com.hospital.entity.RadiologyReportStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RadiologyReportResponse {
    private Long id;
    private String reportId;
    private Long radiologyOrderId;
    private String radiologyOrderCode;
    private Long patientId;
    private String patientName;
    private Long radiologistId;
    private String radiologistName;
    private String findings;
    private String impression;
    private RadiologyReportStatus status;
    private LocalDateTime reportedAt;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
