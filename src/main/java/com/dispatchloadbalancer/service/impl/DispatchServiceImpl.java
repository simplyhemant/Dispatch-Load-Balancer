package com.dispatchloadbalancer.service.impl;

import com.dispatchloadbalancer.dto.request.DispatchRequest;
import com.dispatchloadbalancer.dto.response.DispatchPlanResponse;
import com.dispatchloadbalancer.repository.DispatchPlanRepository;
import com.dispatchloadbalancer.repository.OrderRepository;
import com.dispatchloadbalancer.repository.VehicleRepository;
import com.dispatchloadbalancer.service.DispatchService;
import com.dispatchloadbalancer.strategy.DispatchOptimizationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DispatchServiceImpl implements DispatchService {

    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;
    private final DispatchPlanRepository dispatchPlanRepository;
    private final DispatchOptimizationStrategy dispatchOptimizationStrategy;

    @Override
    public DispatchPlanResponse generateDispatchPlan(DispatchRequest request) {
        // TODO: Implement dispatch plan generation logic
        return null;
    }
}
