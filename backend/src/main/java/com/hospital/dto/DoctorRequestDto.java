package com.hospital.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorRequestDto {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(?:\\+91[\\s-]?)?[6-9]\\d{9}$", message = "Invalid Indian phone number format (10 digits starting with 6-9)")
    private String phone;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    @NotBlank(message = "Qualification is required")
    private String qualification;

    @NotBlank(message = "Medical registration / license number is required")
    private String licenseNumber;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Consultation fee is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Consultation fee cannot be negative")
    private BigDecimal consultationFee;

    // Indian Address Fields
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String district;
    private String state;

    @Pattern(regexp = "^([1-9][0-9]{5})?$", message = "Invalid Indian PIN code (6 digits)")
    private String pincode;

    @Builder.Default
    private String country = "India";

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    private Boolean isActive;
}
