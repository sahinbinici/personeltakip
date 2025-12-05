package com.personeltakip.controller;

import com.personeltakip.automation.repository.PersonRepository;
import com.personeltakip.model.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@Tag(name = "Mobile Authentication", description = "Mobil uygulama için kimlik doğrulama işlemleri")
public class MobileAuthController {

    private static final Logger logger = LoggerFactory.getLogger(MobileAuthController.class);

    @Autowired
    private com.personeltakip.automation.repository.PersonRepository personRepository;

    public static class LoginRequest {
        public String psicno; // Sicil Numarası
        public String tckiml;  // TC Kimlik No
    }

    @Operation(summary = "Mobil uygulama girişi", description = "TC Kimlik No ve Sicil No ile mobil uygulama kullanıcı girişi")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Giriş başarılı"),
        @ApiResponse(responseCode = "401", description = "Geçersiz kimlik bilgileri"),
        @ApiResponse(responseCode = "400", description = "Geçersiz istek")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            logger.info("Mobile login attempt - psicno: {}, tckiml: {}", req != null ? req.psicno : "null", req != null ? req.tckiml : "null");
            
            if (req == null || req.psicno == null || req.tckiml == null || req.psicno.trim().isEmpty() || req.tckiml.trim().isEmpty()) {
                logger.warn("Mobile login failed: Missing required parameters");
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "error", "psicno and tckiml are required"
                ));
            }

            // Parse and validate input
            Integer sicilNo;
            Long tcKimlikNo;
            try {
                sicilNo = Integer.parseInt(req.psicno.trim());
                tcKimlikNo = Long.parseLong(req.tckiml.trim());
            } catch (NumberFormatException e) {
                logger.warn("Mobile login failed: Invalid number format - psicno: {}, tckiml: {}", req.psicno, req.tckiml);
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "error", "Invalid format for psicno or tckiml. Must be numeric."
                ));
            }

            // Verify person exists in automation DB with both TC Kimlik No and Sicil Numarası
            var person = personRepository.findBySicilNoAndTcKimlikNo(sicilNo, tcKimlikNo).orElse(null);
            if (person == null) {
                logger.warn("Mobile login failed: Person not found - psicno: {}, tckiml: {}", sicilNo, tcKimlikNo);
                return ResponseEntity.status(401).body(Map.of(
                    "status", "error",
                    "error", "Invalid credentials. Person not found with provided TC Kimlik No and Sicil Numarası."
                ));
            }

            logger.info("Mobile login successful - psicno: {}, name: {} {}", sicilNo, person.getAdi(), person.getSoyadi());

            // Successful authentication; return complete person info
            return ResponseEntity.ok(Map.of(
                    "status", "ok",
                    "psicno", person.getSicilNo(),
                    "tckiml", person.getTcKimlikNo(),
                    "firstName", person.getAdi() != null ? person.getAdi() : "",
                    "lastName", person.getSoyadi() != null ? person.getSoyadi() : ""
            ));
        } catch (Exception e) {
            logger.error("Mobile login error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "error", "Internal server error: " + e.getMessage()
            ));
        }
    }
}