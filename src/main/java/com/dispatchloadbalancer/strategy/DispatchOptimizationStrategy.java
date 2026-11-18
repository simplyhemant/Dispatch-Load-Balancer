package com.dispatchloadbalancer.strategy;

import com.dispatchloadbalancer.entity.DeliveryOrder;
import com.dispatchloadbalancer.entity.DispatchPlan;
import com.dispatchloadbalancer.entity.Vehicle;

import java.util.List;

public interface DispatchOptimizationStrategy {

    DispatchPlan optimize(List<DeliveryOrder> orders, List<Vehicle> vehicles);
}
