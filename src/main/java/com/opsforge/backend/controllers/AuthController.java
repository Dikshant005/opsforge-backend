package com.opsforge.backend.controllers;

import com.opsforge.backend.dtos.RegisterUserDTO;
import com.opsforge.backend.security.JwtUtil;
import com.opsforge.backend.services.PasswordResetService;
import com.opsforge.backend.services.TokenBlacklistService;
import com.opsforge.backend.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // dev only, in prod replace * with frontend url
@Tag(name = "Authentication", description = "Endpoints for user login and registration")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(AuthenticationManager authenticationManager, 
                          JwtUtil jwtUtil, 
                          UserService userService, 
                          PasswordResetService passwordResetService,
                          TokenBlacklistService tokenBlacklistService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.passwordResetService = passwordResetService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user and returns a JWT token")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> payload) {

        String username = payload.get("username");
        String password = payload.get("password");

        Map<String, String> response = new HashMap<>();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Fetch the user to get their role and check status
            com.opsforge.backend.models.User user = userService.getUserByUsername(username);

            // Block login based on account status
            if (!user.isActive()) {
                response.put("error", "Your account has been deactivated. Please contact an admin.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            if (user.getAccountStatus().equals("PENDING")) {
                response.put("error", "Your account is pending admin approval.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            } else if (user.getAccountStatus().equals("REJECTED")) {
                response.put("error", "Your registration request was rejected.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = jwtUtil.generateToken(username, user.getRole());

            response.put("token", token);
            response.put("message", "Login successful!");
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            response.put("error", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Creates a new user account")
    public ResponseEntity<Map<String, String>> register(@jakarta.validation.Valid @RequestBody RegisterUserDTO registerUserDTO) {
        Map<String, String> response = new HashMap<>();
        try {
            userService.registerUser(registerUserDTO);
            response.put("message", "User registered successfully!");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Generates a reset token and sends a link to the user")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        Map<String, String> response = new HashMap<>();
        try {
            passwordResetService.createResetToken(username);
            response.put("message", "If the username exists, a reset link has been sent.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // We return OK even if user not found to prevent username enumeration
            response.put("message", "If the username exists, a reset link has been sent.");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Resets the user's password using a valid token")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");
        Map<String, String> response = new HashMap<>();
        try {
            passwordResetService.resetPassword(token, newPassword);
            response.put("message", "Password has been reset successfully!");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidates the current JWT token")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader) {
        Map<String, String> response = new HashMap<>();
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(token);
            response.put("message", "Logged out successfully. Token invalidated.");
            return ResponseEntity.ok(response);
        }
        
        response.put("error", "Invalid authorization header.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}



