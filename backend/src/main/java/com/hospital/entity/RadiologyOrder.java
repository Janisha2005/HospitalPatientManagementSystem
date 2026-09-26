package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "radiology_orders", indexes = {
    @Index(name = "idx_rad_patient", columnList = "patient_id"),
    @Index(name = "idx_rad_doctor", columnList = "doctor_id"),
    @Index(name = "idx_rad_opd", columnList = "opd_visit_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RadiologyOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "radiology_order_id", nullable = false, unique = true, length = 30)
    private String radiologyOrderId;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "radiology_test_id", nullable = false)
    private RadiologyTest radiologyTest;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RadiologyOrderStatus status;

    @Column(name = "clinical_indication", columnDefinition = "TEXT")
    private String clinicalIndication;

    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;

    @Column(name = "performed_at")
    private LocalDateTime performedAt;

    @OneToOne(mappedBy = "radiologyOrder", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private RadiologyReport report;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
