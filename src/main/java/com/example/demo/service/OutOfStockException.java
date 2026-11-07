package com.example.demo.service;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String m) { super(m); }
}

/*
OutOfStockException.java is a small custom exception so “sold out” is a distinct, 
catchable signal rather than a generic error — the difference between a normal business outcome and an actual bug.
*/ 