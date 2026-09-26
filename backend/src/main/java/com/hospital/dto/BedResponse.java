package com.hospital.dto;

import com.hospital.entity.BedStatus;
import com.hospital.entity.BedType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BedResponse {
    private Long id;
    private String bedCode;
    private Long wardId;
    private String wardCode;
    private String wardName;
    private String bedNumber;
    private BedType bedType;
    private BedStatus status;
    private Boolean isActive;
    private String currentPatientName;
    private String currentAdmissionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
