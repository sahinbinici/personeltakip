package com.personeltakip.service;

import com.personeltakip.automation.repository.PersonRepository;
import com.personeltakip.model.Role;
import com.personeltakip.model.User;
import com.personeltakip.repository.UserRepository;
import com.personeltakip.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private PersonRepository personRepository; // From 'isicil' DB

    @Autowired
    private UserRepository userRepository; // From 'personeltakip' DB

    @Autowired
    private SmsService smsService; // Real SMS service

    @Autowired
    private PasswordEncoder passwordEncoder;

    // In a real app, use a more robust cache like Redis
    private final Map<String, String> smsVerificationCodes = new HashMap<>();

    @Transactional("automationTransactionManager")
    public void initiateRegistration(String nationalId, String employeeNumber) {
        // Validate input
        if (nationalId == null || nationalId.trim().isEmpty()) {
            throw new RuntimeException("National ID cannot be empty.");
        }

        if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
            throw new RuntimeException("Employee number cannot be empty.");
        }

        logger.info("Initiating registration for national ID: {} and employee number: {}", nationalId, employeeNumber);

        // 1. Check if person exists and get their detailed info from 'isicil'
        // Verify both TC Kimlik No and Sicil Numarası
        Map<String, Object> personDetails = personRepository.findDetailedPersonInfoByTckimlAndBrkodu(Long.parseLong(nationalId), Integer.parseInt(employeeNumber))
                .orElseThrow(() -> new RuntimeException("Person with this National ID and Employee Number not found in the master database."));

        // 2. Check if user is already registered in our system
        if (userRepository.findByTckiml(Long.parseLong(nationalId)).isPresent()) {
            logger.warn("User already registered with national ID: {}", nationalId);
            throw new RuntimeException("User is already registered.");
        }

        // 3. Get phone number
        String phoneNumber = (String) personDetails.get("cepTel");
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            // Try alternative field names
            phoneNumber = (String) personDetails.get("CEPTEL");
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            logger.error("Phone number not found for user with national ID: {}", nationalId);
            throw new RuntimeException("Phone number not found for this user.");
        }

        // 4. Generate and send SMS code
        String smsCode = String.format("%06d", new Random().nextInt(999999));
        smsVerificationCodes.put(nationalId, smsCode);
        logger.info("Generated SMS code for national ID: {}", nationalId);

        smsService.sendSms(new String[]{phoneNumber}, smsCode);
        logger.info("SMS code sent to phone number: {} for national ID: {}", phoneNumber, nationalId);
    }

    @Transactional("personnelTransactionManager")
    public void completeRegistration(String nationalId, String employeeNumber, String smsCode, String password) {
        // Validate inputs
        if (nationalId == null || nationalId.trim().isEmpty()) {
            throw new RuntimeException("National ID cannot be empty.");
        }
        if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
            throw new RuntimeException("Employee number cannot be empty.");
        }
        if (smsCode == null || smsCode.trim().isEmpty()) {
            throw new RuntimeException("SMS code cannot be empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password cannot be empty.");
        }

        logger.info("Completing registration for national ID: {} and employee number: {}", nationalId, employeeNumber);

        // 1. Verify SMS code
        String storedCode = smsVerificationCodes.get(nationalId);
        if (storedCode == null || !storedCode.equals(smsCode)) {
            logger.warn("Invalid or expired SMS code for national ID: {}", nationalId);
            throw new RuntimeException("Invalid or expired SMS code.");
        }

        // Check if this is the first user (make them admin)
        boolean isFirstUser = userRepository.count() == 0;
        Role userRole = isFirstUser ? Role.ROLE_ADMIN : Role.ROLE_USER;

        // 2. Create the user in 'personeltakip' database
        User newUser = User.builder()
                .tckiml(Long.parseLong(nationalId)) // Use National ID as the tckiml (BigInt)
                .username(nationalId) // Use National ID as the username
                .psicno(Integer.parseInt(employeeNumber)) // Store employee number as Integer
                .password(passwordEncoder.encode(password))
                .role(userRole) // Assign appropriate role
                .build();

        User savedUser = userRepository.save(newUser);
        logger.info("User created successfully with ID: {}", savedUser.getId());

        smsVerificationCodes.remove(nationalId); // Clean up the code
        logger.info("Registration completed and SMS code cleaned up for national ID: {}", nationalId);
    }

    public String login(String nationalId, String password) {
        // Validate inputs
        if (nationalId == null || nationalId.trim().isEmpty()) {
            throw new RuntimeException("National ID cannot be empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password cannot be empty.");
        }

        logger.info("Login attempt for national ID: {}", nationalId);

        // Login happens against our own 'personeltakip' database
        User user = userRepository.findByTckiml(Long.parseLong(nationalId))
                .orElseThrow(() -> {
                    logger.warn("User not found with national ID: {}", nationalId);
                    return new RuntimeException("User not found.");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Invalid credentials for national ID: {}", nationalId);
            throw new RuntimeException("Invalid credentials.");
        }

        logger.info("Successful login for user ID: {}", user.getId());

        // Here we would generate and return a JWT token.
        // For now, we'll return a simple success message.
        return "Login Successful! (JWT token would be here)";
    }

    public User getUserByNationalId(String nationalId) {
        return userRepository.findByTckiml(Long.parseLong(nationalId))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}