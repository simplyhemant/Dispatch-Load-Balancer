
package com.dispatchloadbalancer.repository;

import com.dispatchloadbalancer.entity.DispatchPlan;
import com.dispatchloadbalancer.entity.VehicleRoute;
import com.dispatchloadbalancer.enums.DispatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DispatchPlanRepository
        extends JpaRepository<DispatchPlan, Long> {

    // Query 1: Fetch plans, routes and vehicles.
    // Only one List collection (routes) is fetched here.
    @Query("""
            SELECT DISTINCT dp
            FROM DispatchPlan dp
            LEFT JOIN FETCH dp.routes r
            LEFT JOIN FETCH r.vehicle
            WHERE dp.status = :status
            ORDER BY dp.generatedAt DESC
            """)
    List<DispatchPlan> findPlansWithRoutesByStatus(
            @Param("status") DispatchStatus status
    );

    // Query 2: Fetch assignments and their delivery orders.
    @Query("""
            SELECT DISTINCT r
            FROM VehicleRoute r
            LEFT JOIN FETCH r.assignments a
            LEFT JOIN FETCH a.order
            WHERE r.dispatchPlan = :plan
            """)
    List<VehicleRoute> findRoutesWithAssignmentsByPlan(
            @Param("plan") DispatchPlan plan
    );

    // Query 3: Fetch unassigned orders and their delivery order details.
    @Query("""
            SELECT DISTINCT dp
            FROM DispatchPlan dp
            LEFT JOIN FETCH dp.unassignedOrders u
            LEFT JOIN FETCH u.order
            WHERE dp.id = :planId
            """)
    DispatchPlan fetchUnassignedOrdersForPlan(
            @Param("planId") Long planId
    );

    // Mark all previously generated valid plans as stale.
    @Modifying(
            flushAutomatically = true,
            clearAutomatically = true
    )
    @Query("""
            UPDATE DispatchPlan dp
            SET dp.status = :stale
            WHERE dp.status = :planned
            """)
    int markPlansAs(
            @Param("stale") DispatchStatus stale,
            @Param("planned") DispatchStatus planned
    );

    default int markAllPlannedAsStale() {
        return markPlansAs(
                DispatchStatus.STALE,
                DispatchStatus.PLANNED
        );
    }
}
