package com.synix.uwait_backend.repository;

import com.synix.uwait_backend.model.Role;
import com.synix.uwait_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findByRole(Role role);

    void deleteByEmail(String ownerEmail);

    boolean existsByEmail(String email);
}

