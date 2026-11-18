package com.dispatchloadbalancer.service;

import com.dispatchloadbalancer.dto.request.VehicleRequest;
import com.dispatchloadbalancer.dto.request.VehiclesRequest;
import com.dispatchloadbalancer.dto.response.VehicleResponse;
import com.dispatchloadbalancer.entity.Vehicle;
import com.dispatchloadbalancer.exception.DuplicateResourceException;
import com.dispatchloadbalancer.exception.InvalidVehicleException;
import com.dispatchloadbalancer.repository.DispatchPlanRepository;
import com.dispatchloadbalancer.repository.VehicleRepository;
import com.dispatchloadbalancer.service.impl.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DispatchPlanRepository dispatchPlanRepository;

    private VehicleServiceImpl vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleServiceImpl(vehicleRepository, dispatchPlanRepository);
    }

    @Test
    @DisplayName("Successfully save valid vehicles and mark plans stale")
    void testSaveVehicles_Success() {
        VehicleRequest req = VehicleRequest.builder()
                .vehicleId("VEH001")
                .capacity(100.0)
                .currentLatitude(12.9716)
                .currentLongitude(77.6413)
                .currentAddress("Indiranagar, Bangalore")
                .build();

        VehiclesRequest request = VehiclesRequest.builder()
                .vehicles(List.of(req))
                .build();

        Vehicle savedEntity = Vehicle.builder()
                .vehicleId("VEH001")
                .capacity(100.0)
                .currentLatitude(12.9716)
                .currentLongitude(77.6413)
                .currentAddress("Indiranagar, Bangalore")
                .build();

        when(vehicleRepository.findByVehicleIdIn(anyList())).thenReturn(Collections.emptyList());
        when(vehicleRepository.saveAll(anyList())).thenReturn(List.of(savedEntity));

        List<VehicleResponse> responses = vehicleService.saveVehicles(request);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("VEH001", responses.get(0).getVehicleId());
        verify(dispatchPlanRepository, times(1)).markAllPlannedAsStale();
        verify(vehicleRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Duplicate vehicleId in same request throws DuplicateResourceException")
    void testSaveVehicles_DuplicateInRequest_ThrowsException() {
        VehicleRequest req1 = VehicleRequest.builder()
                .vehicleId("VEH001")
                .capacity(100.0)
                .currentLatitude(12.9716)
                .currentLongitude(77.6413)
                .currentAddress("Depot 1")
                .build();

        VehicleRequest req2 = VehicleRequest.builder()
                .vehicleId("VEH001") // duplicate
                .capacity(150.0)
                .currentLatitude(13.0674)
                .currentLongitude(80.2376)
                .currentAddress("Depot 2")
                .build();

        VehiclesRequest request = VehiclesRequest.builder()
                .vehicles(List.of(req1, req2))
                .build();

        assertThrows(DuplicateResourceException.class, () -> vehicleService.saveVehicles(request));
        verify(vehicleRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Invalid vehicle capacity (<= 0) throws InvalidVehicleException")
    void testSaveVehicles_InvalidCapacity_ThrowsException() {
        VehicleRequest req = VehicleRequest.builder()
                .vehicleId("VEH001")
                .capacity(0.0) // invalid
                .currentLatitude(12.9716)
                .currentLongitude(77.6413)
                .currentAddress("Depot")
                .build();

        VehiclesRequest request = VehiclesRequest.builder()
                .vehicles(List.of(req))
                .build();

        assertThrows(InvalidVehicleException.class, () -> vehicleService.saveVehicles(request));
    }
}
