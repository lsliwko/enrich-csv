package com.server.service.mapper;

import com.server.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProductNameEnrichFieldMapper implements EnrichFieldMapper {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public String getInputFieldName() {
        return "product_id";
    }

    @Override
    public String getOutputFieldName() {
        return "product_name";
    }

    @Override
    public String defaultValue() {
        return "Missing Product Name";
    }

    @Override
    public Optional<String> findOutputValueById(String productId) {
        return productRepository.findById(productId).map(product -> product.getProductName());
    }

}
