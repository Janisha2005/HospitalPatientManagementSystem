package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lab_orders", indexes = {
    @Index(name = "idx_lab_patient", columnList = "patient_id"),
    @Index(name = "idx_lab_doctor", columnList = "doctor_id"),
    @Index(name = "idx_lab_opd", columnList = "opd_visit_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lab_order_id", nullable = false, unique = true, length = 30)
    private String labOrderId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "opd_visit_id")
    private OpdVisit opdVisit;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LabOrderStatus status;

    @Column(name = "clinical_note", columnDefinition = "TEXT")
    private String clinicalNote;

    @Column(name = "sample_collected_at")
    private LocalDateTime sampleCollectedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sample_collected_by")
    private User sampleCollectedBy;

    @OneToMany(mappedBy = "labOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<LabOrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
