package com.dispatchloadbalancer.dto.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDispatchResponse {

    private String vehicleId;
    private String currentAddress;
    private Double totalLoad;
    private Double capacity;
    private String totalDistance;
    private List<AssignedOrderResponse> assignedOrders;
}
