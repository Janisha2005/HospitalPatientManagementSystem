package com.hospital.dto;

import com.hospital.entity.EhrRecordType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EhrRecordResponse {
    private Long id;
    private String ehrRecordId;
    private Long patientId;
    private String patientCode;
    private String patientName;
    private Long opdVisitId;
    private String opdVisitCode;
    private Long doctorId;
    private String doctorName;
    private Long departmentId;
    private String departmentName;
    private EhrRecordType recordType;
    private String clinicalSummary;
    private String diagnosisSummary;
    private String treatmentSummary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
