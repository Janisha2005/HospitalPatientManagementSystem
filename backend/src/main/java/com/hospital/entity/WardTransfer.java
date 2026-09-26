package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ward_transfers", indexes = {
        @Index(name = "idx_trf_id", columnList = "transfer_id"),
        @Index(name = "idx_trf_admission", columnList = "admission_id"),
        @Index(name = "idx_trf_patient", columnList = "patient_id"),
        @Index(name = "idx_trf_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WardTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transfer_id", nullable = false, unique = true, length = 30)
    private String transferId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_id", nullable = false)
    private IpdAdmission admission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_ward_id", nullable = false)
    private Ward fromWard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_bed_id", nullable = false)
    private Bed fromBed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_ward_id", nullable = false)
    private Ward toWard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_bed_id", nullable = false)
    private Bed toBed;

    @Column(name = "transfer_datetime", nullable = false)
    private LocalDateTime transferDatetime;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "requested_by", length = 50)
    private String requestedBy;

    @Column(name = "approved_by", length = 50)
    private String approvedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private TransferStatus status = TransferStatus.COMPLETED;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
