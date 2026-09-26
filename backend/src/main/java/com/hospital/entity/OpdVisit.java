package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "opd_visits", indexes = {
    @Index(name = "idx_opd_doctor_date_queue", columnList = "doctor_id, visit_date, queue_number"),
    @Index(name = "idx_opd_patient", columnList = "patient_id"),
    @Index(name = "idx_opd_dept_date", columnList = "department_id, visit_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpdVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "opd_visit_id", nullable = false, unique = true, length = 30)
    private String opdVisitId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "queue_number", nullable = false)
    private Integer queueNumber;

    @Column(name = "chief_complaint", columnDefinition = "TEXT")
    private String chiefComplaint;

    @Column(name = "clinical_notes", columnDefinition = "TEXT")
    private String clinicalNotes;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "treatment_plan", columnDefinition = "TEXT")
    private String treatmentPlan;

    // Vitals
    @Column(name = "vital_temperature", precision = 4, scale = 1)
    private BigDecimal vitalTemperature;

    @Column(name = "vital_pulse")
    private Integer vitalPulse;

    @Column(name = "vital_blood_pressure", length = 20)
    private String vitalBloodPressure;

    @Column(name = "vital_respiratory_rate")
    private Integer vitalRespiratoryRate;

    @Column(name = "vital_oxygen_saturation")
    private Integer vitalOxygenSaturation;

    @Column(name = "height_cm", precision = 5, scale = 1)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "visit_status", nullable = false, length = 30)
    private OpdVisitStatus visitStatus;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
