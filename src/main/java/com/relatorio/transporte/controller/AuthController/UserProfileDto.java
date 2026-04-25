package com.relatorio.transporte.controller.AuthController;
public record UserProfileDto(
    java.util.UUID id,
    String name,
    String email,
    String role,
    String avatarUrl
) {}
