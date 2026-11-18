package com.dispatchloadbalancer.dto.response;

import com.dispatchloadbalancer.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object representing an order")
public class OrderResponse {

    @Schema(description = "Unique alphanumeric order identifier", example = "ORD001")
    private String orderId;

    @Schema(description = "Latitude of drop location", example = "12.9716")
    private Double latitude;

    @Schema(description = "Longitude of drop location", example = "77.5946")
    private Double longitude;

    @Schema(description = "Street/city delivery address", example = "MG Road, Bangalore, Karnataka, India")
    private String address;

    @Schema(description = "Weight of package in kg", example = "10.0")
    private Double packageWeight;

    @Schema(description = "Priority level of the order", example = "HIGH")
    private Priority priority;
}
