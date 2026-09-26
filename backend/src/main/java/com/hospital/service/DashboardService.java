package com.hospital.service;

import com.hospital.dto.DashboardStatsDto;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.PatientRepository;
import com.hospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public DashboardStatsDto getDashboardStats() {
        return DashboardStatsDto.builder()
                .totalPatients(patientRepository.count())
                .totalDoctors(doctorRepository.count())
                .totalDepartments(departmentRepository.count())
                .activeUsers(userRepository.countByIsActiveTrue())
                .build();
    }
}
