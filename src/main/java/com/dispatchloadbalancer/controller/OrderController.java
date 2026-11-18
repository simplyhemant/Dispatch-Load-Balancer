package com.dispatchloadbalancer.controller;

import com.dispatchloadbalancer.dto.request.OrdersRequest;
import com.dispatchloadbalancer.dto.response.ApiResponse;
import com.dispatchloadbalancer.dto.response.OrderResponse;
import com.dispatchloadbalancer.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dispatch/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> saveOrders(@Valid @RequestBody OrdersRequest request) {
        List<OrderResponse> response = orderService.saveOrders(request);
        return null;

    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        List<OrderResponse> response = orderService.getAllOrders();
        return null;
    }
}
