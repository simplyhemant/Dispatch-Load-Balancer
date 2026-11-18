package com.dispatchloadbalancer.dto.response;

import com.dispatchloadbalancer.enums.DispatchStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class DispatchPlanResponse {
    private final Long planId;
    private final DispatchStatus status;
    private final LocalDateTime generatedAt;
    private final String totalDistance;                         // whole fleet
    private final List<VehicleDispatchResponse> dispatchPlan;   // key name matches the spec
    private final List<UnassignedOrderResponse> unassignedOrders;
}