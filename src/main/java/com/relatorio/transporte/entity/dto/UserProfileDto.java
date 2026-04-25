package com.relatorio.transporte.entity.dto;

import java.util.UUID;
public record UserProfileDto(
        UUID id, String name, String email,
        String role, String groupName, String avatarUrl
) {
}
