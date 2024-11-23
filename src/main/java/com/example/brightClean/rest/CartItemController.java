package com.example.brightClean.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.brightClean.domain.CartItem;
import com.example.brightClean.domain.User;
import com.example.brightClean.service.UserService;
import com.example.brightClean.service.impl.CartItemServiceimpl;
import com.example.brightClean.service.CartService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/cartItem")
@Tag(name = "cartItem")
public class CartItemController {

    @Autowired
    private UserService userService;

    @Autowired
    private CartItemServiceimpl cartItemService;

    @Autowired
    private CartService cartService;

    // 更新購物車裡的商品
    @PutMapping("/{cartItemId}")
    public ResponseEntity<String> updateCartItem(@PathVariable("cartItemId") Integer cartItemId,
            @RequestBody CartItem cartItem, @RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserByJWT(jwt);
        cartItemService.updateCartItem(user.getId(), cartItemId, cartItem);

        return new ResponseEntity<>("Cart item updated successfully", HttpStatus.OK);
    }

    // 刪除單一個購物車裡的商品
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<String> deleteCartItem(@PathVariable("cartItemId") Integer cartItemId,
            @CookieValue(name = "jwt", required = false) String jwt) throws Exception {
        User user = userService.findUserByJWT(jwt);
        cartItemService.removeCartItem(user.getId(), cartItemId);
        return new ResponseEntity<>("CartItem deleted successfully", HttpStatus.OK);
    }

    // 刪除購物車裡的全部商品
    @DeleteMapping("/deleteall")
    @Transactional
    public ResponseEntity<String> clearCart(@CookieValue(name = "jwt", required = false) String jwt) throws Exception {
        User user = userService.findUserByJWT(jwt);
        // 呼叫購物車服務層 使用清空購物車的方法
        // cartItemService.clearUserCartItem(user.getId());

        cartService.clearCart(user.getId());
        return new ResponseEntity<>("Cart cleared successfully", HttpStatus.OK);
    }
}
