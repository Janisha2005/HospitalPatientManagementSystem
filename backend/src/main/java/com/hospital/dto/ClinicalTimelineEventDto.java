package com.hospital.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicalTimelineEventDto {
    private String eventType; // e.g., APPOINTMENT, OPD_VISIT, DIAGNOSIS, TREATMENT_PLAN, PRESCRIPTION, LAB_ORDER, LAB_RESULT, RADIOLOGY_ORDER, RADIOLOGY_REPORT, ALLERGY, CONDITION
    private String eventId;
    private String title;
    private String summary;
    private String status;
    private String doctorName;
    private LocalDateTime eventTimestamp;
    private Object details;
}
