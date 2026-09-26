package com.hospital.dto;

import com.hospital.entity.NursingNoteType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NursingNoteResponse {
    private Long id;
    private String noteId;
    private Long admissionId;
    private Long patientId;
    private String patientName;
    private Long nurseId;
    private String nurseName;
    private LocalDateTime noteDatetime;
    private NursingNoteType noteType;
    private String noteText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
