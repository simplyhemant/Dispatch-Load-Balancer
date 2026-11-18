package com.dispatchloadbalancer;

import com.dispatchloadbalancer.dto.request.OrderRequest;
import com.dispatchloadbalancer.dto.request.OrdersRequest;
import com.dispatchloadbalancer.dto.request.VehicleRequest;
import com.dispatchloadbalancer.dto.request.VehiclesRequest;
import com.dispatchloadbalancer.dto.response.AssignedOrderResponse;
import com.dispatchloadbalancer.dto.response.DispatchPlanResponse;
import com.dispatchloadbalancer.dto.response.UnassignedOrderResponse;
import com.dispatchloadbalancer.dto.response.VehicleDispatchResponse;
import com.dispatchloadbalancer.enums.Priority;
import com.dispatchloadbalancer.enums.UnassignedReason;
import com.dispatchloadbalancer.service.DispatchService;
import com.dispatchloadbalancer.service.OrderService;
import com.dispatchloadbalancer.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DispatchPlanIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private DispatchService dispatchService;

    @BeforeEach
    void setUp() {
        dispatchService.reset();
    }

    private OrdersRequest buildSampleOrdersRequest() {
        List<OrderRequest> orders = List.of(
                new OrderRequest("ORD001", 28.6139, 77.2090, "Connaught Place, Delhi, India", 15.0, Priority.HIGH),
                new OrderRequest("ORD002", 28.6139, 77.2090, "Connaught Place, Delhi, India", 10.0, Priority.MEDIUM),
                new OrderRequest("ORD003", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, Priority.LOW),
                new OrderRequest("ORD004", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 25.0, Priority.HIGH),
                new OrderRequest("ORD005", 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India", 30.0, Priority.MEDIUM),
                new OrderRequest("ORD006", 28.6139, 77.2090, "Connaught Place, Delhi, India", 40.0, Priority.LOW),
                new OrderRequest("ORD007", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, Priority.HIGH),
                new OrderRequest("ORD008", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 25.0, Priority.HIGH),
                new OrderRequest("ORD009", 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India", 15.0, Priority.MEDIUM),
                new OrderRequest("ORD010", 28.6139, 77.2090, "Connaught Place, Delhi, India", 30.0, Priority.LOW),
                new OrderRequest("ORD011", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, Priority.HIGH),
                new OrderRequest("ORD012", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 10.0, Priority.MEDIUM),
                new OrderRequest("ORD013", 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India", 25.0, Priority.HIGH),
                new OrderRequest("ORD014", 28.6139, 77.2090, "Connaught Place, Delhi, India", 15.0, Priority.LOW),
                new OrderRequest("ORD015", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, Priority.HIGH),
                new OrderRequest("ORD016", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 30.0, Priority.LOW),
                new OrderRequest("ORD017", 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India", 15.0, Priority.MEDIUM),
                new OrderRequest("ORD018", 28.6139, 77.2090, "Connaught Place, Delhi, India", 10.0, Priority.HIGH),
                new OrderRequest("ORD019", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, Priority.LOW),
                new OrderRequest("ORD020", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 30.0, Priority.HIGH),
                new OrderRequest("ORD021", 28.6139, 77.2090, "Connaught Place, Delhi, India", 25.0, Priority.MEDIUM),
                new OrderRequest("ORD022", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, Priority.LOW),
                new OrderRequest("ORD023", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 30.0, Priority.HIGH),
                new OrderRequest("ORD024", 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India", 15.0, Priority.LOW),
                new OrderRequest("ORD025", 28.6139, 77.2090, "Connaught Place, Delhi, India", 20.0, Priority.MEDIUM),
                new OrderRequest("ORD026", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 25.0, Priority.HIGH),
                new OrderRequest("ORD027", 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India", 20.0, Priority.MEDIUM),
                new OrderRequest("ORD028", 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India", 15.0, Priority.LOW),
                new OrderRequest("ORD029", 28.6139, 77.2090, "Connaught Place, Delhi, India", 30.0, Priority.HIGH),
                new OrderRequest("ORD030", 28.7041, 77.1025, "Karol Bagh, Delhi, India", 20.0, Priority.LOW)
        );
        return new OrdersRequest(orders);
    }

    private VehiclesRequest buildSampleVehiclesRequest() {
        List<VehicleRequest> vehicles = List.of(
                new VehicleRequest("VEH001", 100.0, 28.7041, 77.1025, "Karol Bagh, Delhi, India"),
                new VehicleRequest("VEH002", 80.0, 28.5355, 77.3910, "Sector 18, Noida, Uttar Pradesh, India"),
                new VehicleRequest("VEH003", 120.0, 28.4595, 77.0266, "Cyber Hub, Gurgaon, Haryana, India"),
                new VehicleRequest("VEH004", 90.0, 28.6139, 77.2090, "Connaught Place, Delhi, India"),
                new VehicleRequest("VEH005", 110.0, 28.7041, 77.1025, "Karol Bagh, Delhi, India")
        );
        return new VehiclesRequest(vehicles);
    }

    @Test
    @DisplayName("End-to-End integration test with 30 sample orders and 5 vehicles verifying all constraints")
    void testDispatchPlanOptimization_WithSampleDataset() {
        // 1. Ingest orders and vehicles
        orderService.saveOrders(buildSampleOrdersRequest());
        vehicleService.saveVehicles(buildSampleVehiclesRequest());

        // 2. Generate dispatch plan
        DispatchPlanResponse plan = dispatchService.getDispatchPlan();

        assertNotNull(plan, "Dispatch plan response must not be null");
        assertNotNull(plan.getPlanId(), "Plan ID must be populated upon persistence");
        assertNotNull(plan.getDispatchPlan(), "Vehicle routes list must not be null");
        assertNotNull(plan.getUnassignedOrders(), "Unassigned orders list must not be null");

        // 3. Accounting: All 30 orders must be accounted for without duplication
        Set<String> assignedOrderIds = new HashSet<>();
        int totalAssignedCount = 0;

        for (VehicleDispatchResponse route : plan.getDispatchPlan()) {
            assertNotNull(route.getVehicleId());
            assertTrue(route.getTotalLoad() <= route.getCapacity(),
                    String.format("Vehicle %s load (%.2f) must not exceed capacity (%.2f)",
                            route.getVehicleId(), route.getTotalLoad(), route.getCapacity()));

            double calculatedRouteLoad = 0.0;
            for (int i = 0; i < route.getAssignedOrders().size(); i++) {
                AssignedOrderResponse assigned = route.getAssignedOrders().get(i);
                assertEquals(i + 1, assigned.getStopSequence(), "Stop sequence must be 1-based and contiguous");

                boolean isNew = assignedOrderIds.add(assigned.getOrderId());
                assertTrue(isNew, "Order " + assigned.getOrderId() + " was assigned multiple times!");
                totalAssignedCount++;

                calculatedRouteLoad += assigned.getPackageWeight();
            }

            assertEquals(calculatedRouteLoad, route.getTotalLoad(), 0.001,
                    "Vehicle totalLoad must match sum of assigned order weights");
        }

        Set<String> unassignedOrderIds = new HashSet<>();
        for (UnassignedOrderResponse unassigned : plan.getUnassignedOrders()) {
            assertNotNull(unassigned.getOrderId());
            assertNotNull(unassigned.getReason(), "Unassigned order must provide a valid reason");
            assertTrue(unassigned.getReason() == UnassignedReason.INSUFFICIENT_REMAINING_CAPACITY ||
                            unassigned.getReason() == UnassignedReason.EXCEEDS_ALL_VEHICLE_CAPACITY,
                    "Reason must be a valid UnassignedReason enum");

            boolean isNew = unassignedOrderIds.add(unassigned.getOrderId());
            assertTrue(isNew, "Order " + unassigned.getOrderId() + " appears in unassigned list multiple times!");
        }

        // Mutual exclusivity between assigned and unassigned
        Set<String> intersection = new HashSet<>(assignedOrderIds);
        intersection.retainAll(unassignedOrderIds);
        assertTrue(intersection.isEmpty(), "Orders cannot be both assigned and unassigned: " + intersection);

        // Exactly 30 unique orders accounted for
        assertEquals(30, assignedOrderIds.size() + unassignedOrderIds.size(),
                "Every single one of the 30 orders must be accounted for");
        assertEquals(totalAssignedCount, assignedOrderIds.size(),
                "No duplicate assignments across all vehicles");

        // 4. Priority Constraint Verification:
        // All HIGH-priority orders (275 kg total) must be assigned because total fleet capacity is 500 kg.
        // All MEDIUM-priority orders (145 kg total) + HIGH (275 kg) = 420 kg <= 500 kg, so all should be assigned.
        // Therefore, any unassigned orders must have LOW priority.
        for (UnassignedOrderResponse unassigned : plan.getUnassignedOrders()) {
            assertEquals(Priority.LOW, unassigned.getPriority(),
                    "Only LOW priority orders should be unassigned when capacity is exhausted: " + unassigned.getOrderId());
            assertEquals(UnassignedReason.INSUFFICIENT_REMAINING_CAPACITY, unassigned.getReason());
        }

        // 5. Total Distance consistency
        assertNotNull(plan.getTotalDistance());
        assertTrue(plan.getTotalDistance().endsWith(" km"));

        // 6. Persistence & Cache Retrieval Verification
        DispatchPlanResponse cachedPlan = dispatchService.getDispatchPlan();
        assertEquals(plan.getPlanId(), cachedPlan.getPlanId(), "Subsequent call must return cached plan");
        assertEquals(plan.getDispatchPlan().size(), cachedPlan.getDispatchPlan().size());
        assertEquals(plan.getUnassignedOrders().size(), cachedPlan.getUnassignedOrders().size());
    }

    @Test
    @DisplayName("Reset clears all orders, vehicles, and previous dispatch plans cleanly")
    void testReset_ClearsAllData() {
        orderService.saveOrders(buildSampleOrdersRequest());
        vehicleService.saveVehicles(buildSampleVehiclesRequest());
        dispatchService.getDispatchPlan();

        // Perform reset
        dispatchService.reset();

        // Orders list should now be empty
        assertTrue(orderService.getAllOrders().isEmpty());
        // Vehicles list should now be empty
        assertTrue(vehicleService.getAllVehicles().isEmpty());
    }
}
