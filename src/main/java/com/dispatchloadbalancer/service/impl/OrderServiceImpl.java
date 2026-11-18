package com.dispatchloadbalancer.service.impl;

import com.dispatchloadbalancer.dto.request.OrderRequest;
import com.dispatchloadbalancer.dto.request.OrdersRequest;
import com.dispatchloadbalancer.dto.response.OrderResponse;
import com.dispatchloadbalancer.entity.DeliveryOrder;
import com.dispatchloadbalancer.exception.DuplicateResourceException;
import com.dispatchloadbalancer.exception.InvalidOrderException;
import com.dispatchloadbalancer.repository.DispatchPlanRepository;
import com.dispatchloadbalancer.repository.OrderRepository;
import com.dispatchloadbalancer.service.OrderService;
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
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final DispatchPlanRepository dispatchPlanRepository;

    @Override
    public List<OrderResponse> saveOrders(OrdersRequest request) {
        if (request == null || request.getOrders() == null || request.getOrders().isEmpty()) {
            log.warn("Attempted to save orders with an empty request");
            throw new InvalidOrderException("Orders list cannot be empty.");
        }

        List<OrderRequest> orderRequests = request.getOrders();
        log.info("Processing ingestion of {} delivery orders", orderRequests.size());

        Set<String> seenOrderIds = new HashSet<>(orderRequests.size());
        List<String> requestedIds = new ArrayList<>(orderRequests.size());

        // 1. Validate fields & detect duplicates in a single pass
        for (OrderRequest req : orderRequests) {
            validateOrderRequest(req);

            String normalizedId = req.getOrderId().trim();
            if (!seenOrderIds.add(normalizedId)) {
                log.warn("Duplicate orderId detected in request payload: {}", normalizedId);
                throw new DuplicateResourceException("Duplicate orderId in request: " + normalizedId);
            }
            requestedIds.add(normalizedId);
        }

        // 2. Fetch existing orders in one batch query for upsert
        List<DeliveryOrder> existingList = orderRepository.findByOrderIdIn(requestedIds);
        Map<String, DeliveryOrder> existingByOrderId = new HashMap<>(existingList.size());
        for (DeliveryOrder existing : existingList) {
            existingByOrderId.put(existing.getOrderId(), existing);
        }

        int updatedCount = 0;
        int newCount = 0;

        // 3. Prepare entities (update existing or instantiate new)
        List<DeliveryOrder> ordersToSave = new ArrayList<>(orderRequests.size());
        for (OrderRequest req : orderRequests) {
            String normalizedId = req.getOrderId().trim();
            DeliveryOrder entity = existingByOrderId.get(normalizedId);
            if (entity == null) {
                entity = new DeliveryOrder();
                newCount++;
            } else {
                updatedCount++;
            }
            mapToEntity(req, entity);
            ordersToSave.add(entity);
        }

        // 4. Batch upsert & invalidate previous dispatch plans
        List<DeliveryOrder> savedOrders = orderRepository.saveAll(ordersToSave);
        int invalidatedPlans = dispatchPlanRepository.markAllPlannedAsStale();

        log.info("Successfully upserted {} orders (new: {}, updated: {}). Marked {} existing plans as stale.",
                savedOrders.size(), newCount, updatedCount, invalidatedPlans);

        // 5. Map saved entities to response DTOs
        List<OrderResponse> responses = new ArrayList<>(savedOrders.size());
        for (DeliveryOrder saved : savedOrders) {
            responses.add(mapToResponse(saved));
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        List<DeliveryOrder> orders = orderRepository.findAll();
        log.info("Fetched {} delivery orders from database", orders.size());
        List<OrderResponse> responses = new ArrayList<>(orders.size());

        for (DeliveryOrder order : orders) {
            responses.add(mapToResponse(order));
        }
        return responses;
    }

    private void validateOrderRequest(OrderRequest req) {
        if (req == null) {
            throw new InvalidOrderException("Order entry cannot be null.");
        }
        if (req.getOrderId() == null || req.getOrderId().isBlank()) {
            throw new InvalidOrderException("Order ID is required.");
        }
        if (req.getLatitude() == null || req.getLatitude() < -90.0 || req.getLatitude() > 90.0) {
            throw new InvalidOrderException("Invalid latitude for order " + req.getOrderId() + ": must be between -90 and 90.");
        }
        if (req.getLongitude() == null || req.getLongitude() < -180.0 || req.getLongitude() > 180.0) {
            throw new InvalidOrderException("Invalid longitude for order " + req.getOrderId() + ": must be between -180 and 180.");
        }
        if (req.getAddress() == null || req.getAddress().isBlank()) {
            throw new InvalidOrderException("Address is required for order " + req.getOrderId() + ".");
        }
        if (req.getPackageWeight() == null || req.getPackageWeight() <= 0) {
            throw new InvalidOrderException("Package weight must be greater than 0 for order " + req.getOrderId() + ".");
        }
        if (req.getPriority() == null) {
            throw new InvalidOrderException("Priority (HIGH, MEDIUM, LOW) is required for order " + req.getOrderId() + ".");
        }
    }

    private void mapToEntity(OrderRequest request, DeliveryOrder order) {
        order.setOrderId(request.getOrderId().trim());
        order.setLatitude(request.getLatitude());
        order.setLongitude(request.getLongitude());
        order.setAddress(request.getAddress().trim());
        order.setPackageWeight(request.getPackageWeight());
        order.setPriority(request.getPriority());
    }

    private OrderResponse mapToResponse(DeliveryOrder order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .latitude(order.getLatitude())
                .longitude(order.getLongitude())
                .address(order.getAddress())
                .packageWeight(order.getPackageWeight())
                .priority(order.getPriority())
                .build();
    }
}