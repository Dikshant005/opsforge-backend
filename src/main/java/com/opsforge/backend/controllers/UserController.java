package com.opsforge.backend.controllers;

import com.opsforge.backend.models.AuditLog;
import com.opsforge.backend.models.User;
import com.opsforge.backend.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // in prod replace * with frontend url
@Tag(name = "Users", description = "Endpoints for user management")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET http://localhost:8080/api/users/me
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the profile of the currently logged-in user")
    public ResponseEntity<User> getCurrentUser(Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        return ResponseEntity.ok(user);
    }

    //GET http://localhost:8080/api/users
    @GetMapping
    @Operation(summary = "Get all users", description = "Returns a list of all registered users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/developers")
    @Operation(summary = "Get active developers", description = "Returns a list of approved and active users with the DEV role")
    public List<User> getDevelopers() {
        return userService.getAssignableDevelopers();
    }

    //GET http://localhost:8080/api/users/{username}
    @GetMapping("/{username}")
    @Operation(summary = "Get user by username", description = "Returns profile data for a specific user")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    //PUT http://localhost:8080/api/users/{username}/role
    @PutMapping("/{username}/role")
    @Operation(summary = "Update user role", description = "Allows an Admin to change a user's role to DEV or QA")
    public ResponseEntity<Map<String, String>> updateRole(@PathVariable String username, @RequestBody Map<String, String> payload, Principal principal) {
        String newRole = payload.get("role");
        User admin = userService.getUserByUsername(principal.getName());
        try {
            userService.updateUserRole(username, newRole, admin);
            Map<String, String> response = Map.of("message", "Role updated to " + newRole + " successfully!");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // GET http://localhost:8080/api/users/pending
    @GetMapping("/pending")
    @Operation(summary = "Get pending users", description = "Returns a list of users awaiting admin approval")
    public List<User> getPendingUsers() {
        return userService.getPendingUsers();
    }

    // PUT http://localhost:8080/api/users/{username}/approve
    @PutMapping("/{username}/approve")
    @Operation(summary = "Approve user", description = "Approves a pending registration request")
    public ResponseEntity<Map<String, String>> approveUser(@PathVariable String username, Principal principal) {
        User admin = userService.getUserByUsername(principal.getName());
        userService.approveUser(username, admin);
        return ResponseEntity.ok(Map.of("message", "User " + username + " approved successfully!"));
    }

    // PUT http://localhost:8080/api/users/{username}/reject
    @PutMapping("/{username}/reject")
    @Operation(summary = "Reject user", description = "Rejects a pending registration request")
    public ResponseEntity<Map<String, String>> rejectUser(@PathVariable String username, Principal principal) {
        User admin = userService.getUserByUsername(principal.getName());
        userService.rejectUser(username, admin);
        return ResponseEntity.ok(Map.of("message", "User " + username + " rejected successfully!"));
    }

    @PutMapping("/{username}/deactivate")
    @Operation(summary = "Deactivate user", description = "Deactivates a user account")
    public ResponseEntity<Map<String, String>> deactivateUser(@PathVariable String username, Principal principal) {
        User admin = userService.getUserByUsername(principal.getName());
        userService.deactivateUser(username, admin);
        return ResponseEntity.ok(Map.of("message", "User deactivated successfully"));
    }

    @PutMapping("/{username}/reactivate")
    @Operation(summary = "Reactivate user", description = "Reactivates a user account")
    public ResponseEntity<Map<String, String>> reactivateUser(@PathVariable String username, Principal principal) {
        User admin = userService.getUserByUsername(principal.getName());
        userService.reactivateUser(username, admin);
        return ResponseEntity.ok(Map.of("message", "User reactivated successfully"));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get user history", description = "Returns the audit log for a specific user")
    public List<AuditLog> getUserHistory(@PathVariable Long id) {
        return userService.getUserHistory(id);
    }
}