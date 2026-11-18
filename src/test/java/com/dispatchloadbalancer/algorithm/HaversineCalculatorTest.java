package com.dispatchloadbalancer.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HaversineCalculatorTest {

    private HaversineCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new HaversineCalculator();
    }

    @Test
    @DisplayName("Distance between identical coordinates should be zero")
    void testCalculateDistance_SameCoordinates_ReturnsZero() {
        double lat = 12.9716;
        double lon = 77.5946;

        double distance = calculator.calculateDistance(lat, lon, lat, lon);

        assertEquals(0.0, distance, 0.0001, "Distance between identical points must be 0");
    }

    @Test
    @DisplayName("Distance between Bangalore and Chennai should be approximately 290 km")
    void testCalculateDistance_BangaloreToChennai() {
        // Bangalore (MG Road)
        double bangaloreLat = 12.9716;
        double bangaloreLon = 77.5946;

        // Chennai (Anna Salai)
        double chennaiLat = 13.0827;
        double chennaiLon = 80.2707;

        double distance = calculator.calculateDistance(bangaloreLat, bangaloreLon, chennaiLat, chennaiLon);

        // Great circle distance between Bangalore and Chennai is approx 290 km
        assertTrue(distance > 280.0 && distance < 300.0,
                "Distance between Bangalore and Chennai should be ~290 km, got: " + distance);
    }

    @Test
    @DisplayName("Distance between Connaught Place and Karol Bagh should be approximately 10-15 km")
    void testCalculateDistance_DelhiDropPoints() {
        // Connaught Place, Delhi
        double cpLat = 28.6139;
        double cpLon = 77.2090;

        // Karol Bagh, Delhi
        double kbLat = 28.7041;
        double kbLon = 77.1025;

        double distance = calculator.calculateDistance(cpLat, cpLon, kbLat, kbLon);

        assertTrue(distance > 5.0 && distance < 20.0,
                "Distance between CP and Karol Bagh should be ~14 km, got: " + distance);
    }

    @Test
    @DisplayName("Invalid latitudes beyond [-90, 90] should throw IllegalArgumentException")
    void testCalculateDistance_InvalidLatitude_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculateDistance(91.0, 77.0, 12.0, 77.0));

        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculateDistance(-90.1, 77.0, 12.0, 77.0));
    }

    @Test
    @DisplayName("Invalid longitudes beyond [-180, 180] should throw IllegalArgumentException")
    void testCalculateDistance_InvalidLongitude_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculateDistance(12.0, 181.0, 12.0, 77.0));

        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculateDistance(12.0, -180.5, 12.0, 77.0));
    }
}
