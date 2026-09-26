package com.hospital.dto;

import com.hospital.entity.BedStatus;
import com.hospital.entity.BedType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BedRequest {

    @NotBlank(message = "Bed code is required")
    private String bedCode;

    @NotNull(message = "Ward ID is required")
    private Long wardId;

    @NotBlank(message = "Bed number is required")
    private String bedNumber;

    @NotNull(message = "Bed type is required")
    private BedType bedType;

    private BedStatus status;
    private Boolean isActive;
}
