package com.relatorio.transporte.controller;


import com.relatorio.transporte.entity.dto.CreateUserDto;
import com.relatorio.transporte.entity.dto.UpdateUserDto;
import com.relatorio.transporte.entity.dto.UserResponseDto;
import com.relatorio.transporte.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Usuarios", description = "CRUD de usuarios (MySQL)")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @Operation(summary = "Lista usuarios ativos (ADMIN ou SUPERVISOR)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public List<UserResponseDto> list() {
        return userService.findAll();
    }

    @Operation(summary = "Lista atendentes/supervisores ativos")
    @GetMapping("/agents")
    public List<UserResponseDto> listAgents() {
        return userService.findAgents();
    }

    @Operation(summary = "Busca usuario por ID")
    @GetMapping("/{id}")
    public UserResponseDto getById(@PathVariable UUID id) {
        return userService.findById(id);
    }

    @Operation(summary = "Cria usuario (somente ADMIN)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDto create(@Valid @RequestBody CreateUserDto dto) {
        return userService.create(dto);
    }

    @Operation(summary = "Atualiza usuario (ADMIN ou SUPERVISOR)")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public UserResponseDto update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserDto dto) {
        return userService.update(id, dto);
    }

    @Operation(summary = "Desativa usuario (somente ADMIN)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivate(@PathVariable UUID id) {
        userService.deactivate(id);
    }
}
