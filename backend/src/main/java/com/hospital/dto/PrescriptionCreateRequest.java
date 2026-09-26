package com.hospital.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionCreateRequest {
    @NotNull(message = "Patient ID is required")
    private Long patientId;
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;
    private Long opdVisitId;
    private String clinicalNotes;
    @NotEmpty(message = "Prescription must contain at least one medicine item")
    @Valid
    private List<PrescriptionItemRequest> items;
}
