package com.relatorio.transporte.service;


import com.relatorio.transporte.controller.AuthController.AuthResponseDto;
import com.relatorio.transporte.controller.AuthController.LoginDto;
import com.relatorio.transporte.controller.AuthController.RegisterDto;
import com.relatorio.transporte.controller.AuthController.UserProfileDto;
import com.relatorio.transporte.entity.mysql.User;
import com.relatorio.transporte.exception.BusinessException;
import com.relatorio.transporte.repository.mysql.UserRepository;
import com.relatorio.transporte.service.sec.JwtService;
import com.relatorio.transporte.service.sec.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder      passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    // ── LOGIN ─────────────────────────────────────────────

    public AuthResponseDto login(LoginDto dto) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        if(principal.getStatus() == User.UserStatus.PENDING_APPROVAL){
            throw new BusinessException("Aguardando Aprovação de um ADMINISTRADOR");
        }

        // Atualiza last_login_at
        userRepository.findById(principal.getId()).ifPresent(u -> {
            u.setLastLoginAt(LocalDateTime.now());
            userRepository.save(u);
        });

        return buildAuthResponse(principal);
    }

    // ── REGISTER ──────────────────────────────────────────

    public void register(RegisterDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Email já cadastrado: " + dto.email());
        }

        User user = User.builder()
            .name(dto.name())
            .email(dto.email())
            .passwordHash(passwordEncoder.encode(dto.password()))
            .role(User.UserRole.USUARIO)
            .status(User.UserStatus.PENDING_APPROVAL)
            .build();
        User saved = userRepository.save(user);
//        if(!saved.isActive() && saved.getStatus() == User.UserStatus.PENDING_APPROVAL){
//            throw new RuntimeException("Aguardando Aprovação de um ADMINISTRADOR");
//        }

    }

    // ── REFRESH TOKEN ─────────────────────────────────────

    public AuthResponseDto refreshToken(String refreshToken) {

        if (jwtService.isExpired(refreshToken)) {
            throw new BusinessException("Refresh token expirado");
        }

        var userId = jwtService.extractUserId(refreshToken);
        User user  = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        if (!user.isActive()) {
            throw new DisabledException("Usuário inativo");
        }

        // Revoga o refresh token antigo (rotação)
        blacklist(refreshToken);
        return buildAuthResponse(UserPrincipal.from(user));
    }

    // ── LOGOUT ────────────────────────────────────────────

    public void logout(String accessToken) {
        if (accessToken != null && !accessToken.isBlank()) {
            blacklist(accessToken);
        }
    }

    // ── PRIVATE ───────────────────────────────────────────

    private AuthResponseDto buildAuthResponse(UserPrincipal principal) {
        String access  = jwtService.generateAccessToken(principal);
        String refresh = jwtService.generateRefreshToken(principal);

        User user = userRepository.findById(principal.getId()).orElseThrow();

        return new AuthResponseDto(
            access, refresh,
            3_600_000L,
            new UserProfileDto(
                user.getId(), user.getName(), user.getEmail(),
                user.getRole().name(),
                user.getAvatarUrl()
            )
        );
    }

    private void blacklist(String token) {
        try {
            long exp = jwtService.extractClaim(token,
                claims -> claims.getExpiration().getTime() - System.currentTimeMillis());

        } catch (Exception e) {
            log.warn("Erro ao invalidar token: {}", e.getMessage());
        }
    }

}
