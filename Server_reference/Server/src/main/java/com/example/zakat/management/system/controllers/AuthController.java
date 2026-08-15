package com.example.zakat.management.system.controllers;

import com.example.zakat.management.system.config.JwtConfig;
import com.example.zakat.management.system.dtos.request.LoginRequest;
import com.example.zakat.management.system.dtos.response.JwtResponse;
import com.example.zakat.management.system.dtos.response.UserResponse;
import com.example.zakat.management.system.entities.RefreshToken;
import com.example.zakat.management.system.exceptions.ResourceNotFoundException;
import com.example.zakat.management.system.mappers.UserMapper;
import com.example.zakat.management.system.repositories.RefreshTokenRepository;
import com.example.zakat.management.system.repositories.UserRepository;
import com.example.zakat.management.system.services.AuthService;
import com.example.zakat.management.system.services.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final UserMapper userMapper;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request,
                                             HttpServletResponse response) {
        // Throws AuthenticationException if credentials are wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var accessToken = jwtService.getAccessToken(user);
        var refreshToken = jwtService.getRefreshToken(user);

        // Store refresh token in HttpOnly cookie — prevents JS from reading it (XSS protection)
        var cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth");   // only sent to the refresh endpoint
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration());
        // cookie.setSecure(true);     // uncomment in production (HTTPS only)
        response.addCookie(cookie);

        // Persist refresh token in DB so we can revoke it on logout
        var tokenEntity = new RefreshToken();
        tokenEntity.setToken(refreshToken);
        tokenEntity.setIssuedAt(Instant.now());
        tokenEntity.setExpiry(Instant.now().plusSeconds(jwtConfig.getRefreshTokenExpiration()));
        tokenEntity.setRevoked(false);
        tokenEntity.setUserId(user.getId());
        refreshTokenRepository.save(tokenEntity);

        // ---------------------------------------------------------------
        // EMAIL HOOK — send welcome/login notification email here
        // emailService.sendLoginNotification(user.getEmail(), user.getName());
        // ---------------------------------------------------------------

        return ResponseEntity.ok(new JwtResponse(accessToken));
    }

    @GetMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(
            @CookieValue(value = "refreshToken") String refreshToken) {

        var tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElse(null);

        if (tokenEntity == null
                || tokenEntity.getRevoked()
                || !jwtService.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var user = userRepository.findById(jwtService.getUserIdFromToken(refreshToken))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ResponseEntity.ok(new JwtResponse(jwtService.getAccessToken(user)));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(userMapper.toResponse(authService.getLoggedUser()));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = "refreshToken") String refreshToken,
            HttpServletResponse response) {

        // Clear the cookie in the browser
        var cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        // cookie.setSecure(true);     // uncomment in production
        response.addCookie(cookie);

        // Revoke token in DB so it can't be reused even if someone still holds it
        refreshTokenRepository.revokeToken(refreshToken);

        return ResponseEntity.ok().build();
    }
}
