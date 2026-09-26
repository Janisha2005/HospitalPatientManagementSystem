package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "pharmacy_dispensings", indexes = {
        @Index(name = "idx_dsp_prescription", columnList = "prescription_id"),
        @Index(name = "idx_dsp_patient", columnList = "patient_id"),
        @Index(name = "idx_dsp_medicine", columnList = "medicine_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyDispensing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dispensing_id", nullable = false, unique = true, length = 30)
    private String dispensingId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prescription_id")
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prescription_item_id")
    private PrescriptionItem prescriptionItem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batch_id", nullable = false)
    private MedicineBatch batch;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ipd_admission_id")
    private IpdAdmission ipdAdmission;

    @Column(name = "prescribed_quantity", nullable = false)
    private Integer prescribedQuantity;

    @Column(name = "dispensed_quantity", nullable = false)
    private Integer dispensedQuantity;

    @Column(name = "remaining_quantity", nullable = false)
    private Integer remainingQuantity;

    @Column(name = "dispensed_by", length = 100)
    private String dispensedBy;

    @Column(name = "dispensed_datetime", nullable = false)
    private LocalDateTime dispensedDatetime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private DispensingStatus status = DispensingStatus.FULLY_DISPENSED;

    @Column(length = 255)
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
