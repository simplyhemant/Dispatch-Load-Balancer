package com.dispatchloadbalancer.dto.response;

import com.dispatchloadbalancer.enums.Priority;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignedOrderResponse {

    private String orderId;
    private Double latitude;
    private Double longitude;
    private String address;
    private Double packageWeight;
    private Priority priority;
    private Integer stopSequence;
    private String distanceFromPrevious;
}
