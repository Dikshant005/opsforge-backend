package com.opsforge.backend.config;

import com.opsforge.backend.models.User;
import com.opsforge.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // We pull the username and password directly from application.properties
    @Value("${app.setup.admin-username}")
    private String adminUsername;

    @Value("${app.setup.admin-password}")
    private String adminPassword;

    public DatabaseSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        
        // Check if the database is completely empty
        if (userRepository.count() == 0) {
            System.out.println("Database is empty. Seeding initial Super Admin...");

            User admin = new User();
            admin.setUsername(adminUsername);
            
            // Encrypt the password before saving
            admin.setPassword(passwordEncoder.encode(adminPassword)); 
            
            admin.setRole("ADMIN");
            admin.setAccountStatus("APPROVED");

            userRepository.save(admin);
            System.out.println("Default Admin created successfully!");
        } else {
            System.out.println("Database already contains users. Skipping seed.");
        }
    }
}