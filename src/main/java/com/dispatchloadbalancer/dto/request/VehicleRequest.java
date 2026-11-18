package com.dispatchloadbalancer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "Request object representing a single fleet vehicle")
public class VehicleRequest {

    @Schema(description = "Unique alphanumeric vehicle identifier", example = "VEH001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "vehicleId is required")
    private String vehicleId;

    @Schema(description = "Maximum load carrying capacity in kilograms (must be strictly greater than 0)", example = "100.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "capacity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "capacity must be greater than 0")
    private Double capacity;

    @Schema(description = "Starting depot latitude coordinate (-90 to 90)", example = "12.9716", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "currentLatitude is required")
    @DecimalMin(value = "-90.0", message = "currentLatitude must be >= -90")
    @DecimalMax(value = "90.0", message = "currentLatitude must be <= 90")
    private Double currentLatitude;

    @Schema(description = "Starting depot longitude coordinate (-180 to 180)", example = "77.6413", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "currentLongitude is required")
    @DecimalMin(value = "-180.0", message = "currentLongitude must be >= -180")
    @DecimalMax(value = "180.0", message = "currentLongitude must be <= 180")
    private Double currentLongitude;

    @Schema(description = "Current vehicle base or depot address", example = "Indiranagar, Bangalore, Karnataka, India", maxLength = 500, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "currentAddress is required")
    @Size(max = 500, message = "currentAddress must be at most 500 characters")
    private String currentAddress;
}