package com.hospital.dto;

import com.hospital.entity.AllocationType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BedAllocationRequest {

    @NotNull(message = "Ward ID is required")
    private Long wardId;

    @NotNull(message = "Bed ID is required")
    private Long bedId;

    private AllocationType allocationType;
    private String reason;
}
