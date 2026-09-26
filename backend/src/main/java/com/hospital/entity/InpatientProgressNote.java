package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inpatient_progress_notes", indexes = {
        @Index(name = "idx_pn_note_id", columnList = "progress_note_id"),
        @Index(name = "idx_pn_admission", columnList = "admission_id"),
        @Index(name = "idx_pn_patient", columnList = "patient_id"),
        @Index(name = "idx_pn_doctor", columnList = "doctor_id"),
        @Index(name = "idx_pn_datetime", columnList = "note_datetime")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InpatientProgressNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "progress_note_id", nullable = false, unique = true, length = 30)
    private String progressNoteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_id", nullable = false)
    private IpdAdmission admission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "note_datetime", nullable = false)
    private LocalDateTime noteDatetime;

    @Column(name = "clinical_assessment", columnDefinition = "TEXT")
    private String clinicalAssessment;

    @Column(name = "progress_summary", nullable = false, columnDefinition = "TEXT")
    private String progressSummary;

    @Column(name = "diagnosis_update", columnDefinition = "TEXT")
    private String diagnosisUpdate;

    @Column(name = "treatment_update", columnDefinition = "TEXT")
    private String treatmentUpdate;

    @Column(name = "follow_up_plan", columnDefinition = "TEXT")
    private String followUpPlan;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
