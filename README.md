# Concurrent Checkout — Preventing Overselling Under Load

A Spring Boot service that demonstrates — and fixes — a classic concurrency bug:
**overselling inventory when many buyers check out at the same time.**

The interesting part isn't the e-commerce domain. It's the correctness guarantee: no
matter how many requests hit the service simultaneously, it will never sell more units
than exist. That property is proven by an automated concurrency test that runs against a
real PostgreSQL database.

## The problem

A checkout does three steps: **read** the stock, **check** it's above zero,
**decrement** it and save. Under concurrent load this breaks. When many requests arrive
at once, dozens of them read the same stock value (say `10`) *before* any of them has
written the decrement back. They all pass the check, they all decrement from their own
stale copy, and the service sells far more than it has.

The bug lives in the gap between the read and the write.

## Reproducing it (before the fix)

The test fires **50 concurrent checkouts** at a product with **10 units** in stock.
With no concurrency control, the result is:

```
sold=50 orders=50 stockLeft=4
```

50 buyers succeeded against 10 units, and the stock count itself is corrupted by threads
overwriting each other's writes — a textbook lost-update.

## The fix — optimistic locking

The `Product` entity carries a `@Version` field. On every update, Hibernate adds the
version to the `WHERE` clause:

```sql
UPDATE products SET stock = ?, version = ? WHERE id = ? AND version = ?
```

If another transaction has already moved the row forward, the version no longer matches,
the update affects zero rows, and Hibernate throws
`ObjectOptimisticLockingFailureException`. The checkout service catches that, re-reads the
now-current stock, and retries. Buyers who retry into a sold-out read fail cleanly with
`OutOfStockException`.

This is **optimistic** concurrency control: it doesn't lock rows up front and queue
everyone — it lets all requests proceed and only catches the collision at write time,
betting conflicts are rare.

After the fix, the same test passes:

```
sold=10 orders=10 stockLeft=0
```

Exactly 10 sold, 10 orders created, stock at zero. No oversell.

## Project structure

The code is organized into standard Spring layers, each with a single responsibility:

```
com.example.demo
├── controller   → REST endpoints (CheckoutController)
├── service      → business logic (CheckoutService, OrderTxService, ProductService)
├── repository   → data-access interfaces (ProductRepository, OrderRepository)
└── entity       → database-mapped classes (Product, Order)
```

A request flows **controller → service → repository → entity → database**: the controller
handles the HTTP request and delegates immediately, the service owns the business logic
(including the retry loop), the repository talks to the database, and the entity maps to a
table row.

## Design notes

- **`OrderTxService`** holds the transactional `placeOrder` (read-check-decrement),
  annotated `@Transactional`.
- **`CheckoutService`** is a separate bean that wraps it in the retry loop. Keeping the
  transactional method in its own bean matters: Spring's `@Transactional` works through a
  proxy, and a method calling another method *in the same class* would bypass that proxy,
  so the transaction would silently never apply.
- Inside the transaction, the stock change persists via Hibernate **dirty checking** —
  the modified entity is flushed on commit automatically.

## API

| Method | Endpoint             | Description                        |
|--------|----------------------|------------------------------------|
| POST   | `/products`          | Create a product (`name`, `stock`) |
| GET    | `/products/{id}`     | Fetch a product                    |
| POST   | `/checkout/{id}`     | Check out one unit of a product    |

## Testing

Integration tests run against a real PostgreSQL instance via **Testcontainers**, not an
in-memory substitute — so the concurrency behaviour tested is the behaviour you get in
production. Requires Docker to be running.

```bash
./mvnw test
```

## Tech stack

Java 21 · Spring Boot · Spring Data JPA / Hibernate · PostgreSQL · Testcontainers ·
JUnit 5 · AssertJ · Maven

## Roadmap

This service gets concurrency correct for a single checkout. Planned next steps:

- Run against a configured datasource and exercise the REST endpoints end to end.
- Return DTOs from the controller instead of exposing entities directly.
- Model `Order → Product` as a JPA `@ManyToOne` relationship.
- Split into separate order, inventory, and payment services coordinated by a **saga**
  with compensating transactions and idempotency keys — the distributed-systems version of
  the same no-oversell guarantee.