package com.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDto {
    private Long id;
    private String supplierCode;

    @NotBlank(message = "Supplier name is required")
    private String supplierName;

    private String contactPerson;

    @Pattern(regexp = "^(\\+91)?[0-9]{10}$", message = "Phone must be a valid 10-digit Indian phone number")
    private String phone;

    private String email;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String district;
    private String state;

    @Pattern(regexp = "^$|^[1-9][0-9]{5}$", message = "PIN code must be a valid 6-digit Indian postal code")
    private String pincode;

    private String gstNumber;
    private String drugLicenseNumber;
    private String paymentTerms;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
