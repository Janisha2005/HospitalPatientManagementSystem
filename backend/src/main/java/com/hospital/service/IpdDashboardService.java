package com.hospital.service;

import com.hospital.dto.IpdDashboardDto;
import com.hospital.entity.AdmissionStatus;
import com.hospital.entity.BedStatus;
import com.hospital.repository.BedRepository;
import com.hospital.repository.IpdAdmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IpdDashboardService {

    private final IpdAdmissionRepository ipdAdmissionRepository;
    private final BedRepository bedRepository;

    private static final List<AdmissionStatus> ACTIVE_STATUSES = Arrays.asList(
            AdmissionStatus.REQUESTED, AdmissionStatus.APPROVED, AdmissionStatus.ADMITTED,
            AdmissionStatus.ON_LEAVE, AdmissionStatus.DISCHARGE_PLANNED
    );

    @Transactional(readOnly = true)
    public IpdDashboardDto getDashboardMetrics() {
        long totalActiveAdmissions = ipdAdmissionRepository.countByStatusIn(ACTIVE_STATUSES);
        long todayAdmissions = ipdAdmissionRepository.countByAdmissionDate(LocalDate.now());
        long todayDischarges = ipdAdmissionRepository.countByActualDischargeDate(LocalDate.now());

        long availableBeds = bedRepository.countByStatus(BedStatus.AVAILABLE);
        long occupiedBeds = bedRepository.countByStatus(BedStatus.OCCUPIED);
        long bedsInCleaning = bedRepository.countByStatus(BedStatus.CLEANING);
        long bedsUnderMaintenance = bedRepository.countByStatus(BedStatus.MAINTENANCE) + bedRepository.countByStatus(BedStatus.OUT_OF_SERVICE);

        long pendingAdmissionRequests = ipdAdmissionRepository.countByStatus(AdmissionStatus.REQUESTED);
        long dischargePlanned = ipdAdmissionRepository.countByStatus(AdmissionStatus.DISCHARGE_PLANNED);

        return IpdDashboardDto.builder()
                .totalActiveAdmissions(totalActiveAdmissions)
                .todayAdmissions(todayAdmissions)
                .todayDischarges(todayDischarges)
                .availableBeds(availableBeds)
                .occupiedBeds(occupiedBeds)
                .bedsInCleaning(bedsInCleaning)
                .bedsUnderMaintenance(bedsUnderMaintenance)
                .pendingAdmissionRequests(pendingAdmissionRequests)
                .dischargePlanned(dischargePlanned)
                .build();
    }
}
