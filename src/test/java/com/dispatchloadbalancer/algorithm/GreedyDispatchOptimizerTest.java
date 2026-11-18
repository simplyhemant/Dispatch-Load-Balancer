package com.dispatchloadbalancer.algorithm;

import com.dispatchloadbalancer.entity.DeliveryOrder;
import com.dispatchloadbalancer.entity.DispatchPlan;
import com.dispatchloadbalancer.entity.Vehicle;
import com.dispatchloadbalancer.entity.VehicleRoute;
import com.dispatchloadbalancer.enums.Priority;
import com.dispatchloadbalancer.enums.UnassignedReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GreedyDispatchOptimizerTest {

    private GreedyDispatchOptimizer optimizer;
    private HaversineCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new HaversineCalculator();
        optimizer = new GreedyDispatchOptimizer(calculator);
    }

    @Test
    @DisplayName("HIGH priority orders must be assigned before MEDIUM and LOW orders when capacity is tight")
    void testPriorityPrecedence() {
        // 1 Vehicle with capacity 25 kg
        Vehicle vehicle = Vehicle.builder()
                .vehicleId("VEH001")
                .capacity(25.0)
                .currentLatitude(28.6139)
                .currentLongitude(77.2090)
                .currentAddress("Connaught Place, Delhi")
                .build();

        // 3 orders of weight 20 kg each with different priorities
        DeliveryOrder lowOrder = DeliveryOrder.builder()
                .orderId("ORD_LOW")
                .latitude(28.6200)
                .longitude(77.2100)
                .packageWeight(20.0)
                .priority(Priority.LOW)
                .address("CP Outer")
                .build();

        DeliveryOrder highOrder = DeliveryOrder.builder()
                .orderId("ORD_HIGH")
                .latitude(28.6300)
                .longitude(77.2200)
                .packageWeight(20.0)
                .priority(Priority.HIGH)
                .address("CP Inner")
                .build();

        DeliveryOrder mediumOrder = DeliveryOrder.builder()
                .orderId("ORD_MED")
                .latitude(28.6250)
                .longitude(77.2150)
                .packageWeight(20.0)
                .priority(Priority.MEDIUM)
                .address("CP Middle")
                .build();

        // Pass in arbitrary order: LOW first, HIGH second, MEDIUM third
        List<DeliveryOrder> orders = List.of(lowOrder, highOrder, mediumOrder);
        List<Vehicle> vehicles = List.of(vehicle);

        DispatchPlan plan = optimizer.optimize(orders, vehicles);

        assertNotNull(plan);
        assertEquals(1, plan.getRoutes().size(), "Only one route should have assignments");
        VehicleRoute route = plan.getRoutes().get(0);
        assertEquals(1, route.getAssignments().size(), "Only 1 order fits in 25 kg vehicle");

        // The single assigned order MUST be ORD_HIGH
        assertEquals("ORD_HIGH", route.getAssignments().get(0).getOrder().getOrderId());

        // The remaining 2 orders should be in unassigned list
        assertEquals(2, plan.getUnassignedOrders().size());
    }

    @Test
    @DisplayName("Vehicle capacity must never be exceeded")
    void testCapacityConstraint() {
        Vehicle vehicle = Vehicle.builder()
                .vehicleId("VEH001")
                .capacity(50.0)
                .currentLatitude(28.6139)
                .currentLongitude(77.2090)
                .currentAddress("Connaught Place, Delhi")
                .build();

        DeliveryOrder order1 = DeliveryOrder.builder()
                .orderId("ORD001")
                .latitude(28.6200)
                .longitude(77.2100)
                .packageWeight(30.0)
                .priority(Priority.HIGH)
                .address("Loc 1")
                .build();

        DeliveryOrder order2 = DeliveryOrder.builder()
                .orderId("ORD002")
                .latitude(28.6300)
                .longitude(77.2200)
                .packageWeight(25.0) // 30 + 25 = 55 > 50
                .priority(Priority.HIGH)
                .address("Loc 2")
                .build();

        DispatchPlan plan = optimizer.optimize(List.of(order1, order2), List.of(vehicle));

        VehicleRoute route = plan.getRoutes().get(0);
        assertTrue(route.getTotalLoad() <= vehicle.getCapacity(),
                "Total load (" + route.getTotalLoad() + ") must be <= capacity (" + vehicle.getCapacity() + ")");
        assertEquals(1, route.getAssignments().size());
        assertEquals(1, plan.getUnassignedOrders().size());
    }

    @Test
    @DisplayName("Order exceeding all vehicle capacities gets EXCEEDS_ALL_VEHICLE_CAPACITY")
    void testUnassignedReason_ExceedsAllVehicleCapacity() {
        Vehicle vehicle = Vehicle.builder()
                .vehicleId("VEH001")
                .capacity(50.0)
                .currentLatitude(28.6139)
                .currentLongitude(77.2090)
                .currentAddress("Depot")
                .build();

        DeliveryOrder oversizedOrder = DeliveryOrder.builder()
                .orderId("ORD_BIG")
                .latitude(28.6200)
                .longitude(77.2100)
                .packageWeight(75.0) // 75 > 50
                .priority(Priority.HIGH)
                .address("Loc Big")
                .build();

        DispatchPlan plan = optimizer.optimize(List.of(oversizedOrder), List.of(vehicle));

        assertEquals(0, plan.getRoutes().size());
        assertEquals(1, plan.getUnassignedOrders().size());
        assertEquals(UnassignedReason.EXCEEDS_ALL_VEHICLE_CAPACITY,
                plan.getUnassignedOrders().get(0).getReason());
    }

    @Test
    @DisplayName("Order that fits in a vehicle originally but fleet is filled gets INSUFFICIENT_REMAINING_CAPACITY")
    void testUnassignedReason_InsufficientRemainingCapacity() {
        Vehicle vehicle = Vehicle.builder()
                .vehicleId("VEH001")
                .capacity(50.0)
                .currentLatitude(28.6139)
                .currentLongitude(77.2090)
                .currentAddress("Depot")
                .build();

        DeliveryOrder firstOrder = DeliveryOrder.builder()
                .orderId("ORD_1")
                .latitude(28.6200)
                .longitude(77.2100)
                .packageWeight(30.0)
                .priority(Priority.HIGH)
                .address("Loc 1")
                .build();

        DeliveryOrder secondOrder = DeliveryOrder.builder()
                .orderId("ORD_2")
                .latitude(28.6300)
                .longitude(77.2200)
                .packageWeight(30.0) // 30 fits in 50 empty, but remaining is only 20
                .priority(Priority.LOW)
                .address("Loc 2")
                .build();

        DispatchPlan plan = optimizer.optimize(List.of(firstOrder, secondOrder), List.of(vehicle));

        assertEquals(1, plan.getUnassignedOrders().size());
        assertEquals(UnassignedReason.INSUFFICIENT_REMAINING_CAPACITY,
                plan.getUnassignedOrders().get(0).getReason());
    }
}
