package com.pulsegate.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final int maxRequests;
    private final long windowSeconds;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitService(
            @Value("${pulsegate.rate-limit.max-requests:10}") int maxRequests,
            @Value("${pulsegate.rate-limit.window-seconds:60}") long windowSeconds
    ) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    public synchronized RateLimitDecision consume(String apiKey) {
        Window window = activeWindow(apiKey);
        if (window.used >= maxRequests) {
            return new RateLimitDecision(false, maxRequests, window.used, 0, window.resetAt);
        }

        window.used++;
        return new RateLimitDecision(true, maxRequests, window.used, maxRequests - window.used, window.resetAt);
    }

    public synchronized RateLimitStatus inspect(String apiKey) {
        Window window = activeWindow(apiKey);
        return new RateLimitStatus(maxRequests, window.used, Math.max(maxRequests - window.used, 0), window.resetAt);
    }

    public synchronized void clearAll() {
        windows.clear();
    }

    private Window activeWindow(String apiKey) {
        Instant now = Instant.now();
        Window existing = windows.get(apiKey);
        if (existing == null || !existing.resetAt.isAfter(now)) {
            Window replacement = new Window(0, now.plusSeconds(windowSeconds));
            windows.put(apiKey, replacement);
            return replacement;
        }
        return existing;
    }

    public record RateLimitDecision(boolean allowed, int limit, int used, int remaining, Instant resetAt) {
    }

    public record RateLimitStatus(int limit, int used, int remaining, Instant resetAt) {
    }

    private static final class Window {
        private int used;
        private final Instant resetAt;

        private Window(int used, Instant resetAt) {
            this.used = used;
            this.resetAt = resetAt;
        }
    }
}
