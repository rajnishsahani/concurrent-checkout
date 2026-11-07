package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int stock;

    @Version
    private long version;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public int getStock() { return stock; }
    public void setStock(int s) { this.stock = s; }
}

/*
Product.java is a row in the products table: an id, a name, and a stock count. This is the thing many buyers are about to fight over. 
Right now it has no @Version field, and that absence is deliberate — it’s the hole the bug lives in.
*/




/*

these are all the services mentioned here as of now.

The data — what gets stored

Product.java is a row in the products table: an id, a name, and a stock count. This is the thing many buyers are about to fight over. Right now it has no @Version field, and that absence is deliberate — it’s the hole the bug lives in.

Order.java is a row in the orders table, recording that someone successfully bought a product. It’s annotated @Table(name="orders") because order is a reserved SQL keyword — name the table order and Postgres rejects your query with a syntax error. A trap you’ve already sidestepped.

The data access — how you read and write those rows

ProductRepository.java and OrderRepository.java are empty interfaces, and that emptiness is the point. You extend JpaRepository<Product, Long> and Spring Data generates the implementation at runtime — save, findById, count, delete, all of it, for free. You declare the capability; the framework writes the SQL. That’s why there’s no body to write.

The business logic — the rules of a checkout

OrderTxService.java is the heart of it, and the only file doing real work. placeOrder does read-check-decrement: load the product, check stock is above zero, subtract one, save, record the order. The whole method is wrapped in @Transactional, so all of it commits together or not at all. That innocent-looking read-then-write sequence is exactly where the race condition will tear itself apart under concurrency — hold that thought for the test.

CheckoutService.java is the entry point callers actually hit. Today it just forwards straight to OrderTxService. It exists as a separate bean for a specific reason: @Transactional works through a Spring proxy, and a method calling another method inside the same class skips the proxy entirely — the transaction would silently never apply. Keeping the transactional method in its own bean sidesteps that. It also gives you a clean home for the retry loop in Phase 3, so you won’t have to restructure anything.

OutOfStockException.java is a small custom exception so “sold out” is a distinct, catchable signal rather than a generic error — the difference between a normal business outcome and an actual bug.

The plumbing

DemoApplication.java is the generated entry point — the main that boots Spring. You won’t touch it today.

The mental model: Repositories talk to the database, OrderTxService holds the rules, CheckoutService is the front door, the two entities are the nouns, the exception is a labeled failure. Clean separation, and every layer has exactly one job.
*/