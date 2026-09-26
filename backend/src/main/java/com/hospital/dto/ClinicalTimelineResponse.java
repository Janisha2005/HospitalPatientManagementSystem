package com.hospital.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicalTimelineResponse {
    private Long patientId;
    private String patientCode;
    private String patientName;
    private int totalEvents;
    private List<ClinicalTimelineEventDto> events;
}
