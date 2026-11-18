package com.dispatchloadbalancer.service;

import com.dispatchloadbalancer.dto.request.DispatchRequest;
import com.dispatchloadbalancer.dto.response.DispatchPlanResponse;

public interface DispatchService {

    DispatchPlanResponse generateDispatchPlan(DispatchRequest request);
}
