package com.example.brightClean.service;

import com.example.brightClean.domain.AddItemRequest;
import com.example.brightClean.domain.Cart;
import com.example.brightClean.domain.User;


public interface CartService {

    Cart createCart(User user);

    void addToCart(Integer userId, AddItemRequest req) throws Exception;
    
    Cart calcCartTotal(Integer userId);
    
    public Integer clearCart(int userId) throws Exception;
}
