package com.dispatchloadbalancer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Wrapper request payload containing a list of fleet vehicles")
public class VehiclesRequest {

    @Schema(description = "List of vehicles to register or update", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "Vehicles list cannot be empty")
    @Valid
    private List<VehicleRequest> vehicles;
}
