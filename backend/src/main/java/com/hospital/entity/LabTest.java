package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "lab_tests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_code", nullable = false, unique = true, length = 30)
    private String testCode;

    @Column(name = "test_name", nullable = false, length = 150)
    private String testName;

    @Column(length = 50)
    private String category; // e.g., Hematology, Biochemistry, Microbiology

    @Column(name = "sample_type", length = 50)
    private String sampleType; // e.g. Blood, Urine, Serum

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "normal_range_description", length = 255)
    private String normalRangeDescription;

    @Column(length = 30)
    private String unit;

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
