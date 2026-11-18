package com.dispatchloadbalancer.controller;

import com.dispatchloadbalancer.dto.request.VehiclesRequest;
import com.dispatchloadbalancer.dto.response.ApiResponse;
import com.dispatchloadbalancer.dto.response.VehicleResponse;
import com.dispatchloadbalancer.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dispatch/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> saveVehicles(@Valid @RequestBody VehiclesRequest request) {
        List<VehicleResponse> response = vehicleService.saveVehicles(request);
        return null;

    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAllVehicles() {
        List<VehicleResponse> response = vehicleService.getAllVehicles();
        return null;
    }
}
