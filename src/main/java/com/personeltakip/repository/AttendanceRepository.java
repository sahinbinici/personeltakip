package com.personeltakip.repository;

import com.personeltakip.model.AttendanceRecord;
import com.personeltakip.model.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByEmployeeNumberOrderByCheckInTimeDesc(String employeeNumber);

    Optional<AttendanceRecord> findFirstByEmployeeNumberAndStatusOrderByCheckInTimeDesc(String employeeNumber, AttendanceStatus status);
}
