package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "medicines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medicine_code", nullable = false, unique = true, length = 30)
    private String medicineCode;

    @Column(name = "medicine_name", nullable = false, length = 150)
    private String medicineName;

    @Column(name = "generic_name", length = 150)
    private String genericName;

    @Column(length = 50)
    private String strength;

    @Column(name = "dosage_form", length = 50)
    private String dosageForm; // e.g. Tablet, Capsule, Syrup, Injection

    @Column(length = 100)
    private String manufacturer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private MedicineCategory category;

    @Column(length = 30)
    private String unit; // e.g. Strip, Bottle, Box, Vial

    @Column(name = "reorder_level")
    @Builder.Default
    private Integer reorderLevel = 50;

    @Column(name = "maximum_stock_level")
    @Builder.Default
    private Integer maximumStockLevel = 500;

    @Column(name = "is_prescription_required")
    @Builder.Default
    private Boolean isPrescriptionRequired = true;

    @Column(name = "is_controlled")
    @Builder.Default
    private Boolean isControlled = false;

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
