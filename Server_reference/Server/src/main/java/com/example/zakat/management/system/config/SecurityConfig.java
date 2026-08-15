package com.example.zakat.management.system.config;

import com.example.zakat.management.system.entities.Role;
import com.example.zakat.management.system.filters.JwtAuthenticationFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth

                        // 1. Public (No login required)
                        .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/refresh").permitAll()
                        .requestMatchers("/api/report/**").permitAll()

                        // 2. Admin Only
                        .requestMatchers("/api/assignments/**").hasRole(Role.ADMIN.name())
                        .requestMatchers("/api/dashboard/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/beneficiaries").hasRole(Role.ADMIN.name())
.requestMatchers(HttpMethod.GET, "/api/beneficiaries/queue").hasAnyRole(Role.ADMIN.name(), Role.DONOR.name())
                        .requestMatchers(HttpMethod.GET, "/api/beneficiaries").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/beneficiaries/{id}").hasAnyRole(Role.ADMIN.name(), Role.BENEFICIARY.name())
                        .requestMatchers(HttpMethod.GET, "/api/receipts").hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/receipts/donor/**").hasAnyRole(Role.ADMIN.name(), Role.DONOR.name())

                        // 3. Donor Only (Admin cannot do these)
                        .requestMatchers(HttpMethod.POST, "/api/donors").hasRole(Role.DONOR.name())
                        .requestMatchers(HttpMethod.POST, "/api/inventory").hasRole(Role.DONOR.name())

                        // 4. Donor + Admin
                        .requestMatchers(HttpMethod.GET, "/api/donors/{id}").hasAnyRole(Role.DONOR.name(), Role.ADMIN.name())
                        .requestMatchers(HttpMethod.POST, "/api/donors/{id}/beneficiaries").hasAnyRole(Role.DONOR.name(), Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/donors/{id}/beneficiaries").hasAnyRole(Role.DONOR.name(), Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/donors/{id}/total-donations").hasAnyRole(Role.DONOR.name(), Role.ADMIN.name())

                        // 5. Beneficiary Only
                        .requestMatchers(HttpMethod.PATCH, "/api/beneficiaries/form").hasRole(Role.BENEFICIARY.name())

                        // 6. Auth - Any Authenticated User
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/auth/logout").authenticated()

                        // 7. Any other request needs authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(c -> {
                    c.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
                    c.accessDeniedHandler((req, res, ex) -> res.setStatus(HttpStatus.FORBIDDEN.value()));
                });

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173")); // Vite default port
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}