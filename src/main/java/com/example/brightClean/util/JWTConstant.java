package com.example.brightClean.util;

import org.springframework.beans.factory.annotation.Value;

import io.github.cdimascio.dotenv.Dotenv;

//加鹽值
public class JWTConstant {
    // @Value("${JWT_CONSTANT}")
    // public static String SECRET;
    static Dotenv dotenv = Dotenv.load();
    public static final String SECRET = dotenv.get("JWT_CONSTANT");
}
