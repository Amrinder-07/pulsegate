package com.pulsegate.service;

import com.pulsegate.dto.ApiKeyResponse;
import com.pulsegate.model.ApiKey;
import com.pulsegate.repository.ApiKeyRepository;
import com.pulsegate.repository.RequestLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final RequestLogRepository requestLogRepository;
    private final RateLimitService rateLimitService;
    private final SecureRandom secureRandom = new SecureRandom();

    public ApiKeyService(
            ApiKeyRepository apiKeyRepository,
            RequestLogRepository requestLogRepository,
            RateLimitService rateLimitService
    ) {
        this.apiKeyRepository = apiKeyRepository;
        this.requestLogRepository = requestLogRepository;
        this.rateLimitService = rateLimitService;
    }

    @Transactional
    public ApiKey createKey(String ownerName) {
        ApiKey key = new ApiKey();
        key.setOwnerName(ownerName.trim());
        key.setApiKey(generateKey());
        return apiKeyRepository.save(key);
    }

    @Transactional(readOnly = true)
    public List<ApiKeyResponse> listKeys() {
        return apiKeyRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isValidActiveKey(String apiKey) {
        return apiKey != null && apiKeyRepository.findByApiKeyAndActiveTrue(apiKey).isPresent();
    }

    @Transactional
    public void deleteAllKeys() {
        apiKeyRepository.deleteAll();
        rateLimitService.clearAll();
    }

    private ApiKeyResponse toResponse(ApiKey key) {
        RateLimitService.RateLimitStatus status = rateLimitService.inspect(key.getApiKey());
        return new ApiKeyResponse(
                key.getId(),
                key.getApiKey(),
                key.getOwnerName(),
                key.getCreatedAt(),
                key.isActive(),
                requestLogRepository.countByApiKey(key.getApiKey()),
                status.used(),
                status.remaining()
        );
    }

    private String generateKey() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return "pg_live_" + HexFormat.of().formatHex(bytes);
    }
}
