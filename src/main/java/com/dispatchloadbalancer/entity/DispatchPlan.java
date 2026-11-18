package com.dispatchloadbalancer.entity;

import com.dispatchloadbalancer.enums.DispatchStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dispatch_plans", indexes = @Index(name = "idx_plan_status", columnList = "status"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DispatchPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private DispatchStatus status;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "total_distance_km", nullable = false)
    private Double totalDistanceKm;

    @OneToMany(mappedBy = "dispatchPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VehicleRoute> routes = new ArrayList<>();

    @OneToMany(mappedBy = "dispatchPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UnassignedOrder> unassignedOrders = new ArrayList<>();

    public void addRoute(VehicleRoute route) {
        routes.add(route);
        route.setDispatchPlan(this);
    }

    public void addUnassignedOrder(UnassignedOrder unassigned) {
        unassignedOrders.add(unassigned);
        unassigned.setDispatchPlan(this);
    }
}