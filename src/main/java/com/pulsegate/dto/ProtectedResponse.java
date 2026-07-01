package com.pulsegate.dto;

import java.time.Instant;

public record ProtectedResponse(
        String message,
        String apiKeyPreview,
        int remainingThisMinute,
        Instant resetAt
) {
}
