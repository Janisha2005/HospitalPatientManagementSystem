package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "ipd_admissions", indexes = {
        @Index(name = "idx_ipd_admission_id", columnList = "admission_id"),
        @Index(name = "idx_ipd_patient", columnList = "patient_id"),
        @Index(name = "idx_ipd_doctor", columnList = "admitting_doctor_id"),
        @Index(name = "idx_ipd_ward", columnList = "ward_id"),
        @Index(name = "idx_ipd_bed", columnList = "bed_id"),
        @Index(name = "idx_ipd_status", columnList = "status"),
        @Index(name = "idx_ipd_patient_status", columnList = "patient_id, status"),
        @Index(name = "idx_ipd_adm_date", columnList = "admission_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IpdAdmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false, unique = true, length = 30)
    private String admissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admitting_doctor_id", nullable = false)
    private Doctor admittingDoctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bed_id")
    private Bed bed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opd_visit_id")
    private OpdVisit opdVisit;

    @Enumerated(EnumType.STRING)
    @Column(name = "admission_type", nullable = false, length = 30)
    private AdmissionType admissionType;

    @Column(name = "admission_date", nullable = false)
    private LocalDate admissionDate;

    @Column(name = "admission_time", nullable = false)
    private LocalTime admissionTime;

    @Column(name = "reason_for_admission", nullable = false, columnDefinition = "TEXT")
    private String reasonForAdmission;

    @Column(name = "clinical_summary", columnDefinition = "TEXT")
    private String clinicalSummary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private AdmissionStatus status = AdmissionStatus.REQUESTED;

    @Column(name = "expected_discharge_date")
    private LocalDate expectedDischargeDate;

    @Column(name = "actual_discharge_date")
    private LocalDate actualDischargeDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "discharge_type", length = 30)
    private DischargeType dischargeType;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
