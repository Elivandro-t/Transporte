package com.relatorio.transporte.entity.dto;

import com.relatorio.transporte.entity.mysql.User;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateUserDto(
        @Size(max = 200) String name,
        User.UserRole role,
        UUID groupId,
        Boolean active,
        String password
) {
}
