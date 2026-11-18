package com.dispatchloadbalancer.dto.request;

import com.dispatchloadbalancer.enums.Priority;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderRequest {

    @NotBlank(message = "orderId is required")
    private String orderId;

    @NotNull(message = "latitude is required")
    @DecimalMin(value = "-90.0", message = "latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "latitude must be <= 90")
    private Double latitude;

    @NotNull(message = "longitude is required")
    @DecimalMin(value = "-180.0", message = "longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "longitude must be <= 180")
    private Double longitude;

    @NotBlank(message = "address is required")
    @Size(max = 500, message = "address must be at most 500 characters")
    private String address;

    @NotNull(message = "packageWeight is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "packageWeight must be greater than 0")
    private Double packageWeight;

    @NotNull(message = "priority is required (HIGH, MEDIUM or LOW)")
    private Priority priority;
}