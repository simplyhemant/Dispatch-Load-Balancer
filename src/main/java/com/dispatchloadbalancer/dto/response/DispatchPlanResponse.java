package com.dispatchloadbalancer.dto.response;

import com.dispatchloadbalancer.enums.DispatchStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Complete dispatch plan allocation output")
public class DispatchPlanResponse {

    @Schema(description = "Unique database plan ID", example = "1")
    private final Long planId;

    @Schema(description = "Status of the dispatch plan", example = "PLANNED")
    private final DispatchStatus status;

    @Schema(description = "Timestamp when the plan was calculated")
    private final LocalDateTime generatedAt;

    @Schema(description = "Cumulative distance of all vehicle routes combined", example = "24.50 km")
    private final String totalDistance;

    @Schema(description = "Allocated vehicle routes with their sequenced stops")
    private final List<VehicleDispatchResponse> dispatchPlan;

    @Schema(description = "Orders that could not be assigned due to fleet capacity limits")
    private final List<UnassignedOrderResponse> unassignedOrders;
}