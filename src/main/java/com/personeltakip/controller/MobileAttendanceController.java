package com.personeltakip.controller;

import com.personeltakip.automation.repository.PersonRepository;
import com.personeltakip.model.AttendanceRecord;
import com.personeltakip.service.AttendanceService;
import com.personeltakip.model.QRCodeToken;
import com.personeltakip.service.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@Tag(name = "Mobile Attendance", description = "Mobil uygulama için katılım (giriş-çıkış) işlemleri")
public class MobileAttendanceController {

    private static final Logger logger = LoggerFactory.getLogger(MobileAttendanceController.class);

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private PersonRepository personRepository;

    // Request DTO
    static class AttendanceRequest {
        public String employeeNumber;      // Personel numarası (sicil no)
        public String qrToken;            // QR kod token'ı
        public String ipAddress;          // IP adresi (opsiyonel)
        public Double latitude;           // Enlem
        public Double longitude;          // Boylam
        public String locationDetails;    // Lokasyon detayları (adres vb.)
        public String timestamp;          // Tarih/saat bilgisi (ISO 8601 formatında, opsiyonel - gönderilmezse server zamanı kullanılır)
    }

    @Operation(summary = "Mobil katılım kaydı", description = "QR kod ile mobil uygulamadan giriş-çıkış kaydı oluşturur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Katılım kaydı başarıyla oluşturuldu"),
        @ApiResponse(responseCode = "401", description = "Geçersiz token veya kimlik bilgileri"),
        @ApiResponse(responseCode = "400", description = "Geçersiz istek")
    })
    @PostMapping("/attendance")
    public ResponseEntity<?> processAttendance(@RequestBody AttendanceRequest req) {
        logger.info("Processing mobile attendance request for employee: {}", req.employeeNumber);
        
        if (req == null || req.employeeNumber == null || req.qrToken == null) {
            logger.warn("Missing required parameters: employeeNumber or qrToken");
            return ResponseEntity.badRequest().body(Map.of("error", "employeeNumber and qrToken are required"));
        }

        try {
            // Verify person exists in automation DB
            var person = personRepository.findBySicilNo(Integer.parseInt(req.employeeNumber)).orElse(null);
            if (person == null) {
                logger.warn("Person not found in automation DB for employee number: {}", req.employeeNumber);
                return ResponseEntity.status(401).body(Map.of("error", "Person not found"));
            }

            // Basic validation of coordinates
            if (req.latitude != null && (req.latitude < -90 || req.latitude > 90)) {
                logger.warn("Invalid latitude provided: {}", req.latitude);
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid latitude"));
            }
            if (req.longitude != null && (req.longitude < -180 || req.longitude > 180)) {
                logger.warn("Invalid longitude provided: {}", req.longitude);
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid longitude"));
            }

            // Get the QR token to check if it's valid and get person info
            QRCodeToken token = attendanceService.getQrCodeService().getValidToken(req.qrToken);
            if (token == null) {
                logger.warn("Invalid or expired QR token: {}", req.qrToken);
                return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired QR token"));
            }

            // Check if user has already checked in or out today
            var records = attendanceService.getAttendanceRecordsByEmployee(req.employeeNumber);
            
            // Find if there's an open check-in record (checked in but not checked out)
            var openRecord = records.stream()
                .filter(r -> r.getCheckOutTime() == null)
                .findFirst();
            
            // Parse timestamp if provided, otherwise use server time
            LocalDateTime timestamp;
            if (req.timestamp != null && !req.timestamp.trim().isEmpty()) {
                try {
                    // Try ISO 8601 format (e.g., "2024-01-15T10:30:00")
                    timestamp = LocalDateTime.parse(req.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    logger.info("Using provided timestamp: {}", timestamp);
                } catch (Exception e) {
                    logger.warn("Invalid timestamp format: {}, using server time instead", req.timestamp);
                    timestamp = LocalDateTime.now();
                }
            } else {
                timestamp = LocalDateTime.now();
                logger.info("No timestamp provided, using server time: {}", timestamp);
            }
            
            AttendanceRecord result;
            if (openRecord.isEmpty()) {
                // No open record, so this is a check-in
                logger.info("Processing check-in for employee: {} at {}", req.employeeNumber, timestamp);
                result = attendanceService.processCheckInWithTimestamp(
                        req.employeeNumber, 
                        req.qrToken, 
                        req.ipAddress == null ? "" : req.ipAddress,
                        req.latitude == null ? 0.0 : req.latitude,
                        req.longitude == null ? 0.0 : req.longitude,
                        req.locationDetails,
                        timestamp);
            } else {
                // There's an open record, so this is a check-out
                logger.info("Processing check-out for employee: {} at {}", req.employeeNumber, timestamp);
                result = attendanceService.processCheckOutWithTimestamp(
                        req.employeeNumber, 
                        req.qrToken, 
                        req.ipAddress == null ? "" : req.ipAddress,
                        req.latitude == null ? 0.0 : req.latitude,
                        req.longitude == null ? 0.0 : req.longitude,
                        req.locationDetails,
                        timestamp);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", openRecord.isEmpty() ? "Checked in successfully" : "Checked out successfully",
                "record", result
            ));
        } catch (NumberFormatException e) {
            logger.error("Invalid employee number format: {}", req.employeeNumber, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid employee number format"));
        } catch (Exception e) {
            logger.error("Error processing attendance for employee: {}", req.employeeNumber, e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
    
    // Add getter for QRCodeService to make it accessible in the controller
    public QRCodeService getQrCodeService() {
        return attendanceService.getQrCodeService();
    }
}