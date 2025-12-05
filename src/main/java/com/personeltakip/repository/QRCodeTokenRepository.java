package com.personeltakip.repository;

import com.personeltakip.model.QRCodeToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QRCodeTokenRepository extends JpaRepository<QRCodeToken, Long> {
    Optional<QRCodeToken> findByToken(String token);
    Optional<QRCodeToken> findByTokenAndUsedFalse(String token);
    
    // Find tokens by person and date
    @Query("SELECT q FROM QRCodeToken q WHERE q.personPsicno = ?1 AND DATE(q.createdAt) = ?2")
    List<QRCodeToken> findByPersonPsicnoAndDate(String personPsicno, LocalDate date);
    
    // Find today's unused tokens for a person
    @Query("SELECT q FROM QRCodeToken q WHERE q.personPsicno = ?1 AND DATE(q.createdAt) = ?2 AND q.used = false")
    List<QRCodeToken> findUnusedByPersonPsicnoAndDate(String personPsicno, LocalDate date);
}