package com.dispatchloadbalancer.controller;

import com.dispatchloadbalancer.dto.request.VehicleRequest;
import com.dispatchloadbalancer.dto.request.VehiclesRequest;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VehicleControllerTest {

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
    @DisplayName("POST /api/dispatch/vehicles accepts valid vehicles and returns exact message")
    void testSaveVehicles_ValidPayload_Returns201() throws Exception {
        VehicleRequest vehicle = VehicleRequest.builder()
                .vehicleId("VEH001")
                .capacity(100.0)
                .currentLatitude(12.9716)
                .currentLongitude(77.6413)
                .currentAddress("Indiranagar, Bangalore, Karnataka, India")
                .build();

        VehiclesRequest request = VehiclesRequest.builder()
                .vehicles(List.of(vehicle))
                .build();

        mockMvc.perform(post("/api/dispatch/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Vehicle details accepted."));
    }

    @Test
    @DisplayName("GET /api/dispatch/vehicles returns empty list message when no vehicles exist")
    void testGetAllVehicles_EmptyList_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/dispatch/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("No vehicles found."))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/dispatch/vehicles returns registered fleet vehicles")
    void testGetAllVehicles_WithData_ReturnsOk() throws Exception {
        VehicleRequest vehicle = VehicleRequest.builder()
                .vehicleId("VEH002")
                .capacity(150.0)
                .currentLatitude(13.0674)
                .currentLongitude(80.2376)
                .currentAddress("T Nagar, Chennai, Tamil Nadu, India")
                .build();

        VehiclesRequest request = VehiclesRequest.builder()
                .vehicles(List.of(vehicle))
                .build();

        mockMvc.perform(post("/api/dispatch/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/dispatch/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].vehicleId").value("VEH002"));
    }

    @Test
    @DisplayName("POST /api/dispatch/vehicles with invalid capacity returns 400 Bad Request")
    void testSaveVehicles_InvalidCapacity_Returns400() throws Exception {
        String invalidJson = """
                {
                  "vehicles": [
                    {
                      "vehicleId": "VEH_ERR",
                      "capacity": -10.0,
                      "currentLatitude": 12.9716,
                      "currentLongitude": 77.6413,
                      "currentAddress": "Invalid Base"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/dispatch/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("POST /api/dispatch/vehicles with duplicate vehicleId returns 409 Conflict")
    void testSaveVehicles_DuplicateVehicleId_Returns409() throws Exception {
        VehicleRequest v1 = VehicleRequest.builder()
                .vehicleId("VEH_DUP")
                .capacity(100.0)
                .currentLatitude(12.9716)
                .currentLongitude(77.6413)
                .currentAddress("Depot 1")
                .build();

        VehicleRequest v2 = VehicleRequest.builder()
                .vehicleId("VEH_DUP")
                .capacity(120.0)
                .currentLatitude(13.0674)
                .currentLongitude(80.2376)
                .currentAddress("Depot 2")
                .build();

        VehiclesRequest request = VehiclesRequest.builder()
                .vehicles(List.of(v1, v2))
                .build();

        mockMvc.perform(post("/api/dispatch/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"));
    }
}
