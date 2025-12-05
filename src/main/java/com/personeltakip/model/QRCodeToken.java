package com.personeltakip.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "qr_code_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String token;
    
    @Column(nullable = false)
    private String ipAddress;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private Boolean used = false;
    
    @Column(nullable = false)
    private Boolean usedForCheckIn = false;
    
    @Column(nullable = false)
    private Boolean usedForCheckOut = false;
    
    @Column(name = "person_psicno", nullable = false)
    private String personPsicno;

    @Column(name = "person_tckiml")
    private String personTckiml;

    @Column(name = "person_firstname")
    private String personFirstName;

    @Column(name = "person_lastname")
    private String personLastName;
    
    @PrePersist
    protected void onCreate() {
        if (token == null) {
            token = UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        // Token expires in 24 hours
        expiresAt = LocalDateTime.now().plusHours(24);
    }
}
