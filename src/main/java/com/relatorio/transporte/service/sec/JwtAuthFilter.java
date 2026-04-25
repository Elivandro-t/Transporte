package com.relatorio.transporte.service.sec;

import com.relatorio.transporte.entity.mysql.User;
import com.relatorio.transporte.repository.mysql.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository; // 🔥 trocar aqui

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String jwt = authHeader.substring(7);
            UUID userId = jwtService.extractUserId(jwt);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

                UserPrincipal userPrincipal = UserPrincipal.from(user);

                if (jwtService.isValid(jwt, userId.toString())) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userPrincipal,
                                    null,
                                    userPrincipal.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            log.warn("Falha ao processar JWT: {}", e.getMessage());
        }

        chain.doFilter(request, response);
    }
}
