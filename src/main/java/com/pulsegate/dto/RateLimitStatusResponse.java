package com.pulsegate.dto;

import java.time.Instant;

public record RateLimitStatusResponse(
        int limit,
        int used,
        int remaining,
        Instant resetAt
) {
}
