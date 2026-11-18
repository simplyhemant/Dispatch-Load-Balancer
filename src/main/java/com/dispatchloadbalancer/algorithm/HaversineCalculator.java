package com.dispatchloadbalancer.algorithm;

import org.springframework.stereotype.Component;

@Component
public class HaversineCalculator {

    /**
     * Calculates the great-circle distance between two geographic coordinates using the Haversine formula.
     *
     * @param startLat Latitude of the origin point in degrees
     * @param startLon Longitude of the origin point in degrees
     * @param endLat   Latitude of the destination point in degrees
     * @param endLon   Longitude of the destination point in degrees
     * @return Distance in kilometers
     */
    public double calculateDistance(double startLat, double startLon, double endLat, double endLon) {
        // TODO: Implement Haversine formula calculation
        return 0.0;
    }
}
