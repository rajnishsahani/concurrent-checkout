// package com.example.demo;

// import org.springframework.stereotype.Service;

// // CheckoutService.java — naive version: one shot, no retry.
// @Service
// public class CheckoutService {
//     private final OrderTxService tx;
//     CheckoutService(OrderTxService tx) { this.tx = tx; }

//     public Order checkout(Long productId) {
//         return tx.placeOrder(productId);
//     }
// }


package com.example.demo;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
public class CheckoutService {
    private final OrderTxService tx;
    CheckoutService(OrderTxService tx) { this.tx = tx; }

    public Order checkout(Long productId) {
        for (int attempt = 0; attempt < 50; attempt++) {
            try {
                return tx.placeOrder(productId);
            } catch (ObjectOptimisticLockingFailureException e) {
                // lost the race for this version — loop, re-read fresh, try again
            }
        }
        throw new OutOfStockException("too much contention");
    }
}


/*CheckoutService.java is the entry point callers actually hit. Today it just forwards straight to OrderTxService. 
It exists as a separate bean for a specific reason: @Transactional works through a Spring proxy, 
and a method calling another method inside the same class skips the proxy entirely — the transaction would silently never apply. 
Keeping the transactional method in its own bean sidesteps that. It also gives you a clean home for the retry loop in Phase 3, so you won’t have to restructure anything.
*/