package com.example.brightClean.domain;

import lombok.Data;

@Data
public class AuthResponse {
    private String jwt;
    private String message;
    private String account;
    private String email;
    private Integer cartId;
    private Integer userId;
}
