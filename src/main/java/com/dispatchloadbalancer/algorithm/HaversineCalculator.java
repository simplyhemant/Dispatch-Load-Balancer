
package com.dispatchloadbalancer.algorithm;

import org.springframework.stereotype.Component;

@Component
public class HaversineCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0088;

    /**
     * Calculates the great-circle distance between two geographic
     * coordinates using the Haversine formula.
     *
     * @param startLat Latitude of the origin point in degrees
     * @param startLon Longitude of the origin point in degrees
     * @param endLat   Latitude of the destination point in degrees
     * @param endLon   Longitude of the destination point in degrees
     * @return Distance in kilometers
     */
    public double calculateDistance(
            double startLat,
            double startLon,
            double endLat,
            double endLon) {

        validateCoordinates(startLat, startLon);
        validateCoordinates(endLat, endLon);

        double lat1 = Math.toRadians(startLat);
        double lon1 = Math.toRadians(startLon);

        double lat2 = Math.toRadians(endLat);
        double lon2 = Math.toRadians(endLon);

        double deltaLat = lat2 - lat1;
        double deltaLon = lon2 - lon1;

        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(deltaLon / 2), 2);

        a = Math.max(0.0, Math.min(1.0, a));

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1 - a)
        );

        return EARTH_RADIUS_KM * c;
    }

    private void validateCoordinates(double latitude, double longitude) {

        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Invalid latitude");
        }

        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid longitude");
        }
    }
}
