package com.opsforge.backend.services;

import com.opsforge.backend.dtos.RegisterUserDTO;
import com.opsforge.backend.models.AuditLog;
import com.opsforge.backend.models.User;
import com.opsforge.backend.repositories.AuditLogRepository;
import com.opsforge.backend.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogRepository auditLogRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public User registerUser(RegisterUserDTO registerUserDTO) {
        if (userRepository.findByUsername(registerUserDTO.username()).isPresent()) {
            throw new RuntimeException("Username is already taken!");
        }

        User newUser = new User();
        newUser.setUsername(registerUserDTO.username());
        newUser.setPassword(passwordEncoder.encode(registerUserDTO.password()));
        
        if (userRepository.count() == 0) {
            newUser.setRole("ADMIN");
            newUser.setAccountStatus("APPROVED");
        } else {
            String requestedRole = registerUserDTO.requestedRole().toUpperCase();
            if (!requestedRole.equals("DEV") && !requestedRole.equals("QA")) {
                throw new RuntimeException("Invalid requested role! You must request either DEV or QA.");
            }
            newUser.setRole(requestedRole);
            newUser.setAccountStatus("PENDING");
        }

        User savedUser = userRepository.save(newUser);
        logAction("USER", savedUser.getId(), "REGISTER", "User registered as " + savedUser.getRole(), savedUser);
        return savedUser;
    }

    // Fetch a user, or throw error if they don't exist
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    // Fetch every user in the system
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Admin Governance
    @Transactional
    public List<User> getPendingUsers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getAccountStatus().equals("PENDING"))
                .toList();
    }

    @Transactional
    public void approveUser(String username, User performedBy) {
        User user = getUserByUsername(username);
        user.setAccountStatus("APPROVED");
        userRepository.save(user);
        logAction("USER", user.getId(), "APPROVE", "User approved by admin", performedBy);
    }

    @Transactional
    public void rejectUser(String username, User performedBy) {
        User user = getUserByUsername(username);
        user.setAccountStatus("REJECTED");
        userRepository.save(user);
        logAction("USER", user.getId(), "REJECT", "User rejected by admin", performedBy);
    }

    @Transactional
    public void deactivateUser(String username, User performedBy) {
        User user = getUserByUsername(username);
        user.setActive(false);
        userRepository.save(user);
        logAction("USER", user.getId(), "DEACTIVATE", "User deactivated", performedBy);
    }

    @Transactional
    public void reactivateUser(String username, User performedBy) {
        User user = getUserByUsername(username);
        user.setActive(true);
        userRepository.save(user);
        logAction("USER", user.getId(), "REACTIVATE", "User reactivated", performedBy);
    }

    @Transactional
    public User updateUserRole(String username, String newRole, User performedBy) {
        User user = getUserByUsername(username);
        String oldRole = user.getRole();
        String roleToSet = newRole.toUpperCase();
        
        if (!roleToSet.equals("DEV") && !roleToSet.equals("QA")) {
            throw new RuntimeException("Invalid role! Only DEV or QA can be assigned. ADMIN role cannot be assigned via this API.");
        }

        user.setRole(roleToSet);
        User updatedUser = userRepository.save(user);
        logAction("USER", user.getId(), "ROLE_CHANGE", "Changed role from " + oldRole + " to " + roleToSet, performedBy);
        return updatedUser;
    }

    public List<User> getAssignableDevelopers() {
        return userRepository.findByRoleAndAccountStatusAndIsActiveTrue("DEV", "APPROVED");
    }

    private void logAction(String entity, Long entityId, String action, String details, User performedBy) {
        AuditLog log = new AuditLog();
        log.setEntityName(entity);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setDetails(details);
        log.setPerformedBy(performedBy);
        auditLogRepository.save(log);
    }

    public List<AuditLog> getUserHistory(Long userId) {
        return auditLogRepository.findByEntityNameAndEntityIdOrderByTimestampDesc("USER", userId);
    }
}