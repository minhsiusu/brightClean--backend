package com.example.brightClean.service;

import java.util.List;
import java.util.Optional;
import com.example.brightClean.domain.Product;

public interface ProductService {
    Product create (Product product);

    Optional<Product> findProductById(int id);

    List<Product> findProducts();

    void update(Product product)throws Exception;

    List<Product> findProductsByType(String type);

    List<Product> findProductsBySalses();
}
