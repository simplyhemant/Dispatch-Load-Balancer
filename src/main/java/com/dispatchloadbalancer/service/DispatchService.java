package com.dispatchloadbalancer.service;

import com.dispatchloadbalancer.dto.response.DispatchPlanResponse;

public interface DispatchService {

    DispatchPlanResponse getDispatchPlan();

    void reset();
}