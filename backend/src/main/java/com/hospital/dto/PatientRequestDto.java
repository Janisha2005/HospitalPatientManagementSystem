package com.hospital.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientRequestDto {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(Male|Female|Other)$", message = "Gender must be Male, Female, or Other")
    private String gender;

    @Pattern(regexp = "^(A\\+|A-|B\\+|B-|AB\\+|AB-|O\\+|O-)?$", message = "Invalid blood group")
    private String bloodGroup;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(?:\\+91[\\s-]?)?[6-9]\\d{9}$", message = "Invalid Indian phone number format (10 digits starting with 6-9)")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    // Indian Address DTO fields
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String district;
    private String state;

    @Pattern(regexp = "^([1-9][0-9]{5})?$", message = "Invalid Indian PIN code (6 digits)")
    private String pincode;

    @Builder.Default
    private String country = "India";

    @NotBlank(message = "Emergency contact name is required")
    private String emergencyContactName;

    @NotBlank(message = "Emergency contact phone is required")
    @Pattern(regexp = "^(?:\\+91[\\s-]?)?[6-9]\\d{9}$", message = "Invalid emergency contact Indian phone number format")
    private String emergencyContactPhone;

    @NotBlank(message = "Emergency contact relationship is required")
    private String emergencyContactRelationship;

    private Boolean isActive;
}
