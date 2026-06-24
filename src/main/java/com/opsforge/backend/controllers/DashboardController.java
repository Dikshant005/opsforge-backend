package com.opsforge.backend.controllers;

import com.opsforge.backend.dtos.dashboard.AdminDashboardDTO;
import com.opsforge.backend.dtos.dashboard.DevDashboardDTO;
import com.opsforge.backend.dtos.dashboard.QADashboardDTO;
import com.opsforge.backend.models.User;
import com.opsforge.backend.repositories.UserRepository;
import com.opsforge.backend.services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/dev")
    @PreAuthorize("hasRole('DEV')")
    public ResponseEntity<DevDashboardDTO> getDevDashboard(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(dashboardService.getDevDashboard(user));
    }

    @GetMapping("/qa")
    @PreAuthorize("hasRole('QA')")
    public ResponseEntity<QADashboardDTO> getQADashboard(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(dashboardService.getQADashboard(user));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardDTO> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }
}
