package com.dispatchloadbalancer.service;

import com.dispatchloadbalancer.dto.request.VehiclesRequest;
import com.dispatchloadbalancer.dto.response.VehicleResponse;

import java.util.List;

public interface VehicleService {

    List<VehicleResponse> saveVehicles(VehiclesRequest request);

    List<VehicleResponse> getAllVehicles();
}
