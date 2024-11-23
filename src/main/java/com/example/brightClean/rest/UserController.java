package com.example.brightClean.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.example.brightClean.exception.NotFoundException;
import com.example.brightClean.repository.UserRepository;
import com.example.brightClean.service.MailService;
import com.example.brightClean.service.UserService;
import com.example.brightClean.service.CartService;
import com.example.brightClean.service.impl.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

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

    // @GetMapping("/get")
    // public User getMethodName(@RequestParam("id") int id) {
    // return userservice.getUser(id).get();
    // }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> regist(@RequestBody @Valid UserParam userParam) throws Exception {
        // TODO: process POST request
        // 使用JWTProvider產生token
        System.out.println(userParam);
        userservice.createUser(userParam);
        Cart cart = cartService.createCart(userservice.findByEmail(userParam.getEmail()).orElseThrow());
        String token = jwtService.generateToken(userParam.getEmail(), 12, 60);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(token);
        authResponse.setMessage("Signup Success");
        authResponse.setAccount(userParam.getAccount());
        authResponse.setEmail(userParam.getEmail());

        System.out.println(token);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginParam loginParam) {
        String email = loginParam.getEmail();

        String mismatchTip = "名稱或密碼不正確";
        System.out.println(email);
        System.out.println(loginParam.getPassword());

        User user = userservice.findByEmail(email).orElseThrow();
        // try {
        // // Get user by account
        // // user = userservice.getByAccountOfNonNull(account);

        // user = userservice.getByEmail(account).orElseThrow();
        // System.out.println(user);
        // } catch (NotFoundException e) {

        // throw new NotFoundException(mismatchTip);
        // }

        // if (!userservice.passwordMatch(user, loginParam.getPassword())) {

        // throw new NotFoundException(mismatchTip);
        // }
        AuthResponse authResponse = new AuthResponse();

        if (user == null || !passwordEncoder.matches(loginParam.getPassword(), user.getPassword())) {
            new Exception("Invalid email or password");
            authResponse.setJwt(loginParam.getEmail());
            authResponse.setMessage("Login failed");

            System.out.println(loginParam.getEmail());
            return new ResponseEntity<>(authResponse, HttpStatus.BAD_REQUEST);
        }

        String token = jwtService.generateToken(user.getEmail(), 12, 60);

        // 設置 Cookie
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();
        responseBuilder.header("Set-Cookie",
                "jwt=" + token + "; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=" + (24 * 60 * 60));

        authResponse.setMessage("Login Success");
        authResponse.setAccount(user.getAccount());
        authResponse.setEmail(user.getEmail());

        return responseBuilder.body(authResponse);
    }

    @GetMapping("/getall")
    public List<User> findUsers() {
        return userservice.findUsers();
    }

    // @PostMapping("/add")
    // public ResponseEntity<AuthResponse> postMethodName(@RequestBody User user)
    // throws Exception {

    // String token = jwtService.generateToken(user.getEmail());

    // AuthResponse authResponse = new AuthResponse();
    // authResponse.setJwt(token);
    // authResponse.setMessage("Signup Success");

    // System.out.println(token);
    // userservice.createUser(user);

    // return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    // }

    @PostMapping("/forget")
    public ResponseEntity<ForgetParam> getMethodName(@RequestBody LoginParam loginParam) {
        System.out.println(loginParam);
        userservice.findByEmail(loginParam.getEmail())
                .orElseThrow(() -> new NotFoundException("找無此信箱").setErrorData(loginParam.getEmail()));
        String token = jwtService.generateToken(loginParam.getEmail(), 1, 1);

        ForgetParam forgetParam = new ForgetParam();
        forgetParam.setEmail(loginParam.getEmail());
        forgetParam.setJwt(token);
        // mailService.sendPlainText(loginParam.getEmail(),"忘記密碼","點選更改網址來更換密碼
        // localhost:8081/user/register");
        return new ResponseEntity<>(forgetParam, HttpStatus.OK);
    }

    @PutMapping("/reset/{email}")
    public ResponseEntity<LoginParam> chagedPassword(@PathVariable("email") String email,
            @RequestBody LoginParam loginParam, @CookieValue(name = "jwt", required = false) String jwt)
            throws Exception {
        // TODO: process PUT request
        User user = userservice.findUserByJWT(jwt);
        System.out.println(loginParam.getPassword());
        user.setPassword(passwordEncoder.encode(loginParam.getPassword()));
        userRepository.save(user);
        return new ResponseEntity<>(loginParam, HttpStatus.OK);
    }

    @GetMapping("/jwt")
    public ResponseEntity<User> getUserInfo(@CookieValue(name = "jwt", required = false) String jwt) throws Exception {
        User user = userservice.findUserByJWT(jwt);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();
        responseBuilder.header("Set-Cookie", "jwt=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0");
        return responseBuilder.build();
    }

    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> checkSession(@CookieValue(name = "jwt", required = false) String jwt) {
        Map<String, Object> response = new HashMap<>();
        if (jwt == null || jwt.isEmpty()) {
            response.put("isLoggedIn", false);
            response.put("message", "未登入");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        try {
            String email = jwtService.getEmailFromJWT(jwt);
            User user = userservice.findByEmail(email).orElseThrow(() -> new NotFoundException("用戶未找到"));

            response.put("isLoggedIn", true);
            response.put("account", user.getAccount());
            response.put("email", user.getEmail());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("isLoggedIn", false);
            response.put("message", "無效的 JWT");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }
}
