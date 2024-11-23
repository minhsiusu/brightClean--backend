package com.example.brightClean.rest;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.brightClean.domain.Product;
import com.example.brightClean.repository.ProductRepository;
import com.example.brightClean.service.ProductService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/product")
@Tag(name = "product")
public class ProductController {
    
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    
    @GetMapping("/get")
    public Product findProductById(@RequestParam("id") Integer id) {
        return productService.findProductById(id).get();
    }
    
    @GetMapping("/getall")
    public List<Product> findProducts(){
        return productService.findProducts();
    }

    @PostMapping("/add")
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().toString());
        return ResponseEntity.created(uri).body(productService.create(product));
    }

    @GetMapping("/{page}/{rows}")
    public List<Product> findAllBySales(@PathVariable("page") int page, @PathVariable("rows") int rows){

        Pageable pageable = PageRequest.of(page, rows, Sort.by("id"));
        return productRepository.findAll(pageable).getContent();
    }

    @GetMapping("/gettype")
    public List<Product> findProductByType(@RequestParam("type") String type) {
        return productService.findProductsByType(type);
    }
    
    @GetMapping("/getsalses")
    public List<Product> getAllBySales() {
        return productService.findProductsBySalses();
    }
    
}
