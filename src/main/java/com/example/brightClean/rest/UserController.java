package com.example.brightClean.rest;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.print.DocFlavor.STRING;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.brightClean.domain.AuthResponse;
import com.example.brightClean.domain.Cart;
import com.example.brightClean.domain.User;
import com.example.brightClean.domain.params.ForgetParam;
import com.example.brightClean.domain.params.LoginParam;
import com.example.brightClean.domain.params.UserParam;
import com.example.brightClean.domain.params.UserEditParam;
import com.example.brightClean.exception.NotFoundException;
import com.example.brightClean.repository.UserRepository;
import com.example.brightClean.service.MailService;
import com.example.brightClean.service.UserService;
import com.example.brightClean.service.CartService;
import com.example.brightClean.service.impl.JwtService;

import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.servlet.http.Cookie;

@RestController
@RequestMapping("/user")
@Tag(name = "user")
public class UserController {

    @Autowired
    private UserService userservice;

    @Autowired
    private MailService mailService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CartService cartService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/forget")
    public ResponseEntity<String> forgetPassword(@RequestBody LoginParam loginParam, HttpServletResponse response) {
        // 查找用戶
        User user = userservice.findByEmail(loginParam.getEmail())
                .orElseThrow(() -> new NotFoundException("找無此信箱"));
        // 生成一個小時有效的 Token
        String mailToken = jwtService.generateToken(user, 1, 0, "MAIL");

        String resetLink = "http://localhost:8080/forgot-password?token=" + mailToken;
        mailService.sendPlainText(
                loginParam.getEmail(),
                "重設密碼",
                "請點擊以下連結重設密碼：\n" + resetLink + "\n該連結 1 小時內有效。");

        return ResponseEntity.ok("重設密碼鏈接已發送至您的信箱！");
    }

    @GetMapping("/reset")
    public ResponseEntity<String> verifyResetToken(
            @RequestParam(name = "token", required = false) String urlToken,
            HttpServletResponse response) {

        if (urlToken == null || jwtService.isTokenInvalid(urlToken, "MAIL")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("連結已失效，請重新申請重設密碼");
        }
        // 從 Token 中解析 Email
        String mailToken = jwtService.getEmailFromJWT(urlToken, "MAIL");
        // 為用戶設置一個短期驗證 Cookie(10 分鐘有效）
        addSameSiteCookie(response, "mailtoken", urlToken, 10 * 60); // 10 分鐘有效

        return ResponseEntity.ok("Token 驗證成功，請繼續重設密碼");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestBody Map<String, String> request,
            @CookieValue(name = "mailtoken", required = false) String token,
            HttpServletResponse response) {

        if (token == null || jwtService.isTokenInvalid(token, "MAIL")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("連結已失效或無效");
        }

        String newPassword = request.get("password");
        if (newPassword == null || newPassword.length() < 8) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("密碼必須至少包含 8 個字元");
        }
        try {
            // 從 Token 中解析用戶 Email
            String email = jwtService.getEmailFromJWT(token, "MAIL");
            // 根據 Email 查找用戶
            User user = userservice.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("用戶不存在"));
            // 更新用戶密碼
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // 將 Token 標記為無效
            jwtService.markTokenAsUsed(token, "MAIL");

            // 刪除 mailtoken Cookie
            clearCookie(response, "mailtoken");

            return ResponseEntity.ok("密碼重置成功");
        } catch (Exception e) {
            // 處理其他未預期的錯誤
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("系統錯誤，請稍後再試");
        }
    }

    // 發起註冊請求 (生成註冊連結)
    @PostMapping("/register/initiate")
    public ResponseEntity<String> initiateRegister(@RequestBody LoginParam loginParam, HttpServletResponse response) {
        // 查詢 Email 是否已註冊
        Optional<User> optionalUser = userRepository.findByEmail(loginParam.getEmail());
        if (optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("該 Email 已被使用！");
        }

        // 當該用戶不存在時才創建 Token
        User user = new User();

        // 生成註冊 Token
        String registerToken = jwtService.generateToken(user, 24, 0, "REGISTER");

        // 生成連結網址
        String registerLink = "http://localhost:8080/register?token="
                + registerToken
                + "&email="
                + URLEncoder.encode(loginParam.getEmail(), StandardCharsets.UTF_8);
        // 發送註冊確認信件
        mailService.sendPlainText(
                loginParam.getEmail(),
                "註冊驗證",
                "請點擊以下連結完成註冊：\n" + registerLink + "\n該連結 24 小時內有效。");

        return ResponseEntity.ok("註冊確認連結已發送到您的信箱！");
    }

    // 驗證註冊 Token
    @GetMapping("/register/confirm")
    public ResponseEntity<String> confirmRegister(
            @RequestParam(name = "token", required = false) String registerUrlToken,
            HttpServletResponse response) {
        if (registerUrlToken == null || jwtService.isTokenInvalid(registerUrlToken, "REGISTER")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("連結已失效，請重新申請註冊");
        }
        // 從 Token 中解析 Email
        String registerEmail = jwtService.getEmailFromJWT(registerUrlToken, "REGISTER");

        // 為用戶設置一個短期驗證 Cookie(10 分鐘有效）
        addSameSiteCookie(response, "registertoken", registerUrlToken, 10 * 60); // 10 分鐘有效

        return ResponseEntity.ok("Token 驗證成功，請繼續註冊");
    }

    // 完成註冊
    @PostMapping("/register/complete")
    public ResponseEntity<String> completeRegister(
            @RequestBody UserParam userParam,
            @CookieValue(name = "registertoken", required = false) String registerToken,
            HttpServletResponse response) {
        try {
            if (registerToken == null || jwtService.isTokenInvalid(registerToken, "REGISTER")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("連結已失效或無效");
            }
            // 從 userParam 中取得 email
            String email = userParam.getEmail();

            // 檢查 email 是否有效
            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email 不能為空！");
            }

            // 檢查該 email 是否已註冊
            if (userservice.findByEmail(email).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("該 Email 已被註冊");
            }

            // 創建使用者
            userservice.createUser(userParam);

            User user = userservice.findByEmail(userParam.getEmail())
                    .orElseThrow(() -> new NotFoundException("用戶不存在"));

            // 創建指定購物車
            cartService.createCart(user);

            // 將 Token 標記為無效
            jwtService.markTokenAsUsed(registerToken, "REGISTER");

            // 刪除 registertoken Cookie
            clearCookie(response, "registertoken");

            return ResponseEntity.ok("註冊成功！");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("系統錯誤，請稍後重試！");
        }
    }

    // 設置 Cookie
    private void addSameSiteCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setDomain("localhost");
        response.addCookie(cookie);
    }

    // 清除 Cookie
    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    // 用戶登錄
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginParam loginParam, HttpServletResponse response) {
        String email = loginParam.getEmail();
        User user = userservice.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Invalid email or password"));

        if (!passwordEncoder.matches(loginParam.getPassword(), user.getPassword())) {
            throw new NotFoundException("Invalid email or password");
        }

        String token = jwtService.generateToken(user, 12, 60, "JWT"); // 使用普通 JWT

        // 使用 Cookie 物件設置 JWT
        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(24 * 60 * 60); // 24 小時
        // 開發環境移除 Secure，正式環境加上
        jwtCookie.setSecure(false); // HTTPS 下應設為 true
        jwtCookie.setDomain("localhost"); // 如果跨域，需指定域名
        response.addCookie(jwtCookie);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(token);
        authResponse.setMessage("Login Success");
        authResponse.setAccount(user.getAccount());
        authResponse.setEmail(user.getEmail());

        return ResponseEntity.ok(authResponse);
    }

    // 驗證 JWT
    @GetMapping("/jwt")
    public ResponseEntity<User> getUserInfo(@CookieValue(name = "jwt", required = false) String jwt) throws Exception {
        User user = userservice.findUserByJWT(jwt);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        clearJwtCookie(response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> checkSession(
            @CookieValue(name = "jwt", required = false) String jwt,
            HttpServletResponse response) {
        Map<String, Object> responseMap = new HashMap<>();

        if (jwt == null || jwt.isEmpty()) {
            responseMap.put("isLoggedIn", false);
            responseMap.put("message", "未登入");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMap);
        }

        try {
            // 從 JWT 提取 Email
            String email = jwtService.getEmailFromJWT(jwt, "JWT");

            // 根據 Email 查找用戶
            User user = userservice.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("用戶未找到，請重新登入"));

            // 成功返回用戶信息
            responseMap.put("isLoggedIn", true);
            responseMap.put("name", user.getName());
            responseMap.put("account", user.getAccount());
            responseMap.put("email", user.getEmail());
            responseMap.put("address", user.getAddress());
            responseMap.put("cellphone", user.getCellphone());
            responseMap.put("id", user.getId());
            return ResponseEntity.ok(responseMap);

        } catch (JwtException e) {
            // JWT 驗證失敗，清除 Cookie
            clearJwtCookie(response);
            responseMap.put("isLoggedIn", false);
            responseMap.put("message", "JWT 驗證失敗：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMap);
        } catch (NotFoundException e) {
            clearJwtCookie(response);
            responseMap.put("isLoggedIn", false);
            responseMap.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMap);
        } catch (Exception e) {
            responseMap.put("isLoggedIn", false);
            responseMap.put("message", "系統錯誤，請稍後重試");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    private void clearJwtCookie(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // 根據需求設置為 true 如果使用 HTTPS
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // 立即過期
        response.addCookie(jwtCookie);
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateUserInfo(
            @RequestBody @Valid UserEditParam userEditParam,
            @CookieValue(name = "jwt", required = false) String jwt) {

        if (jwt == null || jwtService.isTokenInvalid(jwt, "JWT")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用戶未登入或 JWT 無效");
        }

        try {
            // 從 JWT 中解析 Email
            String email = jwtService.getEmailFromJWT(jwt, "JWT");

            // 根據 Email 查找用戶
            User user = userservice.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("用戶不存在"));

            // 更新用戶資訊
            if (userEditParam.getName() != null) {
                user.setName(userEditParam.getName());
            }
            if (userEditParam.getAddress() != null) {
                user.setAddress(userEditParam.getAddress());
            }
            if (userEditParam.getCellPhone() != null) {
                user.setCellphone(userEditParam.getCellPhone());
            }

            // 保存更新後的用戶資訊
            userRepository.save(user);

            return ResponseEntity.ok("用戶資訊更新成功");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新用戶資訊時發生錯誤");
        }
    }
}
