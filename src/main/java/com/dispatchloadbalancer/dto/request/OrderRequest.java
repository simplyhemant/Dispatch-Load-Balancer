package com.dispatchloadbalancer.dto.request;

import com.dispatchloadbalancer.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "Request object representing a single delivery order")
public class OrderRequest {

    @Schema(description = "Unique alphanumeric order identifier", example = "ORD001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "orderId is required")
    private String orderId;

    @Schema(description = "Latitude coordinate of delivery drop point (-90 to 90)", example = "12.9716", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "latitude is required")
    @DecimalMin(value = "-90.0", message = "latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "latitude must be <= 90")
    private Double latitude;

    @Schema(description = "Longitude coordinate of delivery drop point (-180 to 180)", example = "77.5946", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "longitude is required")
    @DecimalMin(value = "-180.0", message = "longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "longitude must be <= 180")
    private Double longitude;

    @Schema(description = "Human-readable street and city address", example = "MG Road, Bangalore, Karnataka, India", maxLength = 500, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "address is required")
    @Size(max = 500, message = "address must be at most 500 characters")
    private String address;

    @Schema(description = "Package weight in kilograms (must be strictly greater than 0)", example = "10.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "packageWeight is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "packageWeight must be greater than 0")
    private Double packageWeight;

    @Schema(description = "Delivery urgency priority (HIGH, MEDIUM, LOW)", example = "HIGH", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "priority is required (HIGH, MEDIUM or LOW)")
    private Priority priority;
}