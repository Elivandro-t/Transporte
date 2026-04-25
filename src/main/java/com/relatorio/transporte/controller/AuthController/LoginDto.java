package com.relatorio.transporte.controller.AuthController;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginDto(
    @Email @NotBlank String email,
    @NotBlank @Size(min = 6) String password
) {}

