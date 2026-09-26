package com.hospital.dto;

import com.hospital.entity.EhrRecordType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EhrRecordRequest {
    @NotNull(message = "Patient ID is required")
    private Long patientId;
    private Long opdVisitId;
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;
    @NotNull(message = "Department ID is required")
    private Long departmentId;
    @NotNull(message = "Record type is required")
    private EhrRecordType recordType;
    private String clinicalSummary;
    private String diagnosisSummary;
    private String treatmentSummary;
}
