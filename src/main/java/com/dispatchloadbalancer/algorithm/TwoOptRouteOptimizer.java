
package com.dispatchloadbalancer.algorithm;

import com.dispatchloadbalancer.entity.DeliveryOrder;
import com.dispatchloadbalancer.entity.DispatchAssignment;
import com.dispatchloadbalancer.entity.Vehicle;
import com.dispatchloadbalancer.entity.VehicleRoute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TwoOptRouteOptimizer {

    private final HaversineCalculator haversineCalculator;

    public void optimize(VehicleRoute route) {

        List<DispatchAssignment> assignments = route.getAssignments();

        if (assignments == null || assignments.size() <= 2) {
            updateRouteDetails(route);
            return;
        }

        Vehicle vehicle = route.getVehicle();
        log.debug("2-Opt optimization starting for vehicle {} with {} stops",
                vehicle.getVehicleId(), assignments.size());

        boolean improved = true;
        int improvementsCount = 0;

        // Continue until no shorter route is found
        while (improved) {

            improved = false;

            double currentDistance = calculateRouteDistance(
                    route,
                    assignments
            );

            // Try reversing different route segments
            for (int i = 0; i < assignments.size() - 1; i++) {

                for (int j = i + 1; j < assignments.size(); j++) {

                    List<DispatchAssignment> newRoute =
                            new ArrayList<>(assignments);

                    // Reverse the selected segment
                    Collections.reverse(newRoute.subList(i, j + 1));

                    double newDistance = calculateRouteDistance(
                            route,
                            newRoute
                    );

                    // Keep the change only if distance decreases
                    if (newDistance < currentDistance - 0.000001) {

                        assignments.clear();
                        assignments.addAll(newRoute);

                        currentDistance = newDistance;
                        improved = true;
                        improvementsCount++;

                        break;
                    }
                }

                if (improved) {
                    break;
                }
            }
        }

        updateRouteDetails(route);
        log.debug("2-Opt completed for vehicle {}. Final distance: {} km (improvements applied: {})",
                vehicle.getVehicleId(), route.getTotalDistanceKm(), improvementsCount);
    }

    // Calculate total distance for a given route sequence
    private double calculateRouteDistance(
            VehicleRoute route,
            List<DispatchAssignment> assignments) {

        Vehicle vehicle = route.getVehicle();

        double previousLat = vehicle.getCurrentLatitude();
        double previousLon = vehicle.getCurrentLongitude();

        double totalDistance = 0.0;

        for (DispatchAssignment assignment : assignments) {

            DeliveryOrder order = assignment.getOrder();

            double distance = haversineCalculator.calculateDistance(
                    previousLat,
                    previousLon,
                    order.getLatitude(),
                    order.getLongitude()
            );

            totalDistance += distance;

            previousLat = order.getLatitude();
            previousLon = order.getLongitude();
        }

        return totalDistance;
    }

    // Update stop sequence and individual distances
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
