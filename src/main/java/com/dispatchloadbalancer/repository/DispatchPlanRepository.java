package com.dispatchloadbalancer.repository;

import com.dispatchloadbalancer.entity.DispatchPlan;
import com.dispatchloadbalancer.enums.DispatchStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DispatchPlanRepository extends JpaRepository<DispatchPlan, Long> {

    // Latest cached plan with everything loaded (avoids N+1 and lazy errors)
    @EntityGraph(attributePaths = {
            "routes", "routes.vehicle",
            "routes.assignments", "routes.assignments.order",
            "unassignedOrders", "unassignedOrders.order"
    })
    Optional<DispatchPlan> findFirstByStatusOrderByGeneratedAtDesc(DispatchStatus status);

    // Called whenever orders or vehicles change
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update DispatchPlan p set p.status = :stale where p.status = :planned")
    int markPlansAs(@org.springframework.data.repository.query.Param("stale") DispatchStatus stale,
                    @org.springframework.data.repository.query.Param("planned") DispatchStatus planned);

    default int markAllPlannedAsStale() {
        return markPlansAs(DispatchStatus.STALE, DispatchStatus.PLANNED);
    }
}