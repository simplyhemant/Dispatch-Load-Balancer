package com.dispatchloadbalancer.service.impl;

import com.dispatchloadbalancer.dto.request.VehicleRequest;
import com.dispatchloadbalancer.dto.request.VehiclesRequest;
import com.dispatchloadbalancer.dto.response.VehicleResponse;
import com.dispatchloadbalancer.entity.Vehicle;
import com.dispatchloadbalancer.exception.DuplicateResourceException;
import com.dispatchloadbalancer.exception.InvalidVehicleException;
import com.dispatchloadbalancer.repository.DispatchPlanRepository;
import com.dispatchloadbalancer.repository.VehicleRepository;
import com.dispatchloadbalancer.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DispatchPlanRepository dispatchPlanRepository;

    @Override
    public List<VehicleResponse> saveVehicles(VehiclesRequest request) {
        if (request == null || request.getVehicles() == null || request.getVehicles().isEmpty()) {
            log.warn("Attempted to save vehicles with an empty request");
            throw new InvalidVehicleException("Vehicles list cannot be empty.");
        }

        List<VehicleRequest> vehicleRequests = request.getVehicles();
        log.info("Processing ingestion of {} fleet vehicles", vehicleRequests.size());

        Set<String> seenVehicleIds = new HashSet<>(vehicleRequests.size());
        List<String> requestedIds = new ArrayList<>(vehicleRequests.size());

        // 1. Validate fields & detect duplicates in a single pass
        for (VehicleRequest req : vehicleRequests) {
            validateVehicleRequest(req);

            String normalizedId = req.getVehicleId().trim();
            if (!seenVehicleIds.add(normalizedId)) {
                log.warn("Duplicate vehicleId detected in request payload: {}", normalizedId);
                throw new DuplicateResourceException("Duplicate vehicleId in request: " + normalizedId);
            }
            requestedIds.add(normalizedId);
        }

        // 2. Fetch existing vehicles in one batch query for upsert
        List<Vehicle> existingList = vehicleRepository.findByVehicleIdIn(requestedIds);
        Map<String, Vehicle> existingByVehicleId = new HashMap<>(existingList.size());
        for (Vehicle existing : existingList) {
            existingByVehicleId.put(existing.getVehicleId(), existing);
        }

        int updatedCount = 0;
        int newCount = 0;

        // 3. Prepare entities (update existing or instantiate new)
        List<Vehicle> vehiclesToSave = new ArrayList<>(vehicleRequests.size());
        for (VehicleRequest req : vehicleRequests) {
            String normalizedId = req.getVehicleId().trim();
            Vehicle entity = existingByVehicleId.get(normalizedId);
            if (entity == null) {
                entity = new Vehicle();
                newCount++;
            } else {
                updatedCount++;
            }
            mapToEntity(req, entity);
            vehiclesToSave.add(entity);
        }

        // 4. Batch upsert & invalidate previous dispatch plans
        List<Vehicle> savedVehicles = vehicleRepository.saveAll(vehiclesToSave);
        int invalidatedPlans = dispatchPlanRepository.markAllPlannedAsStale();

        log.info("Successfully upserted {} vehicles (new: {}, updated: {}). Marked {} existing plans as stale.",
                savedVehicles.size(), newCount, updatedCount, invalidatedPlans);

        // 5. Map saved entities to response DTOs
        List<VehicleResponse> responses = new ArrayList<>(savedVehicles.size());
        for (Vehicle saved : savedVehicles) {
            responses.add(mapToResponse(saved));
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        log.info("Fetched {} fleet vehicles from database", vehicles.size());
        List<VehicleResponse> responses = new ArrayList<>(vehicles.size());

        for (Vehicle vehicle : vehicles) {
            responses.add(mapToResponse(vehicle));
        }
        return responses;
    }

    private void validateVehicleRequest(VehicleRequest req) {
        if (req == null) {
            throw new InvalidVehicleException("Vehicle entry cannot be null.");
        }
        if (req.getVehicleId() == null || req.getVehicleId().isBlank()) {
            throw new InvalidVehicleException("Vehicle ID is required.");
        }
        if (req.getCapacity() == null || req.getCapacity() <= 0) {
            throw new InvalidVehicleException("Capacity must be greater than 0 for vehicle " + req.getVehicleId() + ".");
        }
        if (req.getCurrentLatitude() == null || req.getCurrentLatitude() < -90.0 || req.getCurrentLatitude() > 90.0) {
            throw new InvalidVehicleException("Invalid latitude for vehicle " + req.getVehicleId() + ": must be between -90 and 90.");
        }
        if (req.getCurrentLongitude() == null || req.getCurrentLongitude() < -180.0 || req.getCurrentLongitude() > 180.0) {
            throw new InvalidVehicleException("Invalid longitude for vehicle " + req.getVehicleId() + ": must be between -180 and 180.");
        }
        if (req.getCurrentAddress() == null || req.getCurrentAddress().isBlank()) {
            throw new InvalidVehicleException("Current address is required for vehicle " + req.getVehicleId() + ".");
        }
    }

    private void mapToEntity(VehicleRequest request, Vehicle vehicle) {
        vehicle.setVehicleId(request.getVehicleId().trim());
        vehicle.setCapacity(request.getCapacity());
        vehicle.setCurrentLatitude(request.getCurrentLatitude());
        vehicle.setCurrentLongitude(request.getCurrentLongitude());
        vehicle.setCurrentAddress(request.getCurrentAddress().trim());
    }

    private VehicleResponse mapToResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .vehicleId(vehicle.getVehicleId())
                .capacity(vehicle.getCapacity())
                .currentLatitude(vehicle.getCurrentLatitude())
                .currentLongitude(vehicle.getCurrentLongitude())
                .currentAddress(vehicle.getCurrentAddress())
                .build();
    }
}