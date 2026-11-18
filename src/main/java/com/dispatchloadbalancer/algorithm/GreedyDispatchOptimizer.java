package com.dispatchloadbalancer.algorithm;

import com.dispatchloadbalancer.entity.DeliveryOrder;
import com.dispatchloadbalancer.entity.DispatchPlan;
import com.dispatchloadbalancer.entity.Vehicle;
import com.dispatchloadbalancer.strategy.DispatchOptimizationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GreedyDispatchOptimizer implements DispatchOptimizationStrategy {

    private final HaversineCalculator haversineCalculator;

    @Override
    public DispatchPlan optimize(List<DeliveryOrder> orders, List<Vehicle> vehicles) {
        // TODO: Implement greedy dispatch optimization algorithm
        return null;
    }
}
