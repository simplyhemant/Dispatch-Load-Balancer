package com.dispatchloadbalancer.algorithm;

import com.dispatchloadbalancer.entity.DeliveryOrder;
import com.dispatchloadbalancer.entity.DispatchAssignment;
import com.dispatchloadbalancer.entity.Vehicle;
import com.dispatchloadbalancer.entity.VehicleRoute;
import com.dispatchloadbalancer.enums.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TwoOptRouteOptimizerTest {

    private TwoOptRouteOptimizer optimizer;
    private HaversineCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new HaversineCalculator();
        optimizer = new TwoOptRouteOptimizer(calculator);
    }

    private Vehicle createVehicle(String id, double lat, double lon) {
        return Vehicle.builder()
                .vehicleId(id)
                .capacity(100.0)
                .currentLatitude(lat)
                .currentLongitude(lon)
                .currentAddress("Depot " + id)
                .build();
    }

    private DeliveryOrder createOrder(String id, double lat, double lon) {
        return DeliveryOrder.builder()
                .orderId(id)
                .latitude(lat)
                .longitude(lon)
                .address("Address " + id)
                .packageWeight(10.0)
                .priority(Priority.MEDIUM)
                .build();
    }

    private DispatchAssignment createAssignment(DeliveryOrder order, VehicleRoute route, int sequence) {
        return DispatchAssignment.builder()
                .order(order)
                .route(route)
                .stopSequence(sequence)
                .distanceFromPreviousKm(0.0)
                .build();
    }

    @Test
    @DisplayName("Routes with zero, one, or two assignments are handled safely without errors")
    void testOptimize_SmallRoutes_HandledSafely() {
        Vehicle vehicle = createVehicle("VEH001", 12.9716, 77.5946);

        // Case 1: Empty route (0 assignments)
        VehicleRoute emptyRoute = VehicleRoute.builder()
                .vehicle(vehicle)
                .totalLoad(0.0)
                .totalDistanceKm(0.0)
                .assignments(new ArrayList<>())
                .build();
        assertDoesNotThrow(() -> optimizer.optimize(emptyRoute));
        assertEquals(0.0, emptyRoute.getTotalDistanceKm(), 0.001);

        // Case 2: Single-assignment route (1 assignment)
        VehicleRoute singleRoute = VehicleRoute.builder()
                .vehicle(vehicle)
                .totalLoad(10.0)
                .totalDistanceKm(0.0)
                .assignments(new ArrayList<>())
                .build();
        DeliveryOrder o1 = createOrder("ORD001", 12.9716, 77.6413);
        singleRoute.getAssignments().add(createAssignment(o1, singleRoute, 1));
        assertDoesNotThrow(() -> optimizer.optimize(singleRoute));
        assertEquals(1, singleRoute.getAssignments().size());
        assertTrue(singleRoute.getTotalDistanceKm() > 0.0);

        // Case 3: Two-assignment route (2 assignments)
        VehicleRoute twoRoute = VehicleRoute.builder()
                .vehicle(vehicle)
                .totalLoad(20.0)
                .totalDistanceKm(0.0)
                .assignments(new ArrayList<>())
                .build();
        DeliveryOrder o2 = createOrder("ORD002", 13.0827, 80.2707);
        twoRoute.getAssignments().add(createAssignment(o1, twoRoute, 1));
        twoRoute.getAssignments().add(createAssignment(o2, twoRoute, 2));
        assertDoesNotThrow(() -> optimizer.optimize(twoRoute));
        assertEquals(2, twoRoute.getAssignments().size());
        assertEquals(1, twoRoute.getAssignments().get(0).getStopSequence());
        assertEquals(2, twoRoute.getAssignments().get(1).getStopSequence());
    }

    @Test
    @DisplayName("2-Opt untangles a crossing route and strictly decreases total distance")
    void testOptimize_CrossingRoute_ReducesTotalDistance() {
        // Vehicle starts at origin (0.0, 0.0)
        Vehicle vehicle = createVehicle("VEH001", 0.0, 0.0);
        VehicleRoute route = VehicleRoute.builder()
                .vehicle(vehicle)
                .totalLoad(30.0)
                .totalDistanceKm(0.0)
                .assignments(new ArrayList<>())
                .build();

        // 3 drop locations forming a geometric perimeter:
        // A = (1.0, 0.0)
        // B = (1.0, 1.0)
        // C = (0.0, 1.0)
        DeliveryOrder orderA = createOrder("ORDER_A", 1.0, 0.0);
        DeliveryOrder orderB = createOrder("ORDER_B", 1.0, 1.0);
        DeliveryOrder orderC = createOrder("ORDER_C", 0.0, 1.0);

        // Deliberately crossed sequence: Depot -> B -> A -> C
        // Crossing paths: (Depot to B) crosses (A to C)
        route.getAssignments().add(createAssignment(orderB, route, 1));
        route.getAssignments().add(createAssignment(orderA, route, 2));
        route.getAssignments().add(createAssignment(orderC, route, 3));

        // Calculate initial crossed distance
        double initialDist = calculator.calculateDistance(0.0, 0.0, 1.0, 1.0)
                + calculator.calculateDistance(1.0, 1.0, 1.0, 0.0)
                + calculator.calculateDistance(1.0, 0.0, 0.0, 1.0);

        optimizer.optimize(route);

        double optimizedDist = route.getTotalDistanceKm();

        // 1. Total distance must strictly decrease
        assertTrue(optimizedDist < initialDist,
                String.format("2-Opt must reduce route distance. Initial: %.2f km, Optimized: %.2f km",
                        initialDist, optimizedDist));

        // 2. Same orders remain assigned (no lost or duplicated orders)
        assertEquals(3, route.getAssignments().size());
        Set<String> orderIds = new HashSet<>();
        for (DispatchAssignment assignment : route.getAssignments()) {
            orderIds.add(assignment.getOrder().getOrderId());
        }
        assertTrue(orderIds.contains("ORDER_A"));
        assertTrue(orderIds.contains("ORDER_B"));
        assertTrue(orderIds.contains("ORDER_C"));

        // 3. Stop sequences are strictly consecutive 1, 2, 3
        for (int i = 0; i < route.getAssignments().size(); i++) {
            assertEquals(i + 1, route.getAssignments().get(i).getStopSequence(),
                    "Stop sequence must be 1-based and strictly ascending");
        }

        // 4. Sum of individual legs must match total distance
        double sumLegs = 0.0;
        for (DispatchAssignment a : route.getAssignments()) {
            assertTrue(a.getDistanceFromPreviousKm() >= 0.0, "Leg distance cannot be negative");
            sumLegs += a.getDistanceFromPreviousKm();
        }
        assertEquals(sumLegs, route.getTotalDistanceKm(), 0.001,
                "Route total distance must match sum of individual leg distances");
    }

    @Test
    @DisplayName("2-Opt terminates cleanly on already optimal route without redundant changes")
    void testOptimize_AlreadyOptimalRoute_TerminatesWithoutChanges() {
        Vehicle vehicle = createVehicle("VEH001", 0.0, 0.0);
        VehicleRoute route = VehicleRoute.builder()
                .vehicle(vehicle)
                .totalLoad(30.0)
                .totalDistanceKm(0.0)
                .assignments(new ArrayList<>())
                .build();

        // Straight line: Depot(0,0) -> (0,1) -> (0,2) -> (0,3)
        DeliveryOrder o1 = createOrder("O1", 0.0, 1.0);
        DeliveryOrder o2 = createOrder("O2", 0.0, 2.0);
        DeliveryOrder o3 = createOrder("O3", 0.0, 3.0);

        route.getAssignments().add(createAssignment(o1, route, 1));
        route.getAssignments().add(createAssignment(o2, route, 2));
        route.getAssignments().add(createAssignment(o3, route, 3));

        double initialDistance = calculator.calculateDistance(0.0, 0.0, 0.0, 3.0);

        optimizer.optimize(route);

        assertEquals(initialDistance, route.getTotalDistanceKm(), 0.001);
        assertEquals("O1", route.getAssignments().get(0).getOrder().getOrderId());
        assertEquals("O2", route.getAssignments().get(1).getOrder().getOrderId());
        assertEquals("O3", route.getAssignments().get(2).getOrder().getOrderId());
    }
}
