package com.example.brightClean.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.brightClean.domain.AddItemRequest;
import com.example.brightClean.domain.Cart;
import com.example.brightClean.domain.User;
import com.example.brightClean.service.UserService;
import com.example.brightClean.service.CartService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/cart")
@Tag(name = "cart")
public class CartController {

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    // 尋找使用者的購物車
    @GetMapping("/")
    public ResponseEntity<Cart> findUserCart(@CookieValue(name = "jwt", required = false) String jwt) throws Exception {
        User user = userService.findUserByJWT(jwt);
        Cart cart = cartService.calcCartTotal(user.getId());
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    // 增加商品到購物車
    @PutMapping("/add")
    public ResponseEntity<String> addItemToCart(@RequestBody AddItemRequest req,
            @CookieValue(name = "jwt", required = false) String jwt) throws Exception {
        User user = userService.findUserByJWT(jwt);
        cartService.addToCart(user.getId(), req);
        return new ResponseEntity<>("Item added to cart", HttpStatus.OK);
    }

}
