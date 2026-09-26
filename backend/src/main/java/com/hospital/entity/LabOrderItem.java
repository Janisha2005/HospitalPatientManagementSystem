package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "lab_order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_order_id", nullable = false)
    private LabOrder labOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lab_test_id", nullable = false)
    private LabTest labTest;

    @Column(name = "sample_type", length = 50)
    private String sampleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LabItemStatus status;

    @Column(name = "result_value", length = 100)
    private String resultValue;

    @Column(name = "result_unit", length = 30)
    private String resultUnit;

    @Column(name = "reference_range", length = 150)
    private String referenceRange;

    @Column(name = "result_comment", columnDefinition = "TEXT")
    private String resultComment;

    @Column(name = "is_abnormal")
    private Boolean abnormal;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sample_collected_by")
    private User sampleCollectedBy;

    @Column(name = "sample_collected_at")
    private LocalDateTime sampleCollectedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
