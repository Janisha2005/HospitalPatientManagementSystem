package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "beds",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ward_bed_number", columnNames = {"ward_id", "bed_number"})
        },
        indexes = {
                @Index(name = "idx_bed_code", columnList = "bed_code"),
                @Index(name = "idx_bed_ward", columnList = "ward_id"),
                @Index(name = "idx_bed_status", columnList = "status"),
                @Index(name = "idx_bed_ward_status", columnList = "ward_id, status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bed_code", nullable = false, unique = true, length = 30)
    private String bedCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;

    @Column(name = "bed_number", nullable = false, length = 30)
    private String bedNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "bed_type", nullable = false, length = 30)
    private BedType bedType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private BedStatus status = BedStatus.AVAILABLE;

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
