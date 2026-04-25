package com.relatorio.transporte.controller.AuthController;
public record AuthResponseDto(
    String accessToken,
    String refreshToken,
    long expiresIn,
    UserProfileDto user
) {}

