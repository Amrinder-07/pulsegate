package com.pulsegate.controller;

import com.pulsegate.dto.DashboardSummaryResponse;
import com.pulsegate.dto.RequestLogResponse;
import com.pulsegate.service.ApiKeyService;
import com.pulsegate.service.RequestLogService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final RequestLogService requestLogService;
    private final ApiKeyService apiKeyService;

    public DashboardController(RequestLogService requestLogService, ApiKeyService apiKeyService) {
        this.requestLogService = requestLogService;
        this.apiKeyService = apiKeyService;
    }

    @GetMapping("/dashboard/summary")
    public DashboardSummaryResponse summary() {
        return requestLogService.summary();
    }

    @GetMapping("/logs")
    public List<RequestLogResponse> logs(@RequestParam(defaultValue = "50") int limit) {
        return requestLogService.recentLogs(limit);
    }

    @DeleteMapping("/demo-data")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetDemoData() {
        requestLogService.deleteAllLogs();
        apiKeyService.deleteAllKeys();
    }
}
