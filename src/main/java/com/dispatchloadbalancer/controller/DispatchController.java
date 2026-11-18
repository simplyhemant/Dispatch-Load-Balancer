
package com.dispatchloadbalancer.controller;

import com.dispatchloadbalancer.dto.response.ApiResponse;
import com.dispatchloadbalancer.dto.response.DispatchPlanResponse;
import com.dispatchloadbalancer.service.DispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/dispatch")
@RequiredArgsConstructor
@Tag(name = "Dispatch Controller", description = "Endpoints for generating optimized dispatch plans and resetting system data")
public class DispatchController {

    private final DispatchService dispatchService;

    @GetMapping("/plan")
    @Operation(
            summary = "Generate or retrieve optimized dispatch plan",
            description = "Computes or returns the cached optimal dispatch plan allocating pending delivery orders to fleet vehicles while respecting weight capacity, priority, and minimizing Haversine travel distance."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Dispatch plan computed or fetched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DispatchPlanResponse.class),
                            examples = @ExampleObject(
                                    name = "SampleDispatchPlan",
                                    summary = "Sample Dispatch Plan response",
                                    value = """
                                            {
                                              "planId": 1,
                                              "status": "PLANNED",
                                              "totalDistance": "11.00 km",
                                              "dispatchPlan": [
                                                {
                                                  "vehicleId": "VEH001",
                                                  "currentAddress": "Indiranagar, Bangalore, Karnataka, India",
                                                  "totalLoad": 10.0,
                                                  "capacity": 100.0,
                                                  "totalDistance": "5.00 km",
                                                  "assignedOrders": [
                                                    {
                                                      "orderId": "ORD001",
                                                      "latitude": 12.9716,
                                                      "longitude": 77.5946,
                                                      "address": "MG Road, Bangalore, Karnataka, India",
                                                      "packageWeight": 10.0,
                                                      "priority": "HIGH",
                                                      "stopSequence": 1,
                                                      "distanceFromPrevious": "5.00 km"
                                                    }
                                                  ]
                                                }
                                              ],
                                              "unassignedOrders": []
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "No orders or vehicles available to generate plan",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<DispatchPlanResponse> getDispatchPlan() {
        log.info("Received request to retrieve or generate dispatch plan");
        DispatchPlanResponse response = dispatchService.getDispatchPlan();
        log.info("Dispatch plan returned with planId: {}, routes: {}, totalDistance: {}",
                response.getPlanId(),
                response.getDispatchPlan() != null ? response.getDispatchPlan().size() : 0,
                response.getTotalDistance());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/reset")
    @Operation(
            summary = "Reset dispatch data",
            description = "Deletes all stored delivery orders, fleet vehicles, routes, assignments, and generated dispatch plans."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "All dispatch data reset successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<Void>> reset() {
        log.info("Received request to reset all dispatch data");
        dispatchService.reset();
        log.info("All dispatch data has been reset successfully");
        return ResponseEntity.ok(
                ApiResponse.success("Dispatch data reset successfully")
        );
    }
}
