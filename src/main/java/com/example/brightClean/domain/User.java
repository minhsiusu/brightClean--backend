package com.example.brightClean.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="user")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 或 GenerationType.AUTO
    private Integer id;

    @Column(name="name")
    private String name;

    @Column(name="account")
    private String account;

    @Column(name="password")
    private String password;

    @Column(name="email")
    private String email;

    @Column(name="cellphone")
    private String cellphone;
    
    @Column(name="address")
    private String address;
}
