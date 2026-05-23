package com.opsforge.backend.config;

import com.opsforge.backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            //Disable CSRF to stop any corss origin forgery attacks
            // have to explicitly disable as it will still ask the csrf token for POST/PUT/DELETE requests even if we are using JWTs and not cookies for auth
            .csrf(csrf -> csrf.disable())
            //State the Rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", 
                    "/api/auth/login", 
                    "/api/auth/register",
                    "/api/auth/forgot-password",
                    "/api/auth/reset-password",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll() // The front door is unlocked for everyone
                .requestMatchers("/api/users/pending").hasRole("ADMIN")
                .requestMatchers("/api/users/*/approve").hasRole("ADMIN")
                .requestMatchers("/api/users/*/reject").hasRole("ADMIN")
                .requestMatchers("/api/users/*/role").hasRole("ADMIN")
                .requestMatchers("/api/users/*/deactivate").hasRole("ADMIN")
                .requestMatchers("/api/users/*/reactivate").hasRole("ADMIN")
                .requestMatchers("/api/tickets/*/status").hasAnyRole("ADMIN", "QA")
                .requestMatchers("/api/tickets/*/assign").hasAnyRole("ADMIN", "QA")
                .anyRequest().authenticated()// EVERY other door requires a VIP Pass (JWT)
            )
            //No Cookies Allowed
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Add JWT Filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    //The Password Encryptor
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}