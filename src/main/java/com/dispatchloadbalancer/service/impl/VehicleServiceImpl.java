package com.dispatchloadbalancer.service.impl;

import com.dispatchloadbalancer.dto.request.VehiclesRequest;
import com.dispatchloadbalancer.dto.response.VehicleResponse;
import com.dispatchloadbalancer.repository.VehicleRepository;
import com.dispatchloadbalancer.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    @Override
    public List<VehicleResponse> saveVehicles(VehiclesRequest request) {
        // TODO: Implement vehicle validation and saving logic
        return null;
    }

    @Override
    public List<VehicleResponse> getAllVehicles() {
        // TODO: Implement retrieve all vehicles logic
        return null;
    }
}
