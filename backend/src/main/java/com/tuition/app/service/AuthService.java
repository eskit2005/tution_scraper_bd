package com.tuition.app.service;

import com.tuition.app.entity.User;
import com.tuition.app.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public String registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        
        return jwtUtil.generateToken(user.getEmail());
    }

    public String loginUser(String email, String rawPassword) {
        // This delegates password checking and user loading to the DaoAuthenticationProvider
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, rawPassword)
        );
        
        // If authenticate() passes, we generate the token
        return jwtUtil.generateToken(email);
    }
}
