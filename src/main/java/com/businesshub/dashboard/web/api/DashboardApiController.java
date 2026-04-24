package com.businesshub.dashboard.web.api;

import com.businesshub.dashboard.service.DashboardService;
import com.businesshub.dashboard.web.response.ApiResponseMapper;
import com.businesshub.dashboard.web.response.DashboardResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private final DashboardService dashboardService;
    private final ApiResponseMapper apiResponseMapper;

    public DashboardApiController(DashboardService dashboardService, ApiResponseMapper apiResponseMapper) {
        this.dashboardService = dashboardService;
        this.apiResponseMapper = apiResponseMapper;
    }

    @GetMapping
    public DashboardResponse dashboard() {
        return apiResponseMapper.toDashboardResponse(dashboardService.getDashboardView());
    }
}
