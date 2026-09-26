package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "charge_masters", indexes = {
        @Index(name = "idx_chg_code", columnList = "charge_code", unique = true),
        @Index(name = "idx_chg_category", columnList = "charge_category"),
        @Index(name = "idx_chg_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargeMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "charge_code", nullable = false, unique = true, length = 30)
    private String chargeCode; // CHG-YYYY-000001

    @Column(name = "charge_name", nullable = false, length = 150)
    private String chargeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "charge_category", nullable = false, length = 30)
    private ChargeCategory chargeCategory;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(length = 30)
    private String unit; // per day, per test, per consultation, unit

    @Column(name = "base_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseRate;

    @Column(name = "tax_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxPercentage = BigDecimal.ZERO;

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
