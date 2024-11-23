package com.example.brightClean.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.brightClean.domain.Cart;
import com.example.brightClean.domain.CartItem;
import com.example.brightClean.domain.Product;
import com.example.brightClean.domain.User;
import com.example.brightClean.repository.CartItemRepository;
import com.example.brightClean.repository.CartRepository;
import com.example.brightClean.service.CartItemService;

@Service
public class CartItemServiceimpl implements CartItemService{

    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserServiceimpl userServiceimpl;

    public CartItem isCartItemInCart(Cart cart, Product product) {
        return cartItemRepository.isCartItemInCart(cart, product);
    }

    public CartItem createCartItem(CartItem cartItem) {
        cartItem.setQuantity(Math.max(cartItem.getQuantity(), 1));
        cartItem.setPrice(cartItem.getProduct().getPrice() * cartItem.getQuantity());

        return cartItemRepository.save(cartItem);
    }

    public CartItem updateCartItem(Integer userId, Integer cartItemId, CartItem cartItem) throws Exception {
        CartItem item = findCartItemById(cartItemId);
        User user = userServiceimpl.findUserById(item.getCart().getUser().getId()).orElseThrow();
        Integer quantity = cartItem.getQuantity();
        //判斷userId是否正確
        if(user.getId().equals(userId)) {
            //判斷增加數量是否大於等於庫存數量
            if(quantity>=item.getProduct().getCount()){
                quantity=item.getProduct().getCount();
            }
            item.setQuantity(quantity);
            item.setPrice(item.getQuantity() * item.getProduct().getPrice());
        }

        return cartItemRepository.save(item);
    }

  

    public CartItem findCartItemById(Integer id) throws Exception {
        Optional<CartItem> optionalCartItem = cartItemRepository.findById(id);
        if(optionalCartItem.isPresent()) {
            return optionalCartItem.get();
        }
        throw new Exception("CartItem not found with id : " + id);
    }

    public void removeCartItem(Integer userId, Integer cartItemId) throws Exception {
        CartItem item = findCartItemById(cartItemId);
        User user = userServiceimpl.findUserById(item.getCart().getUser().getId()).orElseThrow();
        User reqUser = userServiceimpl.findUserById(userId).orElseThrow();
        if(user.getId().equals(reqUser.getId())) {
            cartItemRepository.deleteById(cartItemId);
            return;
        }
    }
    
    public void clearUserCartItem(Integer userId) {
        // 根據 userId 找到對應的 Cart
        Cart cart = cartRepository.findCartByUserId(userId);

        if (cart != null) {
            // 刪除該 Cart 下的所有 CartItem
            cartItemRepository.deleteAllByCartId(cart.getId());

            // 重設 Cart 的總價格和總數量
            cart.setTotalPrice(0);
            cart.setTotalQuantity(0);
            cartRepository.save(cart); // 保存變更
        }
  }
}
