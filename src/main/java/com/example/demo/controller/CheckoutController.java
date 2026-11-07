package com.example.demo.controller;

import com.example.demo.entity.Order;
import com.example.demo.entity.Product;
import com.example.demo.service.CheckoutService;
import com.example.demo.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final ProductService productService;

    CheckoutController(CheckoutService checkoutService, ProductService productService) {
        this.checkoutService = checkoutService;
        this.productService = productService;
    }

    // POST /products?name=Widget&stock=10
    @PostMapping("/products")
    public Product createProduct(@RequestParam String name, @RequestParam int stock) {
        return productService.create(name, stock);
    }

    // GET /products/1
    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.get(id);
    }

    // POST /checkout/1
    @PostMapping("/checkout/{productId}")
    public Order checkout(@PathVariable Long productId) {
        return checkoutService.checkout(productId);
    }
}