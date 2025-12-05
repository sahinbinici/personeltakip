package com.personeltakip.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String nationalId; // TC Kimlik No
    private String password;
}