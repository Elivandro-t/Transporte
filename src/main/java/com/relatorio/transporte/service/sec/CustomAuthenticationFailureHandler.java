package com.relatorio.transporte.service.sec;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        String message;

        if (exception instanceof DisabledException) {
            message = "Usuário aguardando aprovação";
        } else if (exception instanceof LockedException) {
            message = "Usuário bloqueado";
        } else if (exception instanceof BadCredentialsException) {
            message = "Email ou senha inválidos";
        } else {
            message = "Falha de autenticação";
        }

        response.getWriter().write("{\"status\":401,\"message\":\"" + message + "\"}");
    }
}