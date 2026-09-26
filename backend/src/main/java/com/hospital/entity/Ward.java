package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wards", indexes = {
        @Index(name = "idx_ward_code", columnList = "ward_code"),
        @Index(name = "idx_ward_dept", columnList = "department_id"),
        @Index(name = "idx_ward_type", columnList = "ward_type"),
        @Index(name = "idx_ward_status", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ward_code", nullable = false, unique = true, length = 30)
    private String wardCode;

    @Column(name = "ward_name", nullable = false, length = 100)
    private String wardName;

    @Enumerated(EnumType.STRING)
    @Column(name = "ward_type", nullable = false, length = 30)
    private WardType wardType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(length = 50)
    private String floor;

    @Column(length = 50)
    private String building;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_policy", nullable = false, length = 20)
    @Builder.Default
    private WardGenderPolicy genderPolicy = WardGenderPolicy.MIXED;

    @Column(nullable = false)
    @Builder.Default
    private Integer capacity = 10;

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
