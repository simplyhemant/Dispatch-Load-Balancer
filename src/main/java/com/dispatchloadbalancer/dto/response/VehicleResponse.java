package com.dispatchloadbalancer.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {

    private String vehicleId;
    private Double capacity;
    private Double currentLatitude;
    private Double currentLongitude;
    private String currentAddress;
}
