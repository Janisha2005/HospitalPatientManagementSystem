package com.hospital.repository;

import com.hospital.entity.OpdVisit;
import com.hospital.entity.OpdVisitStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OpdVisitRepository extends JpaRepository<OpdVisit, Long> {

    Optional<OpdVisit> findByOpdVisitId(String opdVisitId);

    Optional<OpdVisit> findByAppointmentId(Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);

    @Query("SELECT MAX(o.queueNumber) FROM OpdVisit o WHERE o.doctor.id = :doctorId AND o.visitDate = :visitDate")
    Integer findMaxQueueNumberForDoctorAndDate(@Param("doctorId") Long doctorId, @Param("visitDate") LocalDate visitDate);

    @Query("SELECT o FROM OpdVisit o WHERE " +
           "(:doctorId IS NULL OR o.doctor.id = :doctorId) AND " +
           "(:patientId IS NULL OR o.patient.id = :patientId) AND " +
           "(:departmentId IS NULL OR o.department.id = :departmentId) AND " +
           "(:visitDate IS NULL OR o.visitDate = :visitDate) AND " +
           "(:visitStatus IS NULL OR o.visitStatus = :visitStatus)")
    Page<OpdVisit> findVisitsWithFilters(@Param("doctorId") Long doctorId,
                                         @Param("patientId") Long patientId,
                                         @Param("departmentId") Long departmentId,
                                         @Param("visitDate") LocalDate visitDate,
                                         @Param("visitStatus") OpdVisitStatus visitStatus,
                                         Pageable pageable);

    List<OpdVisit> findByDoctorIdAndVisitDateOrderByQueueNumberAsc(Long doctorId, LocalDate visitDate);

    List<OpdVisit> findByDoctorIdAndVisitDateAndVisitStatusOrderByQueueNumberAsc(Long doctorId, LocalDate visitDate, OpdVisitStatus visitStatus);

    List<OpdVisit> findByDepartmentIdAndVisitDateOrderByQueueNumberAsc(Long departmentId, LocalDate visitDate);

    List<OpdVisit> findByPatientIdOrderByVisitDateDescCreatedAtDesc(Long patientId);

    long countByVisitDate(LocalDate visitDate);

    long countByVisitDateAndVisitStatus(LocalDate visitDate, OpdVisitStatus visitStatus);

    long countByDoctorIdAndVisitDateAndVisitStatus(Long doctorId, LocalDate visitDate, OpdVisitStatus visitStatus);
}
