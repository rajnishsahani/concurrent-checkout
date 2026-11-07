package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.entity.Product;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderTxService {
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;
    OrderTxService(ProductRepository p, OrderRepository o) { this.productRepo = p; this.orderRepo = o; }

    @Transactional
    public Order placeOrder(Long productId) {
        Product p = productRepo.findById(productId)
                .orElseThrow(() -> new OutOfStockException("no product"));
        if (p.getStock() <= 0) throw new OutOfStockException("out of stock");
        p.setStock(p.getStock() - 1);          // read-check-decrement: the race lives here
        productRepo.save(p);
        return orderRepo.save(new Order(productId));
    }
}

/*OrderTxService.java is the heart of it, and the only file doing real work. placeOrder does 
read-check-decrement: load the product, check stock is above zero, subtract one, save, record the order. 
The whole method is wrapped in @Transactional, so all of it commits together or not at all. 
That innocent-looking read-then-write sequence is exactly where the race condition will tear itself apart under concurrency — hold that thought for the test. 
*/