package com.dispatchloadbalancer.controller;

import com.dispatchloadbalancer.dto.request.OrderRequest;
import com.dispatchloadbalancer.dto.request.OrdersRequest;
import com.dispatchloadbalancer.enums.Priority;
import com.dispatchloadbalancer.service.DispatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerTest {

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
    @DisplayName("POST /api/dispatch/orders accepts valid order payload with 201 Created")
    void testSaveOrders_ValidPayload_Returns201() throws Exception {
        OrderRequest order = OrderRequest.builder()
                .orderId("ORD101")
                .latitude(12.9716)
                .longitude(77.5946)
                .address("MG Road, Bangalore, Karnataka, India")
                .packageWeight(15.0)
                .priority(Priority.HIGH)
                .build();

        OrdersRequest request = OrdersRequest.builder()
                .orders(List.of(order))
                .build();

        mockMvc.perform(post("/api/dispatch/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Delivery orders accepted."));
    }

    @Test
    @DisplayName("GET /api/dispatch/orders returns empty list message when no orders exist")
    void testGetAllOrders_EmptyList_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/dispatch/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("No orders found."))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/dispatch/orders returns ingested orders successfully")
    void testGetAllOrders_WithData_ReturnsOk() throws Exception {
        OrderRequest order = OrderRequest.builder()
                .orderId("ORD102")
                .latitude(13.0827)
                .longitude(80.2707)
                .address("Anna Salai, Chennai, Tamil Nadu, India")
                .packageWeight(20.0)
                .priority(Priority.MEDIUM)
                .build();

        OrdersRequest request = OrdersRequest.builder()
                .orders(List.of(order))
                .build();

        mockMvc.perform(post("/api/dispatch/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/dispatch/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].orderId").value("ORD102"));
    }

    @Test
    @DisplayName("POST /api/dispatch/orders with invalid coordinates returns 400 Bad Request")
    void testSaveOrders_InvalidCoordinates_Returns400() throws Exception {
        String invalidJson = """
                {
                  "orders": [
                    {
                      "orderId": "ORD_ERR",
                      "latitude": 95.0,
                      "longitude": 77.0,
                      "address": "Invalid Point",
                      "packageWeight": 10.0,
                      "priority": "HIGH"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/dispatch/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("POST /api/dispatch/orders with duplicate orderId in payload returns 409 Conflict")
    void testSaveOrders_DuplicateOrderId_Returns409() throws Exception {
        OrderRequest order1 = OrderRequest.builder()
                .orderId("ORD_DUP")
                .latitude(12.9716)
                .longitude(77.5946)
                .address("Location 1")
                .packageWeight(10.0)
                .priority(Priority.HIGH)
                .build();

        OrderRequest order2 = OrderRequest.builder()
                .orderId("ORD_DUP")
                .latitude(12.9716)
                .longitude(77.5946)
                .address("Location 2")
                .packageWeight(20.0)
                .priority(Priority.LOW)
                .build();

        OrdersRequest request = OrdersRequest.builder()
                .orders(List.of(order1, order2))
                .build();

        mockMvc.perform(post("/api/dispatch/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"));
    }
}
