package com.personeltakip.controller;

import com.personeltakip.model.Employee;
import com.personeltakip.service.QRCodeService;
import com.personeltakip.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.zxing.WriterException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/qrcode")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
@Tag(name = "QR Code", description = "QR kod oluşturma ve doğrulama işlemleri")
public class QRCodeController {
    private static final Logger logger = LoggerFactory.getLogger(QRCodeController.class);
    
    @Autowired
    private QRCodeService qrCodeService;
    
    @Autowired
    private com.personeltakip.automation.repository.PersonRepository personRepository;
    
    @Operation(summary = "QR kod token oluştur", description = "Belirtilen personel numarası için QR kod token'ı oluşturur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "QR kod token başarıyla oluşturuldu"),
        @ApiResponse(responseCode = "500", description = "Sunucu hatası")
    })
    @GetMapping("/generate/{employeeNumber}")
    public ResponseEntity<String> generateQRCode(@PathVariable String employeeNumber) {
        try {
            // Get client IP address
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String clientIpAddress = getClientIpAddress(request);
            
            logger.info("Generating QR token for psicno={}, ip={}", employeeNumber, clientIpAddress);
            var person = personRepository.findBySicilNo(Integer.parseInt(employeeNumber))
                .orElseThrow(() -> new RuntimeException("Person not found in automation DB"));

            // Generate QR token for person
            String token = qrCodeService.generateQRTokenForPerson(
                person.getSicilNo().toString(), person.getTcKimlikNo().toString(), person.getAdi(), person.getSoyadi(), clientIpAddress
            );
            logger.info("QR token generated for psicno={}, token={}", employeeNumber, token);
            
            // Return the token
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating QR code: " + e.getMessage());
        }
    }
    
    @Operation(summary = "QR kod görüntüsü al", description = "Token'a göre QR kod görüntüsünü PNG formatında döndürür")
    @GetMapping("/image/{token}")
    public ResponseEntity<byte[]> getQRCodeImage(@PathVariable String token) {
        try {
            // Create QR code content (token)
            String qrContent = token;
            
            // Generate QR code image
            byte[] qrCodeImage = qrCodeService.generateQRCodeImage(qrContent, 200, 200);
            
            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(qrCodeImage.length);
            
            return new ResponseEntity<>(qrCodeImage, headers, HttpStatus.OK);
        } catch (WriterException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @Operation(summary = "QR kod token doğrula", description = "QR kod token'ının geçerli olup olmadığını kontrol eder")
    @GetMapping("/validate/{token}")
    public ResponseEntity<Boolean> validateQRToken(@PathVariable String token) {
        boolean isValid = qrCodeService.isValidToken(token);
        return ResponseEntity.ok(isValid);
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            // If there are multiple IPs, get the first one
            return xForwardedForHeader.split(",")[0].trim();
        }
    }
}