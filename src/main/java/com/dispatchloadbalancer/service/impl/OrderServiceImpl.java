package com.dispatchloadbalancer.service.impl;

import com.dispatchloadbalancer.dto.request.OrdersRequest;
import com.dispatchloadbalancer.dto.response.OrderResponse;
import com.dispatchloadbalancer.repository.OrderRepository;
import com.dispatchloadbalancer.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public List<OrderResponse> saveOrders(OrdersRequest request) {
        // TODO: Implement order validation and saving logic
        return null;
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        // TODO: Implement retrieve all orders logic
        return null;
    }
}
