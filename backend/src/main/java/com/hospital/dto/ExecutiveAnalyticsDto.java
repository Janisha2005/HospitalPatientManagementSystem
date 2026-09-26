package com.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutiveAnalyticsDto {

    // Patient KPIs
    private long totalRegisteredPatients;
    private long newPatients;
    private long returningPatients;
    private long opdPatientsCount;
    private long ipdAdmissionsCount;
    private long dischargesCount;

    // Appointment KPIs
    private long totalAppointments;
    private long scheduledAppointments;
    private long confirmedAppointments;
    private long checkedInAppointments;
    private long completedAppointments;
    private long cancelledAppointments;
    private long noShowAppointments;
    private double completionPercentage;
    private double cancellationPercentage;
    private double noShowPercentage;

    // Clinical KPIs
    private long opdVisitsCount;
    private long labOrdersCount;
    private long completedLabTestsCount;
    private long radiologyOrdersCount;
    private long completedRadiologyStudiesCount;
    private long prescriptionsIssuedCount;

    // Pharmacy KPIs
    private long medicinesDispensedCount;
    private long dispensingTransactionsCount;
    private BigDecimal pharmacyRevenue;
    private long lowStockMedicinesCount;
    private long nearExpiryBatchesCount;
    private long expiredBatchesCount;

    // Financial KPIs
    private BigDecimal totalBilled;
    private BigDecimal totalCollected;
    private BigDecimal totalOutstanding;
    private BigDecimal totalRefunded;
    private BigDecimal totalCreditNotes;
    private BigDecimal netCollected;

    // Bed Occupancy KPIs
    private long totalBeds;
    private long availableBeds;
    private long occupiedBeds;
    private long cleaningBeds;
    private double bedOccupancyPercentage;
}
