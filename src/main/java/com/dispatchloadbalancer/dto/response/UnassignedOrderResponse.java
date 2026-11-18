package com.dispatchloadbalancer.dto.response;

import com.dispatchloadbalancer.enums.Priority;
import com.dispatchloadbalancer.enums.UnassignedReason;
import lombok.*;

@Getter @Builder @AllArgsConstructor
public class UnassignedOrderResponse {
    private String orderId;
    private String address;
    private Double packageWeight;
    private Priority priority;
    private UnassignedReason reason;
}