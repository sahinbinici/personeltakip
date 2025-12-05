package com.personeltakip.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.personeltakip.model.Employee;
import com.personeltakip.model.QRCodeToken;
import com.personeltakip.repository.QRCodeTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QRCodeService {
    
    @Autowired
    private QRCodeTokenRepository qrCodeTokenRepository;
    
    /**
     * Generate QR token for a person
     * Only one token per person per day - can be used for both check-in and check-out
     */
    public String generateQRTokenForPerson(String personPsicno, String personTckiml, String firstName, String lastName, String ipAddress) {
        // Check if there's already a QR code generated for this person today
        LocalDate today = LocalDate.now();
        List<QRCodeToken> todayTokens = qrCodeTokenRepository.findUnusedByPersonPsicnoAndDate(personPsicno, today);
        
        // If there's already a token for today, return it instead of creating a new one
        if (!todayTokens.isEmpty()) {
            QRCodeToken existingToken = todayTokens.get(0); // Get the first one (should only be one)
            return existingToken.getToken();
        }
        
        // Otherwise, create a new token
        QRCodeToken token = new QRCodeToken();
        token.setPersonPsicno(personPsicno);
        token.setPersonTckiml(personTckiml);
        token.setPersonFirstName(firstName);
        token.setPersonLastName(lastName);
        token.setIpAddress(ipAddress);
        qrCodeTokenRepository.save(token);
        return token.getToken();
    }
    
    public byte[] generateQRCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
        
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }
    
    public String generateQRCodeBase64(String text, int width, int height) throws WriterException, IOException {
        byte[] qrCodeBytes = generateQRCodeImage(text, width, height);
        return Base64.getEncoder().encodeToString(qrCodeBytes);
    }
    
    /**
     * Check if token is valid (not expired and not fully used)
     * Token is valid if it hasn't been used for both check-in and check-out
     */
    public boolean isValidToken(String token) {
        var opt = qrCodeTokenRepository.findByToken(token);
        if (opt.isEmpty()) return false;
        QRCodeToken t = opt.get();
        
        // Token is invalid if expired
        if (t.getExpiresAt().isBefore(LocalDateTime.now())) return false;
        
        // Token is invalid if already used for both check-in and check-out
        if (t.getUsedForCheckIn() && t.getUsedForCheckOut()) return false;
        
        return true;
    }

    /**
     * Get a valid token for processing
     */
    public QRCodeToken getValidToken(String token) {
        var opt = qrCodeTokenRepository.findByToken(token);
        if (opt.isEmpty()) return null;
        QRCodeToken t = opt.get();
        
        // Token is invalid if expired
        if (t.getExpiresAt().isBefore(LocalDateTime.now())) return null;
        
        // Token is invalid if already used for both check-in and check-out
        if (t.getUsedForCheckIn() && t.getUsedForCheckOut()) return null;
        
        return t;
    }
    
    /**
     * Mark token as used for check-in
     */
    public void markTokenAsUsedForCheckIn(String token) {
        var opt = qrCodeTokenRepository.findByToken(token);
        if (opt.isPresent()) {
            QRCodeToken t = opt.get();
            t.setUsedForCheckIn(true);
            qrCodeTokenRepository.save(t);
        }
    }
    
    /**
     * Mark token as used for check-out
     */
    public void markTokenAsUsedForCheckOut(String token) {
        var opt = qrCodeTokenRepository.findByToken(token);
        if (opt.isPresent()) {
            QRCodeToken t = opt.get();
            t.setUsedForCheckOut(true);
            qrCodeTokenRepository.save(t);
        }
    }
    
    /**
     * Mark token as used (legacy method for backward compatibility)
     */
    public void markTokenAsUsed(String token) {
        var opt = qrCodeTokenRepository.findByToken(token);
        if (opt.isPresent()) {
            QRCodeToken t = opt.get();
            t.setUsed(true);
            qrCodeTokenRepository.save(t);
        }
    }
}
