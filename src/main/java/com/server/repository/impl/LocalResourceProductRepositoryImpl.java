package com.server.repository.impl;

import com.server.domain.Product;
import com.server.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class LocalResourceProductRepositoryImpl implements ProductRepository {

    final static private Logger logger = LoggerFactory.getLogger(LocalResourceProductRepositoryImpl.class);

    @Value("classpath:product.csv")
    private Resource productCsvResource;

    final static private CellProcessor[] PRODUCT_PROCESSORS = new CellProcessor[] {
            new NotNull(), // productId
            new NotNull() // productName
    };

    private Map<String, Product> productsMap;

    @PostConstruct
    public void init() throws IOException {
        productsMap = loadProductMapFromResourceCsv(productCsvResource);
    }

    private static Map<String, Product> loadProductMapFromResourceCsv(Resource csvResource) throws IOException {
        Map<String, Product> productsMap  = new HashMap<>();

        try (
                ICsvBeanReader beanReader = new CsvBeanReader(new FileReader(csvResource.getFile()), CsvPreference.STANDARD_PREFERENCE);
        ) {

            final String[] header = beanReader.getHeader(true);

            Product product;
            while( (product = beanReader.read(Product.class, header, PRODUCT_PROCESSORS)) != null ) {
                productsMap.put(product.getProductId(), product);
            }
        }

        logger.info(String.format("Loaded %d Product objects from %s", productsMap.size(), csvResource.getFilename()));
        return productsMap;
    }

    @Override
    public Optional<Product> findById(String productId) {
        return Optional.ofNullable(productsMap.get(productId));
    }
}
