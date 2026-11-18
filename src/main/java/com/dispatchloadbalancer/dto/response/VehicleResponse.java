package com.dispatchloadbalancer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object representing a fleet vehicle")
public class VehicleResponse {

    @Schema(description = "Unique alphanumeric vehicle identifier", example = "VEH001")
    private String vehicleId;

    @Schema(description = "Load carrying capacity in kilograms", example = "100.0")
    private Double capacity;

    @Schema(description = "Current depot latitude coordinate", example = "12.9716")
    private Double currentLatitude;

    @Schema(description = "Current depot longitude coordinate", example = "77.6413")
    private Double currentLongitude;

    @Schema(description = "Current vehicle base or depot address", example = "Indiranagar, Bangalore, Karnataka, India")
    private String currentAddress;
}
