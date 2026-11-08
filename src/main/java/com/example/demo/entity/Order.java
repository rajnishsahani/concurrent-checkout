package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    protected Order() {}
    public Order(Product product) { this.product = product; }
    public Long getId() { return id; }
    public Product getProduct() { return product; }
}
/*
Order.java is a row in the orders table, recording that someone successfully bought a product.
 It’s annotated @Table(name="orders") because order is a reserved SQL keyword — name the table order and Postgres rejects your query with a syntax error.
 A trap you’ve already sidestepped.
*/