package com.hospital.dto;

import com.hospital.entity.BillingAccountStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingAccountDto {
    private Long id;
    private String accountNumber;
    private Long patientId;
    private String patientName;
    private String patientCode;
    private BillingAccountStatus accountStatus;
    private BigDecimal creditLimit;
    private BigDecimal currentBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
