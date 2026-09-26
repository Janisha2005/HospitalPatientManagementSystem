package com.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationalReportDto {
    private List<DoctorPerformanceDto> doctorPerformances;
    private List<DepartmentPerformanceDto> departmentPerformances;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorPerformanceDto {
        private Long doctorId;
        private String doctorName;
        private String departmentName;
        private String specialization;
        private long appointmentCount;
        private long completedAppointments;
        private long opdVisitsCount;
        private long prescriptionsCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentPerformanceDto {
        private Long departmentId;
        private String departmentName;
        private String departmentCode;
        private long opdVisitsCount;
        private long ipdAdmissionsCount;
        private long labOrdersCount;
        private long radiologyOrdersCount;
    }
}
