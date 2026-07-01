package com.pulsegate.controller;

import com.pulsegate.dto.ApiKeyResponse;
import com.pulsegate.dto.CreateApiKeyRequest;
import com.pulsegate.model.ApiKey;
import com.pulsegate.service.ApiKeyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiKeyResponse create(@Valid @RequestBody CreateApiKeyRequest request) {
        ApiKey key = apiKeyService.createKey(request.ownerName());
        return new ApiKeyResponse(key.getId(), key.getApiKey(), key.getOwnerName(), key.getCreatedAt(), key.isActive(), 0, 0, 10);
    }

    @GetMapping
    public List<ApiKeyResponse> list() {
        return apiKeyService.listKeys();
    }
}
