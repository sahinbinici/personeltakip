package com.personeltakip.dto;

import lombok.Data;

@Data
public class VerifyRequest {
    private String nationalId;
    private String employeeNumber;
    private String smsCode;
    private String password;
}