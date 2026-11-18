package com.dispatchloadbalancer.controller;

import com.dispatchloadbalancer.dto.request.DispatchRequest;
import com.dispatchloadbalancer.dto.response.ApiResponse;
import com.dispatchloadbalancer.dto.response.DispatchPlanResponse;
import com.dispatchloadbalancer.service.DispatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dispatch/plan")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;

    @GetMapping
    public ResponseEntity<ApiResponse<DispatchPlanResponse>> getDispatchPlan(
            @ModelAttribute DispatchRequest request) {

        return null;

    }
}
