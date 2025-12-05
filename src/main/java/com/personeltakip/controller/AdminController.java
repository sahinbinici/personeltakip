package com.personeltakip.controller;

import com.personeltakip.model.AttendanceRecord;
import com.personeltakip.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/attendance/all")
    public ResponseEntity<List<AttendanceRecord>> getAllAttendanceRecords() {
        return ResponseEntity.ok(adminService.getAllAttendanceRecords());
    }

    @PutMapping("/attendance/update/{recordId}")
    public ResponseEntity<AttendanceRecord> updateAttendanceRecord(@PathVariable Long recordId, @RequestBody AttendanceRecord updatedRecord) {
        try {
            AttendanceRecord record = adminService.updateAttendanceRecord(recordId, updatedRecord);
            return ResponseEntity.ok(record);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/attendance/delete/{recordId}")
    public ResponseEntity<Void> deleteAttendanceRecord(@PathVariable Long recordId) {
        adminService.deleteAttendanceRecord(recordId);
        return ResponseEntity.noContent().build();
    }
}