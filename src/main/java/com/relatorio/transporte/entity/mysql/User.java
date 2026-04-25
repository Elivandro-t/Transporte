package com.relatorio.transporte.entity.mysql;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    @Builder.Default
    private UserRole role = UserRole.USUARIO;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING_APPROVAL;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum UserRole {
        ADMIN, SUPERVISOR, ATENDENTE, USUARIO, FISCAL, GERENTE_PREVENCAO, TI;

        public String toSpringRole() {
            return "ROLE_" + this.name();
        }
    }

    public enum UserStatus {
        PENDING_APPROVAL, ACTIVE, INACTIVE, BLOCKED
    }
}
