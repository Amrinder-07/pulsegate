package com.pulsegate.dto;

public record DashboardSummaryResponse(
        long totalRequests,
        long successfulRequests,
        long failedRequests,
        long blockedRequests,
        double averageLatencyMs,
        long activeApiKeys
) {
}
