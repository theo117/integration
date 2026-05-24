package com.businesshub.dashboard.web.api;

import com.businesshub.dashboard.service.IntegrationEventService;
import com.businesshub.dashboard.web.response.ApiResponseMapper;
import com.businesshub.dashboard.web.response.IntegrationEventResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/integrations/events")
public class IntegrationEventApiController {

    private final IntegrationEventService integrationEventService;
    private final ApiResponseMapper apiResponseMapper;

    public IntegrationEventApiController(IntegrationEventService integrationEventService,
                                         ApiResponseMapper apiResponseMapper) {
        this.integrationEventService = integrationEventService;
        this.apiResponseMapper = apiResponseMapper;
    }

    @GetMapping
    public List<IntegrationEventResponse> getRecentEvents() {
        return integrationEventService.getRecentEvents()
                .stream()
                .map(apiResponseMapper::toIntegrationEventResponse)
                .toList();
    }
}
