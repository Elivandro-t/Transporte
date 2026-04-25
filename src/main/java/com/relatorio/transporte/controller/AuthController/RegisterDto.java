package com.relatorio.transporte.controller.AuthController;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterDto(
    @NotBlank @Size(max = 200) String name,
    @Email @NotBlank String email,
    @NotBlank @Size(min = 8) @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
        message = "Senha deve conter maiúscula, minúscula, número e caractere especial"
    ) String password
) {}

