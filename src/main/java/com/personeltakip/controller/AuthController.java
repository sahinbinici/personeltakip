package com.personeltakip.controller;

import com.personeltakip.automation.repository.PersonRepository;
import com.personeltakip.dto.LoginRequest;
import com.personeltakip.dto.RegisterRequest;
import com.personeltakip.dto.VerifyRequest;
import com.personeltakip.model.User;
import com.personeltakip.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
@Tag(name = "Authentication", description = "Kullanıcı kimlik doğrulama ve kayıt işlemleri")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;
    
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PersonRepository personRepository;

    @Operation(summary = "Kullanıcı kaydı başlat", description = "TC Kimlik No ve Sicil No ile kullanıcı kaydını başlatır ve SMS kodu gönderir")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "SMS kodu başarıyla gönderildi"),
        @ApiResponse(responseCode = "400", description = "Geçersiz istek veya kullanıcı zaten kayıtlı")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            logger.info("Registration initiated for national ID: {} and employee number: {}", registerRequest.getNationalId(), registerRequest.getEmployeeNumber());
            authService.initiateRegistration(registerRequest.getNationalId(), registerRequest.getEmployeeNumber());
            return ResponseEntity.ok("SMS verification code sent.");
        } catch (RuntimeException e) {
            logger.error("Registration failed for national ID: {} and employee number: {} - Error: {}", registerRequest.getNationalId(), registerRequest.getEmployeeNumber(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "SMS doğrulama ve hesap oluşturma", description = "SMS kodu ile doğrulama yapıp kullanıcı hesabını oluşturur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hesap başarıyla oluşturuldu"),
        @ApiResponse(responseCode = "400", description = "Geçersiz SMS kodu veya hata")
    })
    @PostMapping("/verify-and-create")
    public ResponseEntity<?> verifyAndCreateUser(@RequestBody VerifyRequest verifyRequest) {
        try {
            logger.info("Verification initiated for national ID: {} and employee number: {}", verifyRequest.getNationalId(), verifyRequest.getEmployeeNumber());
            authService.completeRegistration(verifyRequest.getNationalId(), verifyRequest.getEmployeeNumber(), verifyRequest.getSmsCode(), verifyRequest.getPassword());
            return ResponseEntity.ok("User registered successfully.");
        } catch (RuntimeException e) {
            logger.error("Verification failed for national ID: {} and employee number: {} - Error: {}", verifyRequest.getNationalId(), verifyRequest.getEmployeeNumber(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Kullanıcı girişi", description = "TC Kimlik No ve şifre ile kullanıcı girişi yapar")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Giriş başarılı"),
        @ApiResponse(responseCode = "401", description = "Geçersiz kimlik bilgileri")
    })
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            logger.info("Login attempt for national ID: {}", loginRequest.getNationalId());
            
            // Authenticate user with Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getNationalId(), loginRequest.getPassword())
            );
            
            // Create SecurityContext and set authentication
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            
            // Save SecurityContext to session
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
            
            // Store user info in session
            User user = authService.getUserByNationalId(loginRequest.getNationalId());
            session.setAttribute("user", user);
            session.setAttribute("loggedIn", true);
            
            // Set session to be permanent (or at least longer-lived)
            session.setMaxInactiveInterval(30 * 60); // 30 minutes
            
            logger.info("Login successful for national ID: {}, session ID: {}", loginRequest.getNationalId(), session.getId());
            
            return ResponseEntity.ok("Login Successful!");
        } catch (Exception e) {
            logger.error("Login failed for national ID: {} - Error: {}", loginRequest.getNationalId(), e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @Operation(summary = "Kullanıcı çıkışı", description = "Kullanıcı oturumunu sonlandırır")
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpSession session) {
        try {
            // Clear Spring Security context
            SecurityContextHolder.clearContext();
            
            // Invalidate session
            session.invalidate();
            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            logger.error("Logout error: {}", e.getMessage());
            return ResponseEntity.status(500).body("Logout failed");
        }
    }

    @Operation(summary = "Mevcut kullanıcı bilgileri", description = "Oturum açmış kullanıcının bilgilerini döndürür")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Kullanıcı bilgileri başarıyla döndürüldü"),
        @ApiResponse(responseCode = "401", description = "Kullanıcı oturum açmamış")
    })
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        try {
            // First try to get from Spring Security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = null;
            
            if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
                // Get user from session or load from database
                user = (User) session.getAttribute("user");
                if (user == null) {
                    // Load user from database using authentication name (nationalId)
                    user = authService.getUserByNationalId(authentication.getName());
                    if (user != null) {
                        session.setAttribute("user", user);
                    }
                }
            }
            
            // Fallback to session attribute
            if (user == null) {
                user = (User) session.getAttribute("user");
            }
            
            if (user == null) {
                return ResponseEntity.status(401).body("Not authenticated");
            }
            
            // Build response map
            Map<String, Object> response = new HashMap<>();
            response.put("nationalId", user.getTckiml());
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            response.put("employeeNumber", user.getPsicno());
            
            // Try to get person details from automation DB
            try {
                var person = personRepository.findBySicilNoAndTcKimlikNo(
                    user.getPsicno(), 
                    user.getTckiml()
                );
                
                if (person.isPresent()) {
                    var personData = person.get();
                    response.put("firstName", personData.getAdi() != null ? personData.getAdi() : "");
                    response.put("lastName", personData.getSoyadi() != null ? personData.getSoyadi() : "");
                    response.put("fullName", (personData.getAdi() != null ? personData.getAdi() : "") + 
                                         " " + (personData.getSoyadi() != null ? personData.getSoyadi() : "").trim());
                    
                    logger.info("Person details loaded from automation DB for user: {}", user.getTckiml());
                } else {
                    // If not found with both, try with TC kimlik no only
                    var personByTc = personRepository.findByTcKimlikNo(user.getTckiml());
                    if (personByTc.isPresent()) {
                        var personData = personByTc.get();
                        response.put("firstName", personData.getAdi() != null ? personData.getAdi() : "");
                        response.put("lastName", personData.getSoyadi() != null ? personData.getSoyadi() : "");
                        response.put("fullName", (personData.getAdi() != null ? personData.getAdi() : "") + 
                                             " " + (personData.getSoyadi() != null ? personData.getSoyadi() : "").trim());
                        
                        logger.info("Person details loaded from automation DB (by TC only) for user: {}", user.getTckiml());
                    } else {
                        logger.warn("Person not found in automation DB for user: {}, sicilNo: {}", user.getTckiml(), user.getPsicno());
                        response.put("firstName", "");
                        response.put("lastName", "");
                        response.put("fullName", user.getUsername());
                    }
                }
            } catch (Exception e) {
                logger.error("Error loading person details from automation DB: {}", e.getMessage());
                // Continue with basic user info
                response.put("firstName", "");
                response.put("lastName", "");
                response.put("fullName", user.getUsername());
            }
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Failed to get user info - Error: {}", e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}
