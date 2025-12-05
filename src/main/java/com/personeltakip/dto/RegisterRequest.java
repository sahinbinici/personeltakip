package com.personeltakip.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String nationalId; // TC Kimlik No
    private String employeeNumber; // Sicil Numarası
}