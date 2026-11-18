package com.dispatchloadbalancer.service;

import com.dispatchloadbalancer.dto.request.OrdersRequest;
import com.dispatchloadbalancer.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    List<OrderResponse> saveOrders(OrdersRequest request);

    List<OrderResponse> getAllOrders();
}
