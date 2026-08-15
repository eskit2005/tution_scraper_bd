package com.example.zakat.management.system.services;

import com.example.zakat.management.system.entities.User;
import com.example.zakat.management.system.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;

    // Extracts the currently authenticated user from the SecurityContext.
    // The principal is the userId (Long) set by JwtAuthenticationFilter.
    public User getLoggedUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userId = (Long) authentication.getPrincipal();
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}
