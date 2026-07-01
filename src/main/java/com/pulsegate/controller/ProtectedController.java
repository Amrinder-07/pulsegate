package com.pulsegate.controller;

import com.pulsegate.dto.ProtectedResponse;
import com.pulsegate.service.ApiKeyService;
import com.pulsegate.service.RateLimitService;
import com.pulsegate.service.RequestLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProtectedController {

    private final ApiKeyService apiKeyService;
    private final RateLimitService rateLimitService;
    private final RequestLogService requestLogService;

    public ProtectedController(
            ApiKeyService apiKeyService,
            RateLimitService rateLimitService,
            RequestLogService requestLogService
    ) {
        this.apiKeyService = apiKeyService;
        this.rateLimitService = rateLimitService;
        this.requestLogService = requestLogService;
    }

    @GetMapping("/protected")
    public ResponseEntity<?> protectedEndpoint(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            HttpServletRequest request
    ) {
        long started = System.nanoTime();

        if (!apiKeyService.isValidActiveKey(apiKey)) {
            long latencyMs = elapsedMs(started);
            requestLogService.log(apiKey, request.getMethod(), request.getRequestURI(), 401, latencyMs, clientIp(request), false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing or invalid X-API-Key header"));
        }

        RateLimitService.RateLimitDecision decision = rateLimitService.consume(apiKey);
        if (!decision.allowed()) {
            long latencyMs = elapsedMs(started);
            requestLogService.log(apiKey, request.getMethod(), request.getRequestURI(), 429, latencyMs, clientIp(request), true);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "error", "Too Many Requests",
                            "limit", decision.limit(),
                            "used", decision.used(),
                            "remaining", decision.remaining(),
                            "resetAt", decision.resetAt()
                    ));
        }

        long latencyMs = elapsedMs(started);
        requestLogService.log(apiKey, request.getMethod(), request.getRequestURI(), 200, latencyMs, clientIp(request), false);
        return ResponseEntity.ok(new ProtectedResponse(
                "Request accepted by PulseGate.",
                RequestLogService.preview(apiKey),
                decision.remaining(),
                decision.resetAt()
        ));
    }

    private static long elapsedMs(long started) {
        return Math.max((System.nanoTime() - started) / 1_000_000, 0);
    }

    private static String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
