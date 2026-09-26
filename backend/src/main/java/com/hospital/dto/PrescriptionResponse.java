package com.hospital.dto;

import com.hospital.entity.PrescriptionStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionResponse {
    private Long id;
    private String prescriptionId;
    private Long patientId;
    private String patientCode;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Long opdVisitId;
    private String opdVisitCode;
    private LocalDate prescriptionDate;
    private PrescriptionStatus status;
    private String clinicalNotes;
    private List<PrescriptionItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
