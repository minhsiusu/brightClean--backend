package com.example.brightClean.domain;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 指定了cart_id 作為外鍵 對應資料庫的 "cart_id" 欄位
    @ManyToOne
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Cart cart;

    // 指定了 product_id 作為外鍵 對應資料庫的 "product_id" 欄位
    @ManyToOne
    private Product product;
    
    @Column(name="price")
    private Integer price;

    @Column(name="quantity")
    private Integer quantity;


    //  // 同樣使用 id 而不是雙向關聯的 cart
    //  @Override
    //  public int hashCode() {
    //      return Objects.hash(id);
    //  }
 
    //  @Override
    //  public boolean equals(Object o) {
    //      if (this == o) return true;
    //      if (o == null || getClass() != o.getClass()) return false;
    //      CartItem cartItem = (CartItem) o;
    //      return Objects.equals(id, cartItem.id);
    //  }
}
