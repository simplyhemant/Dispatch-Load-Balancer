
package com.dispatchloadbalancer.algorithm;

import com.dispatchloadbalancer.entity.DeliveryOrder;
import com.dispatchloadbalancer.entity.DispatchAssignment;
import com.dispatchloadbalancer.entity.Vehicle;
import com.dispatchloadbalancer.entity.VehicleRoute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NearestNeighborRouteOptimizer {

    private final HaversineCalculator haversineCalculator;

    public void optimize(VehicleRoute route) {

        List<DispatchAssignment> assignments = route.getAssignments();

        if (assignments == null || assignments.size() <= 1) {
            updateRouteDetails(route);
            return;
        }

        Vehicle vehicle = route.getVehicle();
        log.debug("Nearest Neighbor optimization starting for vehicle {} with {} stops",
                vehicle.getVehicleId(), assignments.size());

        double currentLat = vehicle.getCurrentLatitude();
        double currentLon = vehicle.getCurrentLongitude();

        List<DispatchAssignment> unvisited =
                new ArrayList<>(assignments);

        List<DispatchAssignment> optimizedRoute =
                new ArrayList<>();

        // Find the nearest unvisited order
        while (!unvisited.isEmpty()) {

            DispatchAssignment nearest = null;
            double minimumDistance = Double.MAX_VALUE;

            for (DispatchAssignment assignment : unvisited) {

                DeliveryOrder order = assignment.getOrder();

                double distance = haversineCalculator.calculateDistance(
                        currentLat,
                        currentLon,
                        order.getLatitude(),
                        order.getLongitude()
                );

                if (distance < minimumDistance) {
                    minimumDistance = distance;
                    nearest = assignment;
                }
            }

            // Add nearest order to optimized route
            optimizedRoute.add(nearest);
            unvisited.remove(nearest);

            // Update current location
            DeliveryOrder order = nearest.getOrder();

            currentLat = order.getLatitude();
            currentLon = order.getLongitude();
        }

        // Replace existing route with optimized route
        assignments.clear();
        assignments.addAll(optimizedRoute);

        updateRouteDetails(route);
        log.debug("Nearest Neighbor completed for vehicle {}. Optimized distance: {} km",
                vehicle.getVehicleId(), route.getTotalDistanceKm());
    }

    private void updateRouteDetails(VehicleRoute route) {

        Vehicle vehicle = route.getVehicle();

        double previousLat = vehicle.getCurrentLatitude();
        double previousLon = vehicle.getCurrentLongitude();

        double totalDistance = 0.0;

        List<DispatchAssignment> assignments = route.getAssignments();

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
}
