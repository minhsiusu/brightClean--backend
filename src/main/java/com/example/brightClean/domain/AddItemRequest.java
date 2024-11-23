package com.example.brightClean.domain;

import lombok.Data;

@Data
public class AddItemRequest {
    private Integer productId;
    private Integer quantity;

    public AddItemRequest(){

    }
}
