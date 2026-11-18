package com.dispatchloadbalancer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Route and load dispatch details for an allocated vehicle")
public class VehicleDispatchResponse {

    @Schema(description = "Unique alphanumeric vehicle identifier", example = "VEH001")
    private String vehicleId;

    @Schema(description = "Starting base address of the vehicle", example = "Indiranagar, Bangalore, Karnataka, India")
    private String currentAddress;

    @Schema(description = "Sum of package weights of all assigned orders", example = "45.0")
    private Double totalLoad;

    @Schema(description = "Total rated load capacity of the vehicle", example = "100.0")
    private Double capacity;

    @Schema(description = "Total travel distance for this vehicle route", example = "12.30 km")
    private String totalDistance;

    @Schema(description = "Ordered sequence of delivery stops assigned to this vehicle")
    private List<AssignedOrderResponse> assignedOrders;
}
