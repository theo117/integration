package com.businesshub.dashboard.web.api;

import com.businesshub.dashboard.service.ReportingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportingApiController {

    private final ReportingService reportingService;

    public ReportingApiController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping
    public ReportingService.ReportingView getReports() {
        return reportingService.getReportingView();
    }
}
