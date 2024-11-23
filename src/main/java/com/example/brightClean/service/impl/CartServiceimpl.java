package com.example.brightClean.service.impl;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.brightClean.domain.AddItemRequest;
import com.example.brightClean.domain.Cart;
import com.example.brightClean.domain.CartItem;
import com.example.brightClean.domain.Product;
import com.example.brightClean.domain.User;
import com.example.brightClean.repository.CartRepository;
import com.example.brightClean.service.CartService;

@Service
public class CartServiceimpl implements CartService{

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ProductServiceimpl productServiceimpl;
    @Autowired
    private CartItemServiceimpl cartItemService;

    public Cart createCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }
    //加入購物車
    public void addToCart(Integer userId, AddItemRequest req) throws Exception{
        Cart cart = cartRepository.findCartByUserId(userId);
        Product product = productServiceimpl.findProductById(req.getProductId()).orElseThrow();
        CartItem isPresent = cartItemService.isCartItemInCart(cart, product);
        //尋找商品是否在購物車裡
        if(isPresent == null) {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(req.getQuantity());
            cartItem.setPrice(req.getQuantity() * product.getPrice());

            CartItem createdCartItem = cartItemService.createCartItem(cartItem);
            cart.getCartItems().add(createdCartItem);
            
            calcCartTotal(userId);
        }
        else{
            Integer totoal =req.getQuantity()+isPresent.getQuantity();

            if(totoal>product.getCount()){
                totoal = product.getCount();
            }
            isPresent.setQuantity(totoal);
            isPresent.setPrice((totoal) * product.getPrice());

            CartItem createdCartItem = cartItemService.createCartItem(isPresent);
            cart.getCartItems().add(createdCartItem);
            calcCartTotal(userId);
        }
    }
    //計算總數
    public Cart calcCartTotal(Integer userId) {
        Cart cart = cartRepository.findCartByUserId(userId);
        int totalPrice = 0, totalQuantity = 0;

        for(CartItem cartItem : cart.getCartItems()) {
            totalPrice += cartItem.getPrice().intValue();
            totalQuantity += cartItem.getQuantity().intValue();
        }

        cart.setTotalPrice(totalPrice);
        cart.setTotalQuantity(totalQuantity);
        return cartRepository.save(cart);
    }
    //清除購物車
    public Integer clearCart(int userId) throws Exception {
        Cart cart = cartRepository.findCartByUserId(userId);
        Integer totalPrice = cart.getTotalPrice();

        Iterator<CartItem> iterator = cart.getCartItems().iterator();
        while (iterator.hasNext()) {
            CartItem cartItem = iterator.next();
            cartItemService.removeCartItem(userId, cartItem.getId());
            iterator.remove();
        }

        cart.setTotalPrice(0);
        cart.setTotalQuantity(0);
        cartRepository.save(cart);

        return totalPrice;
    }

    
}
