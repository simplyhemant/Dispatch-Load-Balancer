package com.dispatchloadbalancer.repository;

import com.dispatchloadbalancer.entity.DeliveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<DeliveryOrder, Long> {

    Optional<DeliveryOrder> findByOrderId(String orderId);

    // Batch upsert: one query for the whole request
    List<DeliveryOrder> findByOrderIdIn(Collection<String> orderIds);
}