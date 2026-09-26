package com.hospital.dto;

import com.hospital.entity.TransferStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WardTransferResponse {
    private Long id;
    private String transferId;
    private Long admissionId;
    private String admissionCode;
    private Long patientId;
    private String patientName;
    private Long fromWardId;
    private String fromWardName;
    private Long fromBedId;
    private String fromBedCode;
    private Long toWardId;
    private String toWardName;
    private Long toBedId;
    private String toBedCode;
    private LocalDateTime transferDatetime;
    private String reason;
    private String requestedBy;
    private String approvedBy;
    private TransferStatus status;
    private LocalDateTime createdAt;
}
