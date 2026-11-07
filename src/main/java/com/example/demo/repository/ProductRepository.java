package com.example.demo.repository;

import com.example.demo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {}

/* The data access — how you read and write those rows

ProductRepository.java and OrderRepository.java are empty interfaces, and that emptiness is the point. 
You extend JpaRepository<Product, Long> and Spring Data generates the implementation at runtime — save, findById, count, delete, all of it, for free.
You declare the capability; the framework writes the SQL. That’s why there’s no body to write.

 */