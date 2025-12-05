package com.personeltakip.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_records")
@Data
@NoArgsConstructor
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_number", nullable = false)
    private String employeeNumber;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "check_in_latitude")
    private Double checkInLatitude;

    @Column(name = "check_in_longitude")
    private Double checkInLongitude;

    @Column(name = "check_out_latitude")
    private Double checkOutLatitude;

    @Column(name = "check_out_longitude")
    private Double checkOutLongitude;

    @Column(name = "check_in_ip_address")
    private String checkInIpAddress;

    @Column(name = "check_out_ip_address")
    private String checkOutIpAddress;

    @Column(name = "check_in_location_details")
    private String checkInLocationDetails;

    @Column(name = "check_out_location_details")
    private String checkOutLocationDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AttendanceStatus status; // e.g., CHECKED_IN, CHECKED_OUT
}
