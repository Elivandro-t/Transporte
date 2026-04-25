package com.relatorio.transporte.controller.AuthController;
import com.relatorio.transporte.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticacao", description = "Login, registro e refresh de tokens JWT")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login com email e senha; retorna access + refresh token")
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @Operation(summary = "Registro de novo usuario (fica em PENDING_APPROVAL ate um ADMIN aprovar)")
    @SecurityRequirements
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto dto) {
        authService.register(dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Renova access token usando o refresh token")
    @SecurityRequirements
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(@Valid @RequestBody RefreshTokenDto dto) {
        return ResponseEntity.ok(authService.refreshToken(dto.refreshToken()));
    }

    @Operation(summary = "Logout: invalida o access token corrente")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader.substring(7));
        return ResponseEntity.noContent().build();
    }
}


