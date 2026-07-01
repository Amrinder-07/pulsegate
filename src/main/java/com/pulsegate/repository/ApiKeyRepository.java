package com.pulsegate.repository;

import com.pulsegate.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByApiKeyAndActiveTrue(String apiKey);

    long countByActiveTrue();
}
