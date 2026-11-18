package com.dispatchloadbalancer.controller;

import com.dispatchloadbalancer.dto.request.OrderRequest;
import com.dispatchloadbalancer.dto.request.OrdersRequest;
import com.dispatchloadbalancer.dto.request.VehicleRequest;
import com.dispatchloadbalancer.dto.request.VehiclesRequest;
import com.dispatchloadbalancer.enums.Priority;
import com.dispatchloadbalancer.service.DispatchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DispatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private DispatchService dispatchService;

    @BeforeEach
    void setUp() {
        dispatchService.reset();
    }

    @Test
    @DisplayName("GET /api/dispatch/plan with no data returns 400 Bad Request")
    void testGetDispatchPlan_NoData_Returns400() throws Exception {
        mockMvc.perform(get("/api/dispatch/plan"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("No delivery orders found. Please submit orders first."));
    }

    @Test
    @DisplayName("GET /api/dispatch/plan with orders and vehicles returns root dispatchPlan and HTTP 200")
    void testGetDispatchPlan_WithData_Returns200WithRootDispatchPlan() throws Exception {
        // 1. Ingest order
        OrderRequest order = OrderRequest.builder()
                .orderId("ORD_TEST_1")
                .latitude(12.9716)
                .longitude(77.5946)
                .address("MG Road, Bangalore, Karnataka, India")
                .packageWeight(10.0)
                .priority(Priority.HIGH)
                .build();
        OrdersRequest orderReq = OrdersRequest.builder().orders(List.of(order)).build();
        mockMvc.perform(post("/api/dispatch/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderReq)))
                .andExpect(status().isCreated());

        // 2. Ingest vehicle
        VehicleRequest vehicle = VehicleRequest.builder()
                .vehicleId("VEH_TEST_1")
                .capacity(100.0)
                .currentLatitude(12.9716)
                .currentLongitude(77.6413)
                .currentAddress("Indiranagar, Bangalore, Karnataka, India")
                .build();
        VehiclesRequest vehicleReq = VehiclesRequest.builder().vehicles(List.of(vehicle)).build();
        mockMvc.perform(post("/api/dispatch/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleReq)))
                .andExpect(status().isCreated());

        // 3. Retrieve plan
        mockMvc.perform(get("/api/dispatch/plan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dispatchPlan", notNullValue()))
                .andExpect(jsonPath("$.dispatchPlan", hasSize(1)))
                .andExpect(jsonPath("$.dispatchPlan[0].vehicleId").value("VEH_TEST_1"))
                .andExpect(jsonPath("$.dispatchPlan[0].totalLoad").value(10.0))
                .andExpect(jsonPath("$.dispatchPlan[0].totalDistance", containsString("km")))
                .andExpect(jsonPath("$.dispatchPlan[0].assignedOrders", hasSize(1)))
                .andExpect(jsonPath("$.dispatchPlan[0].assignedOrders[0].orderId").value("ORD_TEST_1"))
                .andExpect(jsonPath("$.unassignedOrders", notNullValue()));
    }

    @Test
    @DisplayName("DELETE /api/dispatch/reset clears all data and returns success message")
    void testReset_ClearsData_Returns200() throws Exception {
        mockMvc.perform(delete("/api/dispatch/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Dispatch data reset successfully"));

        // Subsequent plan fetch should return 400 because database is empty
        mockMvc.perform(get("/api/dispatch/plan"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }
}
