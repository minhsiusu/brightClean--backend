package com.example.brightClean.service;

import com.example.brightClean.domain.Cart;
import com.example.brightClean.domain.CartItem;
import com.example.brightClean.domain.Product;



public interface CartItemService {

    CartItem isCartItemInCart(Cart cart, Product product);

    CartItem createCartItem(CartItem cartItem);

    CartItem updateCartItem(Integer userId, Integer cartItemId, CartItem cartItem) throws Exception;

    CartItem findCartItemById(Integer id) throws Exception;

    void removeCartItem(Integer userId, Integer cartItemId) throws Exception;

    void clearUserCartItem(Integer userId);
}
