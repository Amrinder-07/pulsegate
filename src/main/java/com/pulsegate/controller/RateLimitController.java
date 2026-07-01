package com.pulsegate.controller;

import com.pulsegate.dto.RateLimitStatusResponse;
import com.pulsegate.service.RateLimitService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rate-limit")
public class RateLimitController {

    private final RateLimitService rateLimitService;

    public RateLimitController(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @GetMapping("/{apiKey}/status")
    public RateLimitStatusResponse status(@PathVariable String apiKey) {
        RateLimitService.RateLimitStatus status = rateLimitService.inspect(apiKey);
        return new RateLimitStatusResponse(status.limit(), status.used(), status.remaining(), status.resetAt());
    }
}
