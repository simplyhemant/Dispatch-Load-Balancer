package com.dispatchloadbalancer.dto.response;

import com.dispatchloadbalancer.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Details of an order assigned to a vehicle route")
public class AssignedOrderResponse {

    @Schema(description = "Unique alphanumeric order identifier", example = "ORD001")
    private String orderId;

    @Schema(description = "Latitude coordinate of drop location", example = "12.9716")
    private Double latitude;

    @Schema(description = "Longitude coordinate of drop location", example = "77.5946")
    private Double longitude;

    @Schema(description = "Delivery address", example = "MG Road, Bangalore, Karnataka, India")
    private String address;

    @Schema(description = "Package weight in kilograms", example = "10.0")
    private Double packageWeight;

    @Schema(description = "Priority level of the order", example = "HIGH")
    private Priority priority;

    @Schema(description = "Stop sequence number in the delivery route (1-based)", example = "1")
    private Integer stopSequence;

    @Schema(description = "Distance from previous stop or starting depot", example = "3.20 km")
    private String distanceFromPrevious;
}
