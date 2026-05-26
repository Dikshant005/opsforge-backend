package com.opsforge.backend.services;

import com.opsforge.backend.models.BlacklistedToken;
import com.opsforge.backend.repositories.BlacklistedTokenRepository;
import com.opsforge.backend.security.JwtUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class TokenBlacklistService {

    private final BlacklistedTokenRepository tokenRepository;
    private final JwtUtil jwtUtil;

    public TokenBlacklistService(BlacklistedTokenRepository tokenRepository, JwtUtil jwtUtil) {
        this.tokenRepository = tokenRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public void blacklistToken(String token) {
        Date expirationDate = jwtUtil.extractExpiration(token);
        BlacklistedToken blacklistedToken = new BlacklistedToken(
                token, 
                expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        );
        tokenRepository.save(blacklistedToken);
    }

    public boolean isTokenBlacklisted(String token) {
        return tokenRepository.existsByToken(token);
    }

    // Runs every 24 hours to keep the database clean
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
        System.out.println("Scheduled Task: Cleaned up expired blacklisted tokens.");
    }
}

