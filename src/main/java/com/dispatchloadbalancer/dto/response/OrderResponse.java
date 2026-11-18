package com.dispatchloadbalancer.dto.response;

import com.dispatchloadbalancer.enums.Priority;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String orderId;
    private Double latitude;
    private Double longitude;
    private String address;
    private Double packageWeight;
    private Priority priority;
}
