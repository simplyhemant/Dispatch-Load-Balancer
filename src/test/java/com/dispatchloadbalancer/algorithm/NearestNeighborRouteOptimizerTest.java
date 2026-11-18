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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NearestNeighborRouteOptimizerTest {

    private NearestNeighborRouteOptimizer optimizer;
    private HaversineCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new HaversineCalculator();
        optimizer = new NearestNeighborRouteOptimizer(calculator);
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
    @DisplayName("Empty route handles optimization gracefully without error")
    void testOptimize_EmptyRoute_HandlesGracefully() {
        Vehicle vehicle = createVehicle("VEH001", 12.9716, 77.5946);
        VehicleRoute route = VehicleRoute.builder()
                .vehicle(vehicle)
                .totalLoad(0.0)
                .totalDistanceKm(0.0)
                .assignments(new ArrayList<>())
                .build();

        optimizer.optimize(route);

        assertNotNull(route.getAssignments());
        assertTrue(route.getAssignments().isEmpty());
        assertEquals(0.0, route.getTotalDistanceKm(), 0.001);
    }

    @Test
    @DisplayName("Single-order route maintains stopSequence 1 and calculates distance from depot")
    void testOptimize_SingleOrder_CalculatesDistanceFromDepot() {
        // Vehicle at Bangalore MG Road
        Vehicle vehicle = createVehicle("VEH001", 12.9716, 77.5946);
        VehicleRoute route = VehicleRoute.builder()
                .vehicle(vehicle)
                .totalLoad(10.0)
                .totalDistanceKm(0.0)
                .assignments(new ArrayList<>())
                .build();

        // Order at Indiranagar (~5.07 km away)
        DeliveryOrder order = createOrder("ORD001", 12.9716, 77.6413);
        DispatchAssignment assignment = createAssignment(order, route, 1);
        route.getAssignments().add(assignment);

        optimizer.optimize(route);

        assertEquals(1, route.getAssignments().size());
        DispatchAssignment result = route.getAssignments().get(0);
        assertEquals(1, result.getStopSequence());
        assertTrue(result.getDistanceFromPreviousKm() > 4.5 && result.getDistanceFromPreviousKm() < 5.5,
                "Distance from depot should be approx 5 km");
        assertEquals(result.getDistanceFromPreviousKm(), route.getTotalDistanceKm(), 0.001);
    }

    @Test
    @DisplayName("Multiple orders are sequenced by proximity starting from vehicle depot")
    void testOptimize_MultipleOrders_SequencedByNearestNeighbor() {
        // Vehicle starts at origin (0.0, 0.0)
        Vehicle vehicle = createVehicle("VEH001", 0.0, 0.0);
        VehicleRoute route = VehicleRoute.builder()
                .vehicle(vehicle)
                .totalLoad(30.0)
                .totalDistanceKm(0.0)
                .assignments(new ArrayList<>())
                .build();

        // 3 orders along longitude:
        // Order Far: at (0.0, 3.0) ~333 km from origin
        // Order Close: at (0.0, 1.0) ~111 km from origin
        // Order Mid: at (0.0, 2.0) ~222 km from origin
        DeliveryOrder far = createOrder("FAR", 0.0, 3.0);
        DeliveryOrder close = createOrder("CLOSE", 0.0, 1.0);
        DeliveryOrder mid = createOrder("MID", 0.0, 2.0);

        // Put them in deliberately suboptimal order: FAR, MID, CLOSE
        route.getAssignments().add(createAssignment(far, route, 1));
        route.getAssignments().add(createAssignment(mid, route, 2));
        route.getAssignments().add(createAssignment(close, route, 3));

        optimizer.optimize(route);

        List<DispatchAssignment> optimized = route.getAssignments();
        assertEquals(3, optimized.size());

        // Expected order: CLOSE (nearest to depot), then MID (nearest to CLOSE), then FAR (nearest to MID)
        assertEquals("CLOSE", optimized.get(0).getOrder().getOrderId());
        assertEquals(1, optimized.get(0).getStopSequence());

        assertEquals("MID", optimized.get(1).getOrder().getOrderId());
        assertEquals(2, optimized.get(1).getStopSequence());

        assertEquals("FAR", optimized.get(2).getOrder().getOrderId());
        assertEquals(3, optimized.get(2).getStopSequence());

        // Total distance along a straight line should be equal to depot to FAR
        double directDistance = calculator.calculateDistance(0.0, 0.0, 0.0, 3.0);
        assertEquals(directDistance, route.getTotalDistanceKm(), 0.01,
                "Sequential straight line route distance must equal direct depot to furthest stop");
    }

    @Test
    @DisplayName("Vehicle starting location is strictly used as origin for nearest stop selection")
    void testOptimize_VehicleDepotSelectionOrigin() {
        // Vehicle located at (10.0, 0.0)
        Vehicle vehicle = createVehicle("VEH001", 10.0, 0.0);
        VehicleRoute route = VehicleRoute.builder()
                .vehicle(vehicle)
                .totalLoad(20.0)
                .totalDistanceKm(0.0)
                .assignments(new ArrayList<>())
                .build();

        // Order A at (9.0, 0.0) (1 degree away from depot)
        // Order B at (0.0, 0.0) (10 degrees away from depot)
        DeliveryOrder orderA = createOrder("ORD_NEAR_DEPOT", 9.0, 0.0);
        DeliveryOrder orderB = createOrder("ORD_FAR_DEPOT", 0.0, 0.0);

        // Insert B first, then A
        route.getAssignments().add(createAssignment(orderB, route, 1));
        route.getAssignments().add(createAssignment(orderA, route, 2));

        optimizer.optimize(route);

        // Order A must become stop 1 because it is closest to depot (10.0, 0.0)
        assertEquals("ORD_NEAR_DEPOT", route.getAssignments().get(0).getOrder().getOrderId());
        assertEquals(1, route.getAssignments().get(0).getStopSequence());

        assertEquals("ORD_FAR_DEPOT", route.getAssignments().get(1).getOrder().getOrderId());
        assertEquals(2, route.getAssignments().get(1).getStopSequence());
    }
}
