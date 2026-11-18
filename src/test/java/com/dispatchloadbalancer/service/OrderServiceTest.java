package com.dispatchloadbalancer.service;

import com.dispatchloadbalancer.dto.request.OrderRequest;
import com.dispatchloadbalancer.dto.request.OrdersRequest;
import com.dispatchloadbalancer.dto.response.OrderResponse;
import com.dispatchloadbalancer.entity.DeliveryOrder;
import com.dispatchloadbalancer.enums.Priority;
import com.dispatchloadbalancer.exception.DuplicateResourceException;
import com.dispatchloadbalancer.exception.InvalidOrderException;
import com.dispatchloadbalancer.repository.DispatchPlanRepository;
import com.dispatchloadbalancer.repository.OrderRepository;
import com.dispatchloadbalancer.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DispatchPlanRepository dispatchPlanRepository;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, dispatchPlanRepository);
    }

    @Test
    @DisplayName("Successfully save valid delivery orders and mark plans stale")
    void testSaveOrders_Success() {
        OrderRequest req = OrderRequest.builder()
                .orderId("ORD001")
                .latitude(12.9716)
                .longitude(77.5946)
                .address("MG Road, Bangalore")
                .packageWeight(10.0)
                .priority(Priority.HIGH)
                .build();

        OrdersRequest request = OrdersRequest.builder()
                .orders(List.of(req))
                .build();

        DeliveryOrder savedEntity = DeliveryOrder.builder()
                .orderId("ORD001")
                .latitude(12.9716)
                .longitude(77.5946)
                .address("MG Road, Bangalore")
                .packageWeight(10.0)
                .priority(Priority.HIGH)
                .build();

        when(orderRepository.findByOrderIdIn(anyList())).thenReturn(Collections.emptyList());
        when(orderRepository.saveAll(anyList())).thenReturn(List.of(savedEntity));

        List<OrderResponse> responses = orderService.saveOrders(request);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("ORD001", responses.get(0).getOrderId());
        verify(dispatchPlanRepository, times(1)).markAllPlannedAsStale();
        verify(orderRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Duplicate orderId within same request throws DuplicateResourceException")
    void testSaveOrders_DuplicateInRequest_ThrowsException() {
        OrderRequest req1 = OrderRequest.builder()
                .orderId("ORD001")
                .latitude(12.9716)
                .longitude(77.5946)
                .address("Address 1")
                .packageWeight(10.0)
                .priority(Priority.HIGH)
                .build();

        OrderRequest req2 = OrderRequest.builder()
                .orderId("ORD001") // duplicate
                .latitude(13.0827)
                .longitude(80.2707)
                .address("Address 2")
                .packageWeight(15.0)
                .priority(Priority.MEDIUM)
                .build();

        OrdersRequest request = OrdersRequest.builder()
                .orders(List.of(req1, req2))
                .build();

        assertThrows(DuplicateResourceException.class, () -> orderService.saveOrders(request));
        verify(orderRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Invalid package weight (<= 0) throws InvalidOrderException")
    void testSaveOrders_InvalidWeight_ThrowsException() {
        OrderRequest req = OrderRequest.builder()
                .orderId("ORD001")
                .latitude(12.9716)
                .longitude(77.5946)
                .address("Address")
                .packageWeight(-5.0) // negative
                .priority(Priority.HIGH)
                .build();

        OrdersRequest request = OrdersRequest.builder()
                .orders(List.of(req))
                .build();

        assertThrows(InvalidOrderException.class, () -> orderService.saveOrders(request));
    }
}
