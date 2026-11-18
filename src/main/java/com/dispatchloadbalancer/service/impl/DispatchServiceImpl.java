
package com.dispatchloadbalancer.service.impl;

import com.dispatchloadbalancer.algorithm.NearestNeighborRouteOptimizer;
import com.dispatchloadbalancer.algorithm.TwoOptRouteOptimizer;
import com.dispatchloadbalancer.dto.response.AssignedOrderResponse;
import com.dispatchloadbalancer.dto.response.DispatchPlanResponse;
import com.dispatchloadbalancer.dto.response.UnassignedOrderResponse;
import com.dispatchloadbalancer.dto.response.VehicleDispatchResponse;
import com.dispatchloadbalancer.entity.DeliveryOrder;
import com.dispatchloadbalancer.entity.DispatchAssignment;
import com.dispatchloadbalancer.entity.DispatchPlan;
import com.dispatchloadbalancer.entity.UnassignedOrder;
import com.dispatchloadbalancer.entity.Vehicle;
import com.dispatchloadbalancer.entity.VehicleRoute;
import com.dispatchloadbalancer.enums.DispatchStatus;
import com.dispatchloadbalancer.exception.InvalidDispatchRequestException;
import com.dispatchloadbalancer.repository.DispatchPlanRepository;
import com.dispatchloadbalancer.repository.OrderRepository;
import com.dispatchloadbalancer.repository.VehicleRepository;
import com.dispatchloadbalancer.service.DispatchService;
import com.dispatchloadbalancer.strategy.DispatchOptimizationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DispatchServiceImpl implements DispatchService {

    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;
    private final DispatchPlanRepository dispatchPlanRepository;

    private final DispatchOptimizationStrategy dispatchOptimizationStrategy;
    private final NearestNeighborRouteOptimizer nearestNeighborRouteOptimizer;
    private final TwoOptRouteOptimizer twoOptRouteOptimizer;

    @Override
    public DispatchPlanResponse getDispatchPlan() {
        log.info("Checking for existing valid dispatch plan in repository");

        // Step 1: Check for an existing valid dispatch plan.
        List<DispatchPlan> cachedPlans =
                dispatchPlanRepository.findPlansWithRoutesByStatus(
                        DispatchStatus.PLANNED
                );

        if (!cachedPlans.isEmpty()) {
            // Latest plan is first because the repository sorts by generatedAt DESC.
            DispatchPlan latestPlan = cachedPlans.get(0);
            log.info("Found cached dispatch plan with ID {} (generatedAt: {}, status: PLANNED)",
                    latestPlan.getId(), latestPlan.getGeneratedAt());

            // Step 2: Fetch assignments and their orders.
            dispatchPlanRepository.findRoutesWithAssignmentsByPlan(latestPlan);

            // Step 3: Fetch unassigned orders.
            dispatchPlanRepository.fetchUnassignedOrdersForPlan(latestPlan.getId());

            log.info("Returning cached plan ID {} with {} routes and {} unassigned orders",
                    latestPlan.getId(), latestPlan.getRoutes().size(), latestPlan.getUnassignedOrders().size());
            return mapToResponse(latestPlan);
        }

        // No valid cached plan. Generate a new one.
        log.info("No valid cached dispatch plan found. Generating new plan.");
        return generateDispatchPlan();
    }

    private DispatchPlanResponse generateDispatchPlan() {
        // Fetch all delivery orders.
        List<DeliveryOrder> orders = orderRepository.findAll();

        if (orders.isEmpty()) {
            log.warn("Cannot generate dispatch plan: No delivery orders found in database");
            throw new InvalidDispatchRequestException(
                    "No delivery orders found. Please submit orders first."
            );
        }

        // Fetch all available vehicles.
        List<Vehicle> vehicles = vehicleRepository.findAll();

        if (vehicles.isEmpty()) {
            log.warn("Cannot generate dispatch plan: No fleet vehicles found in database");
            throw new InvalidDispatchRequestException(
                    "No vehicles found. Please submit fleet vehicles first."
            );
        }

        log.info("Starting dispatch optimization for {} delivery orders across {} vehicles",
                orders.size(), vehicles.size());

        // Step 1: Greedy order-to-vehicle assignment.
        DispatchPlan plan =
                dispatchOptimizationStrategy.optimize(orders, vehicles);

        // Step 2: Optimize the stop sequence for every vehicle.
        double totalDistance = 0.0;

        for (VehicleRoute route : plan.getRoutes()) {
            // Nearest Neighbor: determine the stop sequence.
            nearestNeighborRouteOptimizer.optimize(route);

            // 2-opt: improve the route distance.
            twoOptRouteOptimizer.optimize(route);

            totalDistance += route.getTotalDistanceKm();
        }

        // Step 3: Update total fleet distance.
        plan.setTotalDistanceKm(totalDistance);

        // Step 4: Persist the complete dispatch plan.
        DispatchPlan savedPlan = dispatchPlanRepository.save(plan);

        log.info("Persisted new dispatch plan ID {} with {} routes, {} unassigned orders, totalDistance: {} km",
                savedPlan.getId(),
                savedPlan.getRoutes().size(),
                savedPlan.getUnassignedOrders().size(),
                String.format(Locale.US, "%.2f", savedPlan.getTotalDistanceKm()));

        // Step 5: Convert saved plan into response DTO.
        return mapToResponse(savedPlan);
    }

    @Override
    public void reset() {
        log.info("Clearing all dispatch data (plans, orders, vehicles)");
        dispatchPlanRepository.deleteAll();
        orderRepository.deleteAll();
        vehicleRepository.deleteAll();
        log.info("All dispatch data cleared successfully");
    }

    private DispatchPlanResponse mapToResponse(DispatchPlan plan) {

        List<VehicleDispatchResponse> vehicleResponses =
                new ArrayList<>();

        // Map vehicle routes and assigned orders.
        for (VehicleRoute route : plan.getRoutes()) {

            List<AssignedOrderResponse> assignedOrderResponses =
                    new ArrayList<>();

            for (DispatchAssignment assignment : route.getAssignments()) {

                DeliveryOrder order = assignment.getOrder();

                AssignedOrderResponse orderResponse =
                        AssignedOrderResponse.builder()
                                .orderId(order.getOrderId())
                                .latitude(order.getLatitude())
                                .longitude(order.getLongitude())
                                .address(order.getAddress())
                                .packageWeight(order.getPackageWeight())
                                .priority(order.getPriority())
                                .stopSequence(assignment.getStopSequence())
                                .distanceFromPrevious(
                                        formatDistance(
                                                assignment.getDistanceFromPreviousKm()
                                        )
                                )
                                .build();

                assignedOrderResponses.add(orderResponse);
            }

            Vehicle vehicle = route.getVehicle();

            VehicleDispatchResponse vehicleResponse =
                    VehicleDispatchResponse.builder()
                            .vehicleId(vehicle.getVehicleId())
                            .currentAddress(vehicle.getCurrentAddress())
                            .capacity(vehicle.getCapacity())
                            .totalLoad(route.getTotalLoad())
                            .totalDistance(
                                    formatDistance(route.getTotalDistanceKm())
                            )
                            .assignedOrders(assignedOrderResponses)
                            .build();

            vehicleResponses.add(vehicleResponse);
        }

        // Map unassigned orders.
        List<UnassignedOrderResponse> unassignedResponses =
                plan.getUnassignedOrders()
                        .stream()
                        .map(unassigned -> {

                            DeliveryOrder order = unassigned.getOrder();

                            return UnassignedOrderResponse.builder()
                                    .orderId(order.getOrderId())
                                    .address(order.getAddress())
                                    .packageWeight(order.getPackageWeight())
                                    .priority(order.getPriority())
                                    .reason(unassigned.getReason())
                                    .build();
                        })
                        .toList();

        // Build final dispatch plan response.
        return DispatchPlanResponse.builder()
                .planId(plan.getId())
                .status(plan.getStatus())
                .generatedAt(plan.getGeneratedAt())
                .totalDistance(
                        formatDistance(plan.getTotalDistanceKm())
                )
                .dispatchPlan(vehicleResponses)
                .unassignedOrders(unassignedResponses)
                .build();
    }

    private String formatDistance(Double distance) {

        return String.format(
                Locale.US,
                "%.2f km",
                distance == null ? 0.0 : distance
        );
    }
}
