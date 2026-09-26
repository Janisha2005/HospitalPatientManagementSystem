package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "nursing_notes", indexes = {
        @Index(name = "idx_nn_note_id", columnList = "note_id"),
        @Index(name = "idx_nn_admission", columnList = "admission_id"),
        @Index(name = "idx_nn_patient", columnList = "patient_id"),
        @Index(name = "idx_nn_datetime", columnList = "note_datetime")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NursingNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "note_id", nullable = false, unique = true, length = 30)
    private String noteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_id", nullable = false)
    private IpdAdmission admission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nurse_id")
    private User nurse;

    @Column(name = "note_datetime", nullable = false)
    private LocalDateTime noteDatetime;

    @Enumerated(EnumType.STRING)
    @Column(name = "note_type", nullable = false, length = 30)
    private NursingNoteType noteType;

    @Column(name = "note_text", nullable = false, columnDefinition = "TEXT")
    private String noteText;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
