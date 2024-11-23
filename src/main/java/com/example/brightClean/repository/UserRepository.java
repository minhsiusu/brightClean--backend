package com.example.brightClean.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.example.brightClean.domain.User;


@Repository
public interface UserRepository extends JpaRepository<User,Integer>{

    @NonNull
    Optional<User> findByAccount(String account);

    @NonNull
    Optional<User> findByEmail(@NonNull String email);

    // User findByAccount(String account);

    @NonNull
    Optional<User> findByCellphone(@NonNull String cellphone);
}
