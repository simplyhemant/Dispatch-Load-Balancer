package com.dispatchloadbalancer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Wrapper request payload containing a list of delivery orders")
public class OrdersRequest {

    @Schema(description = "List of delivery orders to ingest or update", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "Orders list cannot be empty")
    @Valid
    private List<OrderRequest> orders;
}
