package com.server.repository;

import com.server.domain.Product;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository { //extends JpaRepository<Product, String> {

    Optional<Product> findById(String productId);

}
