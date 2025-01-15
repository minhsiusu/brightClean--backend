package com.example.brightClean.util;

import org.springframework.beans.factory.annotation.Value;

import io.github.cdimascio.dotenv.Dotenv;

//加鹽值
public class JWTConstant {

    // 加載 .env 文件
    static Dotenv dotenv = Dotenv.load();
    // JWT 密鑰（加鹽值）
    public static final String SECRET = dotenv.get("JWT_CONSTANT");
    public static final String M_SECRET = dotenv.get("MAIL_CONSTANT");
    public static final String REGISTER_CONSTANT = dotenv.get("REGISTER_CONSTANT");
}
