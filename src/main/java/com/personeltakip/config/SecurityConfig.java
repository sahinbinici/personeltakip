package com.personeltakip.config;

import com.personeltakip.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userService)
                .passwordEncoder(passwordEncoder())
                .and().build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .securityContext(context -> context.securityContextRepository(securityContextRepository()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
            .authorizeHttpRequests(auth -> auth
                // Swagger UI - allow all
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                
                // Static resources - allow all
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                
                // Public pages - allow all
                .requestMatchers("/", "/index.html", "/login.html").permitAll()
                
                // Auth API - allow all
                .requestMatchers("/api/auth/**").permitAll()
                
                // QR Code page - authenticated users only (both ROLE_USER and ROLE_ADMIN)
                .requestMatchers("/qrcode.html", "/qrcode").authenticated()
                
                // QR Code API - authenticated users only
                .requestMatchers("/api/qrcode/generate/**", "/api/qrcode/image/**").authenticated()
                .requestMatchers("/api/qrcode/validate/**").permitAll()
                
                // Mobile API - login is public, other endpoints require authentication
                .requestMatchers("/api/mobile/login").permitAll()
                .requestMatchers("/api/mobile/**").authenticated()
                
                // Admin pages - ROLE_ADMIN only
                .requestMatchers("/employees.html", "/attendance.html").hasRole("ADMIN")
                
                // Admin API - ROLE_ADMIN only
                .requestMatchers("/api/admin/**", "/api/employees/**", "/api/attendance/**").hasRole("ADMIN")
                
                // Deny all other requests
                .anyRequest().denyAll()
            );

        return http.build();
    }
}
