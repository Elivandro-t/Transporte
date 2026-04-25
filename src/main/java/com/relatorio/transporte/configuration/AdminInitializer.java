package com.relatorio.transporte.configuration;

import com.relatorio.transporte.entity.mysql.User;
import com.relatorio.transporte.repository.mysql.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@transporte.local}")
    private String adminEmail;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.admin.name:Administrador}")
    private String adminName;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin '{}' ja existe, nada a fazer.", adminEmail);
            return;
        }

        User admin = User.builder()
                .name(adminName)
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .role(User.UserRole.ADMIN)
                .status(User.UserStatus.ACTIVE)
                .active(true)
                .build();

        userRepository.save(admin);
        log.info("Admin criado: email='{}' (altere a senha apos o primeiro login)", adminEmail);
    }
}
