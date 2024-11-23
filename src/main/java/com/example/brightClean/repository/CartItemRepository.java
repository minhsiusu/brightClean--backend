package com.example.brightClean.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.brightClean.domain.Cart;
import com.example.brightClean.domain.CartItem;
import com.example.brightClean.domain.Product;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart = :cart AND ci.product = :product")
    public CartItem isCartItemInCart(@Param("cart") Cart cart, @Param("product") Product product);

    // 使用 cartId 刪除所有 CartItem
    void deleteAllByCartId(Integer cartId);
}
