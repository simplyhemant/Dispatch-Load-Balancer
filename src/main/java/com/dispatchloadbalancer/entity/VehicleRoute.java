package com.dispatchloadbalancer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehicle_routes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VehicleRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispatch_plan_id", nullable = false)
    private DispatchPlan dispatchPlan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "total_load", nullable = false)
    private Double totalLoad;

    @Column(name = "total_distance_km", nullable = false)
    private Double totalDistanceKm;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stopSequence ASC")
    @Builder.Default
    private List<DispatchAssignment> assignments = new ArrayList<>();

    public void addAssignment(DispatchAssignment assignment) {
        assignments.add(assignment);
        assignment.setRoute(this);
    }
}