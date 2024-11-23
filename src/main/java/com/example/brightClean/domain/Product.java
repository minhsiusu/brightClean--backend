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
@Table(name = "product")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 或 GenerationType.AUTO
    private Integer id;

    @Column(name="name")
    private String name;
    
    @Column(name="price")
    private Integer price;
    
    @Column(name="count")
    private Integer count;
    
    @Column(name="type")
    private String type;

    @Column(name="picture")
    private String picture;
    
    @Column(name="detail")
    private String detail;
    
    @Column(name="sales")
    private Integer sales;
}
