package com.example.brightClean.service;

import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.example.brightClean.domain.User;
import com.example.brightClean.domain.params.UserParam;

public interface UserService {

    User create(User user);

    Optional<User> findUserById(int id);

    @NonNull
    Optional<User> findUserByAccount(@NonNull String account);

    @NonNull
    User findByAccountOfNonNull(@NonNull String account);

    boolean passwordMatch(@NonNull User user, @Nullable String plainPassword);

    List<User> findUsers();

    void update(User user) throws Exception;

    @NonNull
    Optional<User> findByEmail(@NonNull String email);

    @NonNull
    Optional<User> findByCellPhone(@NonNull String cellphone);

    void createUser(UserParam userParam) throws Exception;

    User findUserByJWT(String jwt) throws Exception;
}
