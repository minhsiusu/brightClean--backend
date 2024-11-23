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

import io.micrometer.common.lang.NonNull;

@Service
public class UserServiceimpl implements UserService{


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
        return findUserByAccount(account).orElseThrow(() -> new NotFoundException("The name does not exist").setErrorData(account));
    }

    @Override
    public boolean passwordMatch(@org.springframework.lang.NonNull User user, @Nullable String plainPassword) {
        // TODO Auto-generated method stub
        return passwordEncoder.matches(plainPassword, user.getPassword());
    }

    // @Override
    // public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
    //     User user=userRepository.findByAccount(account).orElseThrow();
    //     return (UserDetails) user;
    // }

    public void createUser(UserParam userParam) throws Exception {
        // User isEmailExists = userRepository.findByEmail(user.getEmail()).orElseThrow();

        // if(isEmailExists != null) {
        //     throw new Exception("Error: Email is already registered.");
        // }

        User createdUser = new User();
        createdUser.setName(userParam.getName());
        createdUser.setAccount(userParam.getAccount());
        createdUser.setEmail(userParam.getEmail());
        createdUser.setPassword(passwordEncoder.encode(userParam.getPassword()));
        createdUser.setCellphone(userParam.getCellPhone());
        createdUser.setAddress(userParam.getAddress());

        userRepository.save(createdUser);
    }

    public User findUserByJWT(String jwt) throws Exception{
        String email = jwtService.getEmailFromJWT(jwt);
        User user = userRepository.findByEmail(email).orElseThrow();
        if(user == null){
            throw new Exception("Error: Invalid JWT");
        }
        return user;
    }
}
