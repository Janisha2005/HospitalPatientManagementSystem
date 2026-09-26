package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false, unique = true, length = 30)
    private String patientId;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, length = 10)
    private String gender;

    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    // Indian Address Model
    @Column(name = "address_line_1", length = 150)
    private String addressLine1;

    @Column(name = "address_line_2", length = 150)
    private String addressLine2;

    @Column(length = 50)
    private String city;

    @Column(length = 50)
    private String district;

    @Column(length = 50)
    private String state;

    @Column(length = 6)
    private String pincode;

    @Column(length = 50)
    @Builder.Default
    private String country = "India";

    @Column(name = "emergency_contact_name", nullable = false, length = 100)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", nullable = false, length = 20)
    private String emergencyContactPhone;

    @Column(name = "emergency_contact_relationship", nullable = false, length = 50)
    private String emergencyContactRelationship;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
