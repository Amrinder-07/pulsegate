package com.pulsegate.dto;

import java.time.Instant;

public record ApiKeyResponse(
        Long id,
        String apiKey,
        String ownerName,
        Instant createdAt,
        boolean active,
        long totalRequests,
        int currentWindowRequests,
        int remainingThisMinute
) {
}
