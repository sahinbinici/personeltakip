package com.personeltakip.controller;

import com.personeltakip.model.AttendanceRecord;
import com.personeltakip.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class AttendanceController {
    
    @Autowired
    private AttendanceService attendanceService;
    
    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn(@RequestParam String employeeNumber,
                                   @RequestParam String qrToken,
                                   @RequestParam String ipAddress,
                                   @RequestParam Double latitude,
                                   @RequestParam Double longitude,
                                   @RequestParam(required = false) String locationDetails) {
        try {
            AttendanceRecord record = attendanceService.processCheckIn(
                    employeeNumber, qrToken, ipAddress, latitude, longitude, locationDetails);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/checkout")
    public ResponseEntity<?> checkOut(@RequestParam String employeeNumber,
                                    @RequestParam String qrToken,
                                    @RequestParam String ipAddress,
                                    @RequestParam Double latitude,
                                    @RequestParam Double longitude,
                                    @RequestParam(required = false) String locationDetails) {
        try {
            AttendanceRecord record = attendanceService.processCheckOut(
                    employeeNumber, qrToken, ipAddress, latitude, longitude, locationDetails);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/records/{employeeNumber}")
    public ResponseEntity<List<AttendanceRecord>> getAttendanceRecords(@PathVariable String employeeNumber) {
        try {
            List<AttendanceRecord> records = attendanceService.getAttendanceRecordsByEmployee(employeeNumber);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}