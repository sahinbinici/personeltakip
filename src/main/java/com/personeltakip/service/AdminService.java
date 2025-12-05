package com.personeltakip.service;

import com.personeltakip.model.AttendanceRecord;
import com.personeltakip.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    public List<AttendanceRecord> getAllAttendanceRecords() {
        return attendanceRepository.findAll();
    }

    public AttendanceRecord updateAttendanceRecord(Long recordId, AttendanceRecord updatedRecord) {
        AttendanceRecord existingRecord = attendanceRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found with id: " + recordId));

        // Update fields
        existingRecord.setCheckInTime(updatedRecord.getCheckInTime());
        existingRecord.setCheckOutTime(updatedRecord.getCheckOutTime());
        existingRecord.setCheckInLatitude(updatedRecord.getCheckInLatitude());
        // ... update other fields as needed

        return attendanceRepository.save(existingRecord);
    }

    public void deleteAttendanceRecord(Long recordId) {
        attendanceRepository.deleteById(recordId);
    }
}
