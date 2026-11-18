package com.dispatchloadbalancer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dispatch_assignments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"route_id", "stop_sequence"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DispatchAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private VehicleRoute route;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private DeliveryOrder order;

    @Column(name = "stop_sequence", nullable = false)
    private Integer stopSequence;

    @Column(name = "distance_from_previous_km", nullable = false)
    private Double distanceFromPreviousKm;
}