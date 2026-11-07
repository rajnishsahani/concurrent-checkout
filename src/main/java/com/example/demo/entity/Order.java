package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;

    protected Order() {}
    public Order(Long productId) { this.productId = productId; }
    public Long getId() { return id; }
    public Long getProductId() { return productId; }
}
/*
Order.java is a row in the orders table, recording that someone successfully bought a product.
 It’s annotated @Table(name="orders") because order is a reserved SQL keyword — name the table order and Postgres rejects your query with a syntax error.
 A trap you’ve already sidestepped.
*/