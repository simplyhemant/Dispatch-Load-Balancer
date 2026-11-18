package com.dispatchloadbalancer.repository;

import com.dispatchloadbalancer.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByVehicleId(String vehicleId);

    List<Vehicle> findByVehicleIdIn(Collection<String> vehicleIds);
}