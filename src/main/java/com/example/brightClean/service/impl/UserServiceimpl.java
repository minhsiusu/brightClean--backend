package com.example.brightClean.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.brightClean.domain.User;
import com.example.brightClean.domain.params.UserParam;
import com.example.brightClean.exception.NotFoundException;
import com.example.brightClean.repository.UserRepository;
import com.example.brightClean.service.UserService;

import io.jsonwebtoken.JwtException;
import io.micrometer.common.lang.NonNull;

@Service
public class UserServiceimpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User create(@NonNull User user) {
        return userRepository.save(user);
    }

    @Override
    public void update(User user) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public List<User> findUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findUserById(int id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByCellPhone(String cellphone) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getByCellPhone'");
    }

    @Override
    public Optional<User> findUserByAccount(String account) {
        return userRepository.findByAccount(account);
    }

    @Override
    public User findByAccountOfNonNull(String account) {
        return findUserByAccount(account)
                .orElseThrow(() -> new NotFoundException("The name does not exist").setErrorData(account));
    }

    @Override
    public boolean passwordMatch(@org.springframework.lang.NonNull User user, @Nullable String plainPassword) {
        // TODO Auto-generated method stub
        return passwordEncoder.matches(plainPassword, user.getPassword());
    }

    // @Override
    // public UserDetails loadUserByUsername(String account) throws
    // UsernameNotFoundException {
    // User user=userRepository.findByAccount(account).orElseThrow();
    // return (UserDetails) user;
    // }

    public void createUser(UserParam userParam) throws IllegalArgumentException, Exception {
        // 檢查 Email 是否已經被註冊
        if (userParam.getEmail() == null || userParam.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email 不能為空！");
        }

        if (userRepository.findByEmail(userParam.getEmail()).isPresent()) {
            throw new IllegalArgumentException("該 Email 已被註冊！");
        }

        // 創建新用戶
        User createdUser = new User();
        createdUser.setName(userParam.getName());
        createdUser.setAccount(userParam.getAccount());
        createdUser.setEmail(userParam.getEmail());
        createdUser.setPassword(passwordEncoder.encode(userParam.getPassword()));
        createdUser.setCellphone(userParam.getCellPhone());
        createdUser.setAddress(userParam.getAddress());

        try {
            // 保存用戶到資料庫
            userRepository.save(createdUser);
            System.out.println("用戶註冊成功: " + userParam.getEmail());
        } catch (Exception e) {
            // 捕獲資料庫異常並封裝為更具描述性的異常
            throw new Exception("系統錯誤：用戶無法創建，請稍後再試！", e);
        }
    }

    public User findUserByJWT(String jwt) throws Exception {
        try {
            // 根據 JWT 自動判斷其類型並提取 Email
            String email;

            if (jwt.startsWith("MAIL_")) {
                email = jwtService.getEmailFromJWT(jwt, "MAIL");
            } else if (jwt.startsWith("REGISTER_")) {
                email = jwtService.getEmailFromJWT(jwt, "REGISTER");
            } else {
                email = jwtService.getEmailFromJWT(jwt, "JWT"); // 默認處理普通 JWT
            }

            // 根據 Email 查找用戶
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("User not found for email: " + email));
        } catch (JwtException e) {
            throw new Exception("Invalid or expired JWT: " + e.getMessage(), e);
        } catch (NotFoundException e) {
            throw new Exception("User not found: " + e.getMessage(), e);
        }
    }
}
