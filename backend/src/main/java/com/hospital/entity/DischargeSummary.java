package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "discharge_summaries", indexes = {
        @Index(name = "idx_dsc_summary_id", columnList = "discharge_summary_id"),
        @Index(name = "idx_dsc_admission", columnList = "admission_id"),
        @Index(name = "idx_dsc_patient", columnList = "patient_id"),
        @Index(name = "idx_dsc_doctor", columnList = "doctor_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DischargeSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "discharge_summary_id", nullable = false, unique = true, length = 30)
    private String dischargeSummaryId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_id", nullable = false, unique = true)
    private IpdAdmission admission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "admission_summary", columnDefinition = "TEXT")
    private String admissionSummary;

    @Column(name = "clinical_course", columnDefinition = "TEXT")
    private String clinicalCourse;

    @Column(name = "final_diagnosis", columnDefinition = "TEXT")
    private String finalDiagnosis;

    @Column(name = "procedures_summary", columnDefinition = "TEXT")
    private String proceduresSummary;

    @Column(name = "investigation_summary", columnDefinition = "TEXT")
    private String investigationSummary;

    @Column(name = "treatment_summary", columnDefinition = "TEXT")
    private String treatmentSummary;

    @Column(name = "medication_summary", columnDefinition = "TEXT")
    private String medicationSummary;

    @Column(name = "condition_at_discharge", length = 100)
    private String conditionAtDischarge;

    @Column(name = "follow_up_instructions", columnDefinition = "TEXT")
    private String followUpInstructions;

    @Column(name = "discharge_date", nullable = false)
    private LocalDate dischargeDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
