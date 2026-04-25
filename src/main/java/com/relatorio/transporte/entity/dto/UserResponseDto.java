package com.relatorio.transporte.entity.dto;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDto(
        UUID id, String name, String email,
        String role,
        String avatarUrl, boolean active,
        LocalDateTime lastLoginAt, LocalDateTime createdAt
) {

}
