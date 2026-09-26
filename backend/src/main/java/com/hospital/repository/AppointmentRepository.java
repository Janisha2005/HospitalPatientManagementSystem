package com.hospital.repository;

import com.hospital.entity.Appointment;
import com.hospital.entity.AppointmentStatus;
import com.hospital.entity.AppointmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByAppointmentId(String appointmentId);

    @Query("SELECT a FROM Appointment a WHERE " +
           "(:doctorId IS NULL OR a.doctor.id = :doctorId) AND " +
           "(:patientId IS NULL OR a.patient.id = :patientId) AND " +
           "(:departmentId IS NULL OR a.department.id = :departmentId) AND " +
           "(:appointmentDate IS NULL OR a.appointmentDate = :appointmentDate) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:appointmentType IS NULL OR a.appointmentType = :appointmentType)")
    Page<Appointment> findAppointmentsWithFilters(@Param("doctorId") Long doctorId,
                                                  @Param("patientId") Long patientId,
                                                  @Param("departmentId") Long departmentId,
                                                  @Param("appointmentDate") LocalDate appointmentDate,
                                                  @Param("status") AppointmentStatus status,
                                                  @Param("appointmentType") AppointmentType appointmentType,
                                                  Pageable pageable);

    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusNotIn(Long doctorId, LocalDate appointmentDate, List<AppointmentStatus> excludedStatuses);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :appointmentDate " +
           "AND a.status NOT IN :excludedStatuses AND (:id IS NULL OR a.id != :id) " +
           "AND (a.startTime < :endTime AND a.endTime > :startTime)")
    List<Appointment> findDoctorOverlappingAppointments(@Param("doctorId") Long doctorId,
                                                        @Param("appointmentDate") LocalDate appointmentDate,
                                                        @Param("startTime") LocalTime startTime,
                                                        @Param("endTime") LocalTime endTime,
                                                        @Param("excludedStatuses") List<AppointmentStatus> excludedStatuses,
                                                        @Param("id") Long id);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.appointmentDate = :appointmentDate " +
           "AND a.status NOT IN :excludedStatuses AND (:id IS NULL OR a.id != :id) " +
           "AND (a.startTime < :endTime AND a.endTime > :startTime)")
    List<Appointment> findPatientOverlappingAppointments(@Param("patientId") Long patientId,
                                                         @Param("appointmentDate") LocalDate appointmentDate,
                                                         @Param("startTime") LocalTime startTime,
                                                         @Param("endTime") LocalTime endTime,
                                                         @Param("excludedStatuses") List<AppointmentStatus> excludedStatuses,
                                                         @Param("id") Long id);

    List<Appointment> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(Long patientId);

    List<Appointment> findByDoctorIdAndAppointmentDateOrderByStartTimeAsc(Long doctorId, LocalDate appointmentDate);

    long countByAppointmentDate(LocalDate date);

    long countByAppointmentDateAndStatus(LocalDate date, AppointmentStatus status);
}
