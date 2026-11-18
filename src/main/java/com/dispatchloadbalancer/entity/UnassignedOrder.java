package com.dispatchloadbalancer.entity;

import com.dispatchloadbalancer.enums.UnassignedReason;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "unassigned_orders",
       uniqueConstraints = @UniqueConstraint(columnNames = {"dispatch_plan_id", "order_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UnassignedOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispatch_plan_id", nullable = false)
    private DispatchPlan dispatchPlan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private DeliveryOrder order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private UnassignedReason reason;
}