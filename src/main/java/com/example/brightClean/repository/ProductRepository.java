package com.example.brightClean.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.example.brightClean.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    public List<Product> findAllByOrderBySalesDesc();

    @Query("SELECT p FROM Product p WHERE p.type = :type")
    public List<Product> findProductByType(@Param("type") String type);

    List<Product> findByNameContainingIgnoreCase(String keyword);

}
