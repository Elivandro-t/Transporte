package com.relatorio.transporte.controller.AuthController;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenDto(@NotBlank String refreshToken) {}

