package com.dispatchloadbalancer.dto.request;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchRequest {

    private String strategy;

    private List<String> orderIds;

    private List<String> vehicleIds;
}
