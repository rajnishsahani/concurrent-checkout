package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.demo.entity.Product;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.CheckoutService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class CheckoutConcurrencyTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired ProductRepository productRepo;
    @Autowired OrderRepository orderRepo;
    @Autowired CheckoutService checkoutService;

    @Test
    void neverOversellsUnderConcurrency() throws InterruptedException {
        Product p = new Product();
        p.setName("Widget");
        p.setStock(10);                       // sirf 10 units hain
        Long productId = productRepo.save(p).getId();

        int threads = 50;                     // 50 log 10 units ke liye ladte hain
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    start.await();            // sab wait karte hain...
                    checkoutService.checkout(productId);
                    success.incrementAndGet();
                } catch (Exception ignored) {}
            });
        }
        start.countDown();                    // ...phir sab ek saath fire
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);

        int sold = success.get();
        long ordersCreated = orderRepo.count();
        int stockLeft = productRepo.findById(productId).orElseThrow().getStock();

        System.out.printf("sold=%d orders=%d stockLeft=%d%n", sold, ordersCreated, stockLeft);

        assertThat(sold).isEqualTo(10);
        assertThat(ordersCreated).isEqualTo(10);
        assertThat(stockLeft).isEqualTo(0);
    }
}