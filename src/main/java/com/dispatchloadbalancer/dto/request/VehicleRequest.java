package com.dispatchloadbalancer.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VehicleRequest {

    @NotBlank(message = "vehicleId is required")
    private String vehicleId;

    @NotNull(message = "capacity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "capacity must be greater than 0")
    private Double capacity;

    @NotNull(message = "currentLatitude is required")
    @DecimalMin(value = "-90.0", message = "currentLatitude must be >= -90")
    @DecimalMax(value = "90.0", message = "currentLatitude must be <= 90")
    private Double currentLatitude;

    @NotNull(message = "currentLongitude is required")
    @DecimalMin(value = "-180.0", message = "currentLongitude must be >= -180")
    @DecimalMax(value = "180.0", message = "currentLongitude must be <= 180")
    private Double currentLongitude;

    @NotBlank(message = "currentAddress is required")
    @Size(max = 500, message = "currentAddress must be at most 500 characters")
    private String currentAddress;
}