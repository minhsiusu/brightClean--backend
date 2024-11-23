package com.example.brightClean.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.brightClean.domain.Product;
import com.example.brightClean.repository.ProductRepository;
import com.example.brightClean.service.ProductService;

@Service
public class ProductServiceimpl implements ProductService{

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Product create(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Optional<Product> findProductById(int id) {
       return productRepository.findById(id);
    }

    @Override
    public List<Product> findProducts() {
        return productRepository.findAll();
    }

    @Override
    public void update(Product product) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }
    
    @Override
    public List<Product> findProductsByType(String type) {
        return productRepository.findProductByType(type);
    }

    @Override
    public List<Product> findProductsBySalses() {
        return productRepository.findAllByOrderBySalesDesc();
    }

    
}
