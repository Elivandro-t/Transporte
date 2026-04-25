package com.relatorio.transporte.repository.mysql;

import com.relatorio.transporte.entity.mysql.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByActiveTrue();

    @Query("SELECT u FROM User u WHERE u.active = true AND u.role IN ('ATENDENTE','SUPERVISOR')")
    List<User> findAllAgents();

    List<User> findByStatus(User.UserStatus status);
}
