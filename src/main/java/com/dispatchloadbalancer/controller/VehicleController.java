
package com.dispatchloadbalancer.controller;

import com.dispatchloadbalancer.dto.request.VehiclesRequest;
import com.dispatchloadbalancer.dto.response.ApiResponse;
import com.dispatchloadbalancer.dto.response.VehicleResponse;
import com.dispatchloadbalancer.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dispatch/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicle Controller", description = "Endpoints for managing fleet vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @Operation(
            summary = "Ingest fleet vehicles",
            description = "Registers or updates available fleet vehicles with capacities and starting depot coordinates. Invalidates previously generated dispatch plans."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Vehicle details accepted successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failure or invalid vehicle attributes",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Duplicate vehicleId found within request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<Void>> saveVehicles(
            @Valid @RequestBody VehiclesRequest request
    ) {
        int count = request.getVehicles() != null ? request.getVehicles().size() : 0;
        log.info("Received request to ingest {} vehicles", count);

        vehicleService.saveVehicles(request);

        log.info("Successfully processed and saved {} vehicles", count);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vehicle details accepted."));
    }

    @GetMapping
    @Operation(
            summary = "Retrieve all vehicles",
            description = "Fetches a complete list of all currently registered fleet vehicles."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Vehicles retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAllVehicles() {
        log.info("Received request to fetch all fleet vehicles");
        List<VehicleResponse> vehicles = vehicleService.getAllVehicles();
        log.info("Returning {} vehicles", vehicles.size());

        if (vehicles.isEmpty()) {
            return ResponseEntity.ok(
                    ApiResponse.success("No vehicles found.", vehicles)
            );
        }

        return ResponseEntity.ok(
                ApiResponse.success("Vehicles retrieved successfully", vehicles)
        );
    }
}
