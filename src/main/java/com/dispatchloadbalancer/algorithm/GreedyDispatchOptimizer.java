
package com.dispatchloadbalancer.algorithm;

import com.dispatchloadbalancer.entity.*;
import com.dispatchloadbalancer.enums.*;
import com.dispatchloadbalancer.strategy.DispatchOptimizationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GreedyDispatchOptimizer implements DispatchOptimizationStrategy {

    private final HaversineCalculator haversineCalculator;

    @Override
    public DispatchPlan optimize(List<DeliveryOrder> orders, List<Vehicle> vehicles) {

        if (orders == null || vehicles == null) {
            throw new IllegalArgumentException("Orders and vehicles cannot be null");
        }

        // Create dispatch plan
        DispatchPlan plan = DispatchPlan.builder()
                .status(DispatchStatus.PLANNED)
                .generatedAt(LocalDateTime.now())
                .totalDistanceKm(0.0)
                .build();

        // Sort orders by priority: HIGH -> MEDIUM -> LOW
        List<DeliveryOrder> sortedOrders = new ArrayList<>(orders);

        Collections.sort(sortedOrders, new Comparator<DeliveryOrder>() {
            @Override
            public int compare(DeliveryOrder o1, DeliveryOrder o2) {
                return Integer.compare(
                        o1.getPriority().ordinal(),
                        o2.getPriority().ordinal()
                );
            }
        });

        // Create an empty route for each vehicle
        List<VehicleRoute> routes = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            VehicleRoute route = VehicleRoute.builder()
                    .vehicle(vehicle)
                    .totalLoad(0.0)
                    .totalDistanceKm(0.0)
                    .build();

            routes.add(route);
        }

        // Process orders according to priority
        for (DeliveryOrder order : sortedOrders) {

            VehicleRoute bestRoute = null;
            int bestPosition = -1;
            double minimumAdditionalDistance = Double.MAX_VALUE;

            // Find the best vehicle and insertion position
            for (VehicleRoute route : routes) {

                Vehicle vehicle = route.getVehicle();

                double remainingCapacity =
                        vehicle.getCapacity() - route.getTotalLoad();

                // Check vehicle capacity
                if (order.getPackageWeight() > remainingCapacity) {
                    continue;
                }

                List<DispatchAssignment> assignments = route.getAssignments();

                // Check every possible insertion position
                for (int i = 0; i <= assignments.size(); i++) {

                    double additionalDistance =
                            calculateAdditionalDistance(route, order, i);

                    if (additionalDistance < minimumAdditionalDistance) {

                        minimumAdditionalDistance = additionalDistance;
                        bestRoute = route;
                        bestPosition = i;
                    }
                }
            }

            // Assign order to the best vehicle
            if (bestRoute != null) {

                assignOrder(bestRoute, order, bestPosition);

            } else {

                // No vehicle has sufficient remaining capacity
                UnassignedReason reason =
                        getUnassignedReason(order, vehicles);

                UnassignedOrder unassigned = UnassignedOrder.builder()
                        .order(order)
                        .reason(reason)
                        .build();

                plan.addUnassignedOrder(unassigned);
            }
        }

        // Add routes containing assigned orders to the dispatch plan
        double totalDistance = 0.0;

        for (VehicleRoute route : routes) {

            if (!route.getAssignments().isEmpty()) {

                plan.addRoute(route);

                totalDistance += route.getTotalDistanceKm();
            }
        }

        plan.setTotalDistanceKm(totalDistance);

        return plan;
    }

    // Calculate additional distance for inserting an order
    private double calculateAdditionalDistance(
            VehicleRoute route,
            DeliveryOrder order,
            int position) {

        List<DispatchAssignment> assignments = route.getAssignments();

        Vehicle vehicle = route.getVehicle();

        double previousLat;
        double previousLon;

        // Previous location: vehicle or previous delivery order
        if (position == 0) {

            previousLat = vehicle.getCurrentLatitude();
            previousLon = vehicle.getCurrentLongitude();

        } else {

            DeliveryOrder previousOrder =
                    assignments.get(position - 1).getOrder();

            previousLat = previousOrder.getLatitude();
            previousLon = previousOrder.getLongitude();
        }

        // Distance from previous location to new order
        double distanceToNewOrder =
                haversineCalculator.calculateDistance(
                        previousLat,
                        previousLon,
                        order.getLatitude(),
                        order.getLongitude()
                );

        // If inserting at the end, no existing route segment is replaced
        if (position == assignments.size()) {
            return distanceToNewOrder;
        }

        // Get the next existing delivery order
        DeliveryOrder nextOrder =
                assignments.get(position).getOrder();

        // Calculate old distance between previous and next locations
        double oldDistance =
                haversineCalculator.calculateDistance(
                        previousLat,
                        previousLon,
                        nextOrder.getLatitude(),
                        nextOrder.getLongitude()
                );

        // Calculate distance from new order to next order
        double distanceFromNewOrder =
                haversineCalculator.calculateDistance(
                        order.getLatitude(),
                        order.getLongitude(),
                        nextOrder.getLatitude(),
                        nextOrder.getLongitude()
                );

        // Additional distance introduced by insertion
        return Math.max(
                0.0,
                distanceToNewOrder + distanceFromNewOrder - oldDistance
        );
    }

    // Assign an order and update the vehicle route
    private void assignOrder(
            VehicleRoute route,
            DeliveryOrder order,
            int position) {

        DispatchAssignment assignment = DispatchAssignment.builder()
                .order(order)
                .stopSequence(position + 1)
                .distanceFromPreviousKm(0.0)
                .build();

        // Insert order at the selected position
        route.getAssignments().add(position, assignment);
        assignment.setRoute(route);

        // Update vehicle load
        route.setTotalLoad(
                route.getTotalLoad() + order.getPackageWeight()
        );

        // Recalculate route distance and stop sequences
        updateRouteDetails(route);
    }

    // Recalculate distances after inserting an order
    private void updateRouteDetails(VehicleRoute route) {

        List<DispatchAssignment> assignments = route.getAssignments();

        Vehicle vehicle = route.getVehicle();

        double previousLat = vehicle.getCurrentLatitude();
        double previousLon = vehicle.getCurrentLongitude();

        double totalDistance = 0.0;

        for (int i = 0; i < assignments.size(); i++) {

            DispatchAssignment assignment = assignments.get(i);

            DeliveryOrder order = assignment.getOrder();

            double distance = haversineCalculator.calculateDistance(
                    previousLat,
                    previousLon,
                    order.getLatitude(),
                    order.getLongitude()
            );

            assignment.setStopSequence(i + 1);
            assignment.setDistanceFromPreviousKm(distance);

            totalDistance += distance;

            previousLat = order.getLatitude();
            previousLon = order.getLongitude();
        }

        route.setTotalDistanceKm(totalDistance);
    }

    // Determine why an order could not be assigned
    private UnassignedReason getUnassignedReason(
            DeliveryOrder order,
            List<Vehicle> vehicles) {

        double maximumCapacity = 0.0;

        for (Vehicle vehicle : vehicles) {

            if (vehicle.getCapacity() > maximumCapacity) {
                maximumCapacity = vehicle.getCapacity();
            }
        }

        if (order.getPackageWeight() > maximumCapacity) {
            return UnassignedReason.EXCEEDS_ALL_VEHICLE_CAPACITY;
        }

        return UnassignedReason.INSUFFICIENT_REMAINING_CAPACITY;
    }
}
