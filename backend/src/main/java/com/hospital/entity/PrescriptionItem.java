package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prescription_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @Column(name = "medicine_name", nullable = false, length = 150)
    private String medicineName;

    @Column(length = 50)
    private String strength;

    @Column(length = 50)
    private String dosage; // e.g., "1 Tablet"

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PrescriptionRoute route;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PrescriptionFrequency frequency;

    @Column(name = "duration_value")
    private Integer durationValue;

    @Column(name = "duration_unit", length = 20)
    private String durationUnit; // e.g., DAYS, WEEKS

    @Column(length = 50)
    private String quantity;

    @Column(length = 255)
    private String instructions;
}
