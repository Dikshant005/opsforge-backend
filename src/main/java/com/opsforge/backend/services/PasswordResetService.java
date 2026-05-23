package com.opsforge.backend.services;

import com.opsforge.backend.models.PasswordResetToken;
import com.opsforge.backend.models.User;
import com.opsforge.backend.repositories.PasswordResetTokenRepository;
import com.opsforge.backend.repositories.UserRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository, 
                                UserRepository userRepository, 
                                PasswordEncoder passwordEncoder,
                                JavaMailSender mailSender) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @Transactional
    public void createResetToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        // Clean up any existing tokens for this user
        tokenRepository.deleteByUser(user);

        // Generate a unique token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user, 30); // 30 minutes expiry

        tokenRepository.save(resetToken);

        // Send Email
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        sendEmail(user.getUsername(), "Password Reset Request", 
                "Click the link below to reset your password. It expires in 30 minutes:\n\n" + resetLink);
        
        System.out.println("Email sent to: " + username);
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to); // Assuming username is the email address as per your setup
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or non-existent password reset token."));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Password reset token has expired.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Token used, now destroy it
        tokenRepository.delete(resetToken);
    }
}


