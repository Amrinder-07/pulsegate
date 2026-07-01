package com.pulsegate.dto;

import java.time.Instant;

public record RequestLogResponse(
        Long id,
        String apiKeyPreview,
        String method,
        String path,
        int statusCode,
        long latencyMs,
        String ipAddress,
        Instant timestamp,
        boolean blocked
) {
}
