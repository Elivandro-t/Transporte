package com.relatorio.transporte.service;


import com.relatorio.transporte.entity.mysql.User;
import com.relatorio.transporte.entity.dto.CreateUserDto;
import com.relatorio.transporte.entity.dto.UpdateUserDto;
import com.relatorio.transporte.entity.dto.UserResponseDto;
import com.relatorio.transporte.exception.BusinessException;
import com.relatorio.transporte.exception.ResourceNotFoundException;
import com.relatorio.transporte.repository.mysql.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Cacheable("users")
    @Transactional(readOnly = true)
    public List<UserResponseDto> findAll() {
        return userRepository.findByActiveTrue().stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public UserResponseDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @CacheEvict(value = "users", allEntries = true)
    public UserResponseDto create(CreateUserDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Email já cadastrado");
        }

        User user = User.builder()
            .name(dto.name())
            .email(dto.email())
            .passwordHash(passwordEncoder.encode(dto.password()))
            .role(dto.role())

            .status(User.UserStatus.PENDING_APPROVAL)
                .active(false)
            .build();
   var save = userRepository.save(user);
        if(!save.isActive() && save.getStatus()== User.UserStatus.PENDING_APPROVAL){
           throw new RuntimeException("Aguardando provação de um administrador");
        }

        return toDto(save);
    }

    @CacheEvict(value = "users", allEntries = true)
    public UserResponseDto update(UUID id, UpdateUserDto dto) {
        User user = findOrThrow(id);

        if (dto.name()    != null) user.setName(dto.name());
        if (dto.role()    != null) user.setRole(dto.role());
        if (dto.active()  != null) user.setActive(dto.active());
        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.password()));
        }

        return toDto(userRepository.save(user));
    }

    @CacheEvict(value = "users", allEntries = true)
    public void deactivate(UUID id) {
        User user = findOrThrow(id);
        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> findAgents() {
        return userRepository.findAllAgents().stream()
            .map(this::toDto)
            .toList();
    }

    // ── Approval flow ────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UserResponseDto> findPendingUsers() {
        return userRepository.findByStatus(User.UserStatus.PENDING_APPROVAL).stream()
            .map(this::toDto)
            .toList();
    }

    @CacheEvict(value = "users", allEntries = true)
    public UserResponseDto approveUser(UUID id) {
        User user = findOrThrow(id);
        if (user.getStatus() != User.UserStatus.PENDING_APPROVAL) {
            throw new BusinessException("Usuário não está pendente de aprovação");
        }
        user.setStatus(User.UserStatus.ACTIVE);
        User saved = userRepository.save(user);
        return toDto(saved);
    }

    @CacheEvict(value = "users", allEntries = true)
    public UserResponseDto rejectUser(UUID id) {
        User user = findOrThrow(id);
        user.setStatus(User.UserStatus.INACTIVE);
        user.setActive(false);
        return toDto(userRepository.save(user));
    }

    // ── Private ──────────────────────────────────────────

    private User findOrThrow(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }

    private UserResponseDto toDto(User u) {
        return new UserResponseDto(
            u.getId(), u.getName(), u.getEmail(),
            u.getRole().name(),
            u.getAvatarUrl(), u.isActive(),
            u.getLastLoginAt(), u.getCreatedAt()
        );
    }
}
