package com.personeltakip.service;

import com.personeltakip.model.AttendanceRecord;
import com.personeltakip.model.AttendanceStatus;
import com.personeltakip.model.QRCodeToken;
import com.personeltakip.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private QRCodeService qrCodeService;

    /**
     * Process check-in for an employee using QR token
     * Same token can be used for both check-in and check-out on the same day
     */
    public AttendanceRecord processCheckIn(String employeeNumber, String qrToken, String ipAddress, Double latitude, Double longitude, String locationDetails) {
        logger.info("Processing check-in for employee: {}, QR token: {}", employeeNumber, qrToken);
        
        // Validate QR token
        QRCodeToken token = qrCodeService.getValidToken(qrToken);
        if (token == null) {
            logger.warn("Invalid or expired QR token for employee: {}", employeeNumber);
            throw new RuntimeException("Invalid or expired QR token.");
        }

        // Check if already checked in today
        Optional<AttendanceRecord> lastRecord = attendanceRepository.findFirstByEmployeeNumberAndStatusOrderByCheckInTimeDesc(employeeNumber, AttendanceStatus.CHECKED_IN);
        if (lastRecord.isPresent()) {
            logger.warn("User has already checked in: {}", employeeNumber);
            throw new RuntimeException("User has already checked in.");
        }

        // Check if token was already used for check-in
        if (token.getUsedForCheckIn()) {
            logger.warn("QR token already used for check-in: {}", qrToken);
            throw new RuntimeException("This QR code has already been used for check-in today.");
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setEmployeeNumber(employeeNumber);
        record.setCheckInTime(LocalDateTime.now());
        record.setCheckInIpAddress(ipAddress);
        record.setCheckInLatitude(latitude);
        record.setCheckInLongitude(longitude);
        record.setCheckInLocationDetails(locationDetails);
        record.setStatus(AttendanceStatus.CHECKED_IN);

        AttendanceRecord savedRecord = attendanceRepository.save(record);
        
        // Mark QR token as used for check-in (but not for check-out yet)
        qrCodeService.markTokenAsUsedForCheckIn(qrToken);
        
        logger.info("Check-in successful for employee: {}, record ID: {}", employeeNumber, savedRecord.getId());
        
        return savedRecord;
    }

    /**
     * Process check-out for an employee using QR token
     * Same token can be used for both check-in and check-out on the same day
     */
    public AttendanceRecord processCheckOut(String employeeNumber, String qrToken, String ipAddress, Double latitude, Double longitude, String locationDetails) {
        logger.info("Processing check-out for employee: {}, QR token: {}", employeeNumber, qrToken);
        
        // Validate QR token
        QRCodeToken token = qrCodeService.getValidToken(qrToken);
        if (token == null) {
            logger.warn("Invalid or expired QR token for employee: {}", employeeNumber);
            throw new RuntimeException("Invalid or expired QR token.");
        }

        // Check if token was already used for check-out
        if (token.getUsedForCheckOut()) {
            logger.warn("QR token already used for check-out: {}", qrToken);
            throw new RuntimeException("This QR code has already been used for check-out today.");
        }

        AttendanceRecord lastRecord = attendanceRepository.findFirstByEmployeeNumberAndStatusOrderByCheckInTimeDesc(employeeNumber, AttendanceStatus.CHECKED_IN)
                .orElseThrow(() -> {
                    logger.warn("No active check-in found for employee: {}", employeeNumber);
                    return new RuntimeException("No active check-in found to check out.");
                });

        lastRecord.setCheckOutTime(LocalDateTime.now());
        lastRecord.setCheckOutIpAddress(ipAddress);
        lastRecord.setCheckOutLatitude(latitude);
        lastRecord.setCheckOutLongitude(longitude);
        lastRecord.setCheckOutLocationDetails(locationDetails);
        lastRecord.setStatus(AttendanceStatus.CHECKED_OUT);

        AttendanceRecord savedRecord = attendanceRepository.save(lastRecord);
        
        // Mark QR token as used for check-out (but not for check-in)
        qrCodeService.markTokenAsUsedForCheckOut(qrToken);
        
        logger.info("Check-out successful for employee: {}, record ID: {}", employeeNumber, savedRecord.getId());
        
        return savedRecord;
    }

    /**
     * Process check-in with custom timestamp (for mobile apps)
     */
    public AttendanceRecord processCheckInWithTimestamp(String employeeNumber, String qrToken, String ipAddress, 
                                                       Double latitude, Double longitude, String locationDetails, 
                                                       LocalDateTime timestamp) {
        logger.info("Processing check-in for employee: {}, QR token: {}, timestamp: {}", employeeNumber, qrToken, timestamp);
        
        // Validate QR token
        QRCodeToken token = qrCodeService.getValidToken(qrToken);
        if (token == null) {
            logger.warn("Invalid or expired QR token for employee: {}", employeeNumber);
            throw new RuntimeException("Invalid or expired QR token.");
        }

        // Check if already checked in today
        Optional<AttendanceRecord> lastRecord = attendanceRepository.findFirstByEmployeeNumberAndStatusOrderByCheckInTimeDesc(employeeNumber, AttendanceStatus.CHECKED_IN);
        if (lastRecord.isPresent()) {
            logger.warn("User has already checked in: {}", employeeNumber);
            throw new RuntimeException("User has already checked in.");
        }

        // Check if token was already used for check-in
        if (token.getUsedForCheckIn()) {
            logger.warn("QR token already used for check-in: {}", qrToken);
            throw new RuntimeException("This QR code has already been used for check-in today.");
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setEmployeeNumber(employeeNumber);
        record.setCheckInTime(timestamp); // Use provided timestamp
        record.setCheckInIpAddress(ipAddress);
        record.setCheckInLatitude(latitude);
        record.setCheckInLongitude(longitude);
        record.setCheckInLocationDetails(locationDetails);
        record.setStatus(AttendanceStatus.CHECKED_IN);

        AttendanceRecord savedRecord = attendanceRepository.save(record);
        
        // Mark QR token as used for check-in (but not for check-out yet)
        qrCodeService.markTokenAsUsedForCheckIn(qrToken);
        
        logger.info("Check-in successful for employee: {}, record ID: {}, timestamp: {}", employeeNumber, savedRecord.getId(), timestamp);
        
        return savedRecord;
    }

    /**
     * Process check-out with custom timestamp (for mobile apps)
     */
    public AttendanceRecord processCheckOutWithTimestamp(String employeeNumber, String qrToken, String ipAddress, 
                                                        Double latitude, Double longitude, String locationDetails, 
                                                        LocalDateTime timestamp) {
        logger.info("Processing check-out for employee: {}, QR token: {}, timestamp: {}", employeeNumber, qrToken, timestamp);
        
        // Validate QR token
        QRCodeToken token = qrCodeService.getValidToken(qrToken);
        if (token == null) {
            logger.warn("Invalid or expired QR token for employee: {}", employeeNumber);
            throw new RuntimeException("Invalid or expired QR token.");
        }

        // Check if token was already used for check-out
        if (token.getUsedForCheckOut()) {
            logger.warn("QR token already used for check-out: {}", qrToken);
            throw new RuntimeException("This QR code has already been used for check-out today.");
        }

        AttendanceRecord lastRecord = attendanceRepository.findFirstByEmployeeNumberAndStatusOrderByCheckInTimeDesc(employeeNumber, AttendanceStatus.CHECKED_IN)
                .orElseThrow(() -> {
                    logger.warn("No active check-in found for employee: {}", employeeNumber);
                    return new RuntimeException("No active check-in found to check out.");
                });

        lastRecord.setCheckOutTime(timestamp); // Use provided timestamp
        lastRecord.setCheckOutIpAddress(ipAddress);
        lastRecord.setCheckOutLatitude(latitude);
        lastRecord.setCheckOutLongitude(longitude);
        lastRecord.setCheckOutLocationDetails(locationDetails);
        lastRecord.setStatus(AttendanceStatus.CHECKED_OUT);

        AttendanceRecord savedRecord = attendanceRepository.save(lastRecord);
        
        // Mark QR token as used for check-out (but not for check-in)
        qrCodeService.markTokenAsUsedForCheckOut(qrToken);
        
        logger.info("Check-out successful for employee: {}, record ID: {}, timestamp: {}", employeeNumber, savedRecord.getId(), timestamp);
        
        return savedRecord;
    }

    public List<AttendanceRecord> getAttendanceRecordsByEmployee(String employeeNumber) {
        logger.info("Fetching attendance records for employee: {}", employeeNumber);
        List<AttendanceRecord> records = attendanceRepository.findByEmployeeNumberOrderByCheckInTimeDesc(employeeNumber);
        logger.info("Found {} attendance records for employee: {}", records.size(), employeeNumber);
        return records;
    }
    
    // Getter for QRCodeService
    public QRCodeService getQrCodeService() {
        return qrCodeService;
    }
}
