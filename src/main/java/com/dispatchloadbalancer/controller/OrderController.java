package com.dispatchloadbalancer.controller;

import com.dispatchloadbalancer.dto.request.OrdersRequest;
import com.dispatchloadbalancer.dto.response.ApiResponse;
import com.dispatchloadbalancer.dto.response.OrderResponse;
import com.dispatchloadbalancer.service.OrderService;
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
@RequestMapping("/api/dispatch/orders")
@RequiredArgsConstructor
@Tag(name = "Order Controller", description = "Endpoints for ingesting and retrieving delivery orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(
            summary = "Ingest delivery orders",
            description = "Accepts a batch of delivery orders. Validates coordinates, package weights, and priorities. Upserts existing orders and invalidates previous dispatch plans."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Delivery orders accepted successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failure or invalid order attributes",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Duplicate orderId found within request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<Void>> saveOrders(
            @Valid @RequestBody OrdersRequest request
    ) {
        int count = request.getOrders() != null ? request.getOrders().size() : 0;
        log.info("Received request to ingest {} delivery orders", count);

        orderService.saveOrders(request);

        log.info("Successfully processed and saved {} delivery orders", count);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Delivery orders accepted."));
    }

    @GetMapping
    @Operation(
            summary = "Retrieve all orders",
            description = "Fetches a complete list of all currently stored delivery orders in the system."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Orders retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        log.info("Received request to fetch all delivery orders");
        List<OrderResponse> orders = orderService.getAllOrders();
        log.info("Returning {} delivery orders", orders.size());

        if (orders.isEmpty()) {
            return ResponseEntity.ok(
                    ApiResponse.success("No orders found.", orders)
            );
        }

        return ResponseEntity.ok(
                ApiResponse.success("Orders fetched successfully.", orders)
        );
    }
}