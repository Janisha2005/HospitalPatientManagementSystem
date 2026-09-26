package com.hospital.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WardTransferRequest {

    @NotNull(message = "Target Ward ID is required")
    private Long toWardId;

    @NotNull(message = "Target Bed ID is required")
    private Long toBedId;

    private String reason;
}
