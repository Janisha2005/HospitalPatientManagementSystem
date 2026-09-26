package com.hospital.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabDashboardDto {
    private long totalOrders;
    private long pendingOrders;
    private long sampleCollected;
    private long inProgress;
    private long completedOrders;
    private List<LabOrderResponse> recentPendingOrders;
}
