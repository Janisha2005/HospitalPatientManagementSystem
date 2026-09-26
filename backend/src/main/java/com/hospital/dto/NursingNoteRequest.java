package com.hospital.dto;

import com.hospital.entity.NursingNoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NursingNoteRequest {

    private LocalDateTime noteDatetime;

    @NotNull(message = "Note type is required")
    private NursingNoteType noteType;

    @NotBlank(message = "Note text is required")
    private String noteText;
}
