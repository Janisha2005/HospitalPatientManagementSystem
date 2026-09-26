package com.hospital.dto;

import com.hospital.entity.OpdVisitStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpdStatusRequest {

    @NotNull(message = "Visit status is required")
    private OpdVisitStatus status;
}
