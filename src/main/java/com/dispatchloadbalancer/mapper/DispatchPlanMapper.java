package com.dispatchloadbalancer.mapper;

import com.dispatchloadbalancer.dto.response.*;
import com.dispatchloadbalancer.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class DispatchPlanMapper {

    public DispatchPlanResponse toResponse(DispatchPlan plan) {
        List<VehicleDispatchResponse> routes = plan.getRoutes().stream()
                .map(this::toVehicleDispatch)
                .toList();

        List<UnassignedOrderResponse> unassigned = plan.getUnassignedOrders().stream()
                .map(this::toUnassigned)
                .toList();

        return DispatchPlanResponse.builder()
                .planId(plan.getId())
                .status(plan.getStatus())
                .generatedAt(plan.getGeneratedAt())
                .totalDistance(formatKm(plan.getTotalDistanceKm()))
                .dispatchPlan(routes)
                .unassignedOrders(unassigned)
                .build();
    }

    private VehicleDispatchResponse toVehicleDispatch(VehicleRoute route) {
        Vehicle vehicle = route.getVehicle();
        return VehicleDispatchResponse.builder()
                .vehicleId(vehicle.getVehicleId())
                .currentAddress(vehicle.getCurrentAddress())
                .capacity(vehicle.getCapacity())
                .totalLoad(route.getTotalLoad())
                .totalDistance(formatKm(route.getTotalDistanceKm()))
                .assignedOrders(route.getAssignments().stream().map(this::toAssigned).toList())
                .build();
    }

    private AssignedOrderResponse toAssigned(DispatchAssignment assignment) {
        DeliveryOrder order = assignment.getOrder();
        return AssignedOrderResponse.builder()
                .orderId(order.getOrderId())
                .latitude(order.getLatitude())
                .longitude(order.getLongitude())
                .address(order.getAddress())
                .packageWeight(order.getPackageWeight())
                .priority(order.getPriority())
                .stopSequence(assignment.getStopSequence())
                .distanceFromPrevious(formatKm(assignment.getDistanceFromPreviousKm()))
                .build();
    }

    private UnassignedOrderResponse toUnassigned(UnassignedOrder unassigned) {
        DeliveryOrder order = unassigned.getOrder();
        return UnassignedOrderResponse.builder()
                .orderId(order.getOrderId())
                .address(order.getAddress())
                .packageWeight(order.getPackageWeight())
                .priority(order.getPriority())
                .reason(unassigned.getReason())
                .build();
    }

    public static String formatKm(Double km) {
        return String.format(Locale.ROOT, "%.2f km", km == null ? 0.0 : km);
    }
}