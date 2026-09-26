package com.hospital.repository;

import com.hospital.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    List<DoctorAvailability> findByDoctorIdAndIsActiveTrue(Long doctorId);

    List<DoctorAvailability> findByDoctorIdAndDayOfWeekAndIsActiveTrue(Long doctorId, DayOfWeek dayOfWeek);

    @Query("SELECT da FROM DoctorAvailability da WHERE da.doctor.id = :doctorId AND da.dayOfWeek = :dayOfWeek AND da.isActive = true " +
           "AND (:id IS NULL OR da.id != :id) " +
           "AND ((da.startTime < :endTime AND da.endTime > :startTime))")
    List<DoctorAvailability> findOverlappingAvailability(@Param("doctorId") Long doctorId,
                                                          @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                                          @Param("startTime") LocalTime startTime,
                                                          @Param("endTime") LocalTime endTime,
                                                          @Param("id") Long id);
}
