package com.example.demo.service;

import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final ProductRepository productRepo;
    ProductService(ProductRepository productRepo) { this.productRepo = productRepo; }

    public Product create(String name, int stock) {
        Product p = new Product();
        p.setName(name);
        p.setStock(stock);
        return productRepo.save(p);
    }

    public Product get(Long id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new OutOfStockException("no product"));
    }
}