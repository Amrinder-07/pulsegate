package com.pulsegate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateApiKeyRequest(
        @NotBlank(message = "ownerName is required")
        @Size(max = 120, message = "ownerName must be 120 characters or fewer")
        String ownerName
) {
}
