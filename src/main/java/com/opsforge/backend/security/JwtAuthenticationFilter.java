package com.opsforge.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // verify the tokens
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        //Grab the "Authorization" header out of the HTTP Request
        final String authHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwt = null;

        //Check if the header exists and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Cut off the first 7 characters ("Bearer ") to just get the mathematical token
            jwt = authHeader.substring(7);
            
            try {
                // Use the Machine to read the name on the badge
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // If the token is fake or expired, it will throw an error here.
                System.out.println("Invalid or expired JWT Token.");
            }
        }

        //If we found a username, and the Bouncer hasn't already verified them...
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            //Validate the math on the token
            if (jwtUtil.validateToken(jwt, username)) {
                
                // Extract role and create Authority
                String role = jwtUtil.extractRole(jwt);
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                //token real create an official Spring Security ID Badge
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.singletonList(authority)
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                //Hand the official badge to the Bouncer (SecurityContextHolder)
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        //move to next filter or finally to controller
        filterChain.doFilter(request, response);
    }
}
