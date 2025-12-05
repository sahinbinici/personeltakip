package com.personeltakip.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Service
public class SmsService {

    public void sendSms(String[] toPhoneNumber, String message) {
        try {
            URL url = new URL("https://api.vatansms.net/api/v1/otp");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            String jsonInputString = createJsonPayload(toPhoneNumber, message);

            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("SMS API Response Code: " + responseCode);
            System.out.println("SMS API Response Message: " + conn.getResponseMessage());

            if (responseCode != 200) {
                throw new RuntimeException("Failed to send SMS. API responded with code: " + responseCode);
            }

        } catch(Exception e) {
            throw new RuntimeException("Error while sending SMS: " + e.getMessage(), e);
        }
    }

    private String createJsonPayload(String[] toPhoneNumber, String message) {
        Map<String, Object> params = new HashMap<>();
        params.put("api_id", "29d463733f56db81be9eb355");
        params.put("api_key", "79259ea325e14e8603ab7cf7");
        params.put("sender", "G.ANTEP UNI");
        params.put("message_type", "normal");
        params.put("message", "Personel Takip Sistemi Doğrulama Kodunuz: " + message);
        params.put("phones", toPhoneNumber);

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error creating JSON for SMS API", e);
        }
    }
}