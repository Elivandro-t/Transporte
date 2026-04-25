package com.relatorio.transporte.entity.dto;

import com.relatorio.transporte.entity.mysql.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateUserDto(
        @NotBlank @Size(max = 200) String name,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8) String password,
        User.UserRole role,
        UUID groupId
) {
}
