package com.opsforge.backend.repositories;

import com.opsforge.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Custom method to find a user by their username for login/security
    Optional<User> findByUsername(String username);

    List<User> findByRoleAndAccountStatusAndIsActiveTrue(String role, String accountStatus);
    
    long countByAccountStatus(String accountStatus);
    long countByIsActiveTrue();
}