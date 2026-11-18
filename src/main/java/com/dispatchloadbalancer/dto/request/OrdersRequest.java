package com.dispatchloadbalancer.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdersRequest {

    @NotEmpty(message = "Orders list cannot be empty")
    @Valid
    private List<OrderRequest> orders;
}
