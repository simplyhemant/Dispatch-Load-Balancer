package com.dispatchloadbalancer.dto.response;

import com.dispatchloadbalancer.enums.Priority;
import com.dispatchloadbalancer.enums.UnassignedReason;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Builder @AllArgsConstructor
@Schema(description = "Details of an order that could not be assigned to any vehicle")
public class UnassignedOrderResponse {

    @Schema(description = "Order identifier", example = "ORD030")
    private String orderId;

    @Schema(description = "Delivery address", example = "Karol Bagh, Delhi, India")
    private String address;

    @Schema(description = "Package weight in kilograms", example = "20.0")
    private Double packageWeight;

    @Schema(description = "Priority level of the order", example = "LOW")
    private Priority priority;

    @Schema(description = "Reason why this order could not be accommodated", example = "INSUFFICIENT_REMAINING_CAPACITY")
    private UnassignedReason reason;
}