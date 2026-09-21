<div align="center">

# 🔒 BidSafe

### Real-Time Bidding Platform with Enterprise-Grade Concurrency Safety

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Hub-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://hub.docker.com/repository/docker/isuru071/bidsafe/general)
[![Prometheus](https://img.shields.io/badge/Prometheus-Monitoring-E6522C?style=for-the-badge&logo=prometheus&logoColor=white)](https://prometheus.io/)
[![Grafana](https://img.shields.io/badge/Grafana-Dashboards-F46800?style=for-the-badge&logo=grafana&logoColor=white)](https://grafana.com/)

> **Production-ready auction platform engineered to handle high-throughput concurrent bidding** — demonstrating race condition detection, optimistic & pessimistic locking, distributed Redis locks, idempotency, WebSocket real-time updates, rate limiting, and full observability through Prometheus + Grafana.

</div>

---

## 📌 Why BidSafe?

In every real-world high-stakes auction — whether it is eBay, stock trading platforms, or government e-procurement systems — **dozens of users can submit bids simultaneously in the final seconds**. Without proper concurrency control, multiple bids can read the same "current highest bid," all pass validation, and corrupt the auction state — a classic **race condition**.

BidSafe is engineered to **demonstrate, reproduce, and solve** this problem using the same battle-tested strategies employed in enterprise Java backends:

| Challenge | BidSafe's Solution |
|---|---|
| Race conditions on concurrent bids | Pessimistic DB locking (`SELECT ... FOR UPDATE`) |
| Stale reads across distributed nodes | Optimistic locking (`@Version` on `Auction` entity) |
| Duplicate bid submissions (network retries) | Per-request **Idempotency Keys** |
| API abuse / bid flooding | **Bucket4j** token-bucket rate limiting |
| Real-time bid notifications | **WebSocket / STOMP** message broker |
| Distributed lock across instances | **Redis** distributed locking with Lua scripts |
| Observability at scale | **Micrometer → Prometheus → Grafana** pipeline |

---

## ✨ Key Features

- 🔐 **JWT-secured REST API** — stateless authentication with role-based access
- ⚡ **WebSocket / STOMP** — real-time bid broadcasting to all connected clients
- 🔒 **Pessimistic Locking** — `SELECT FOR UPDATE` ensures only one thread updates the auction at a time
- 🧮 **Optimistic Locking** — `@Version` field detects concurrent updates without blocking reads
- 🗝️ **Redis Distributed Lock** — Lua-script-based atomic acquire/release for cross-instance safety
- 🔁 **Idempotency** — identical bid requests (same `Idempotency-Key` header) are deduplicated safely
- 🚦 **Rate Limiting** — per-user token bucket (4 requests per 4 seconds) via Bucket4j
- 📊 **Custom Metrics** — `bids.received`, `bids.accepted`, `bids.rejected`, `redis.locks.acquired` exposed via Micrometer
- 📈 **Prometheus + Grafana** — full observability stack with custom dashboards
- 🐳 **Docker-ready** — single-command spin-up with Docker Compose
- 🏥 **Spring Actuator** — health, readiness, JVM, and HTTP request metrics

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                             │
│   REST Clients (Postman / curl)  ·  WebSocket Clients           │
└───────────────────────┬─────────────────────┬───────────────────┘
                        │ HTTP/JWT             │ WS/STOMP
┌───────────────────────▼─────────────────────▼───────────────────┐
│                   SPRING BOOT APPLICATION                       │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────────┐    │
│  │ Auth         │  │ Bid          │  │ WebSocket           │    │
│  │ Controller   │  │ Controller   │  │ Controller          │    │
│  └──────┬───────┘  └──────┬───────┘  └─────────────────────┘    │
│         │                 │                                     │
│  ┌──────▼─────────────────▼────────────────────────────────┐    │
│  │                   SERVICE LAYER                         │    │
│  │  AuthService · BidService · RedisLockService            │    │
│  │  RateLimitService (Bucket4j) · BidSafeMetrics           │    │
│  └──────┬───────────────────────┬────────────────────────┬─┘    │
│         │ JPA/Hibernate         │ Redis Template         │      │
└─────────┼───────────────────────┼────────────────────────┼──────┘
          │                       │                        │
 ┌────────▼────────┐   ┌──────────▼──────┐    ┌──────────▼──────┐
 │   MySQL 8.0     │   │   Redis 7       │    │  Prometheus +   │
 │ (Pessimistic /  │   │ (Distributed    │    │  Grafana        │
 │  Optimistic     │   │   Locking)      │    │  (Observability)│
 │  Locking)       │   └─────────────────┘    └─────────────────┘
 └─────────────────┘
```

---

## 🚀 Getting Started

### Option 1 — Docker Hub (Recommended, Zero Config)

The pre-built image is published on Docker Hub. This is the fastest way to run BidSafe.

*The BidSafe image published to Docker Hub*
<img width="1300" height="736" alt="docker hub - uploaded image details - bidsafe_latest" src="https://github.com/user-attachments/assets/4e02dcef-d061-4317-b574-82a6f6d2e950" />


**1. Pull the image**
```bash
docker pull isuru071/bidsafe:latest
```

**2. Create a `compose.yaml`** with the following content:

```yaml
services:
  app:
    image: isuru071/bidsafe:latest
    container_name: bidsafe-app
    environment:
      SERVER_PORT: 8080
      JWT_SECRET: change-me-to-a-long-random-secret-at-least-32-characters
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: bid_safe
      DB_USERNAME: bid_app
      DB_PASSWORD: bid_app_password
      REDIS_HOST: redis
      REDIS_PORT: 6379
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_started
    ports:
      - "8080:8080"
    restart: unless-stopped

  mysql:
    image: mysql:8.0
    container_name: bidsafe-mysql
    environment:
      MYSQL_DATABASE: bid_safe
      MYSQL_USER: bid_app
      MYSQL_PASSWORD: bid_app_password
      MYSQL_ROOT_PASSWORD: rootpassword
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD-SHELL", "mysqladmin ping -h localhost -u root -prootpassword --silent"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 20s
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    container_name: bidsafe-redis
    ports:
      - "6379:6379"
    restart: unless-stopped

volumes:
  mysql_data:
```

**3. Start the stack**
```bash
docker compose up -d
```

*App, MySQL and Redis containers running in the same Docker network*
<img width="1300" height="736" alt="recreate the containers in the same docker network" src="https://github.com/user-attachments/assets/767a52fc-3cb0-4676-ab65-b5b6a86cdcc2" />


**4. Verify it's running**
```bash
curl http://localhost:8080/actuator/health
```

> 🐳 Docker Hub Image: [isuru071/bidsafe](https://hub.docker.com/repository/docker/isuru071/bidsafe/general)

---

### Option 2 — Clone & Build from Source

**Prerequisites**
- Java 17+
- Maven 3.9+
- Docker & Docker Compose

```bash
# 1. Clone the repository
git clone https://github.com/IsuruIndrajith/BidSafe-Real-time-bidding-platform-with-concurrency-safe-bids.git
cd BidSafe

# 2. Build and start everything
docker compose up --build -d

# 3. Verify
curl http://localhost:8080/actuator/health
```

**Or run directly with Maven (requires local MySQL + Redis):**
```bash
./mvnw spring-boot:run
```

---

## 📡 API Reference

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Login and receive JWT token |

**Register:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "email": "alice@example.com", "password": "password123"}'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "alice@example.com", "password": "password123"}'
```

*Structured log output captured during user login*
<img width="1441" height="722" alt="31  Logs for user login" src="https://github.com/user-attachments/assets/96dfc9a3-1699-4fdf-8d6d-e75e5e22bc42" />


### Auctions & Bids

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/auctions` | JWT | Create a new auction |
| `GET`  | `/api/auctions/{id}` | JWT | Get auction details |
| `POST` | `/api/auctions/{auctionId}/bids` | JWT | Place a bid |

**Place a bid (with Idempotency-Key):**
```bash
curl -X POST http://localhost:8080/api/auctions/1/bids \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: unique-key-001" \
  -d '{"amount": 1500.00}'
```

*A successful bid placed via the REST API*
<img width="1057" height="712" alt="1 created a bid" src="https://github.com/user-attachments/assets/2f1b6e0b-0728-43d4-af05-d8f6dd899ee4" />

*MySQL bid table after several bids have been submitted*
<img width="515" height="322" alt="3  data base after multiple bids are made" src="https://github.com/user-attachments/assets/3aa5816c-9ce0-49fe-9fbc-e829f7b2f758" />


**Input validation is enforced on all requests:**

*Bid amounts of 0 or negative are rejected with a validation error*
<img width="1473" height="753" alt="26  validated bid request value for 0 and negative values" src="https://github.com/user-attachments/assets/39632433-afec-47cb-99d7-020ecd99762a" />


*Global exception handler returning consistent error responses*
<img width="773" height="472" alt="28 Successfull gloabal exception handling test" src="https://github.com/user-attachments/assets/dc9fed60-c428-4e9d-b795-c0e74c5ebddd" />


---

## 🧠 Technical Deep Dive — Concurrency Strategies

### 1. The Race Condition Problem

Without locking, concurrent threads can both read `currentHighestBid = 1000`, both pass the "bid must be higher" check, and both write — corrupting the auction state.

```
Thread A reads  highest = 1000  ─┐
Thread B reads  highest = 1000  ─┤── Both pass validation
Thread A writes highest = 1100  ─┤
Thread B writes highest = 1050  ─┘── 1050 < 1100 : DATA CORRUPTION
```

The test below fires multiple threads concurrently at the same auction to reproduce this exact scenario:

*JUnit test spawning multiple concurrent threads to bid on the same auction*
<img width="1456" height="730" alt="9 Bid concurrency test with multiple threads" src="https://github.com/user-attachments/assets/5f860241-8264-4e65-83ca-14f9fec53cdb" />


*The possible race window*
<img width="437" height="311" alt="11  actual race condition" src="https://github.com/user-attachments/assets/c9e9e494-877e-46ac-9121-478f2206063b" />


*Console output showing 5 threads entering the validation check at the same time before any write completes*
<img width="1001" height="552" alt="12  race condition demostrated for 5 concurrent threads" src="https://github.com/user-attachments/assets/093af84f-e75f-43a1-9593-00e2e5009823" />


---

### 2. Pessimistic Locking (Primary Defense)

`AuctionRepository.findByIdForUpdate()` issues a `SELECT ... FOR UPDATE` that acquires a row-level lock in MySQL. Only one thread processes a bid at a time per auction.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM Auction a WHERE a.id = :id")
Optional<Auction> findByIdForUpdate(@Param("id") Long id);
```

*MySQL's row-level locking visible during simultaneous concurrent update attempts*
<img width="1451" height="587" alt="16  MySQL database also has its own locking behavior while processing concurrent updates" src="https://github.com/user-attachments/assets/9a9ba9cf-f73d-4c2c-b0e3-efe75e265838" />


*Integration test confirming `SELECT FOR UPDATE` serializes all concurrent transactions correctly*
<img width="1135" height="673" alt="21 Pessimistic locking test" src="https://github.com/user-attachments/assets/47e967e6-1215-48b6-b30d-76d896030268" />


*After enabling pessimistic locking — only the winning bid is accepted; all others are cleanly rejected*
<img width="957" height="602" alt="10  placed concurrency bids through a test and got accepted and rejected numbers" src="https://github.com/user-attachments/assets/46cb3be1-41f7-4cfc-969c-a0e62718789f" />


---

### 3. Optimistic Locking (Second Layer)

The `Auction` entity carries a `@Version Long version` field. Hibernate automatically increments this on every update. Any concurrent write with a stale version throws `OptimisticLockingFailureException`.

```java
@Version
private Long version;
```

*Implementing the `@Version` field on the `Auction` JPA entity*
<img width="702" height="137" alt="13  Implemented versions for auction model for optimistic locking" src="https://github.com/user-attachments/assets/414dd894-afaf-423d-80c2-0d5a77fc69ac" />


*Version column visible in the Auction model and corresponding database table*
<img width="381" height="90" alt="14  setup versions in auction model" src="https://github.com/user-attachments/assets/1b039c25-4eb9-4307-b189-48edd365c74f" />


*The race window narrows — stale-version writes are now detected before they corrupt data*
<img width="1427" height="587" alt="15 race window after the versions " src="https://github.com/user-attachments/assets/30aa35d7-4373-4617-92f2-e0239d56c33b" />


*Test verifying that `OptimisticLockingFailureException` is thrown on a conflicting concurrent write*
<img width="1460" height="381" alt="17  optimisticLocking test - with checking version" src="https://github.com/user-attachments/assets/aa09a6f6-c42b-45c6-9b7d-86744d23e7b6" />


*Version counter incremented after each successful auction update*
<img width="446" height="277" alt="18 new version after the update" src="https://github.com/user-attachments/assets/8b85686f-3c34-41d4-a4e3-7d54798d91dc" />


*Exception handler returning a structured 409 Conflict response to the client*
<img width="1417" height="732" alt="19 OptimisticLocking exception handled" src="https://github.com/user-attachments/assets/402d07c2-245c-450d-9ab2-bc6bd7a69d3f" />


---

### 4. Redis Distributed Lock (Cross-Instance Safety)

For horizontally-scaled deployments, a Redis-based distributed lock ensures cross-JVM mutual exclusion. The acquire is atomic (`SET NX PX`) and the release uses a Lua script so only the lock owner can delete it.

```java
// Atomic acquire (SET NX PX)
redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, expiration);

// Safe release via Lua script (only the owner can release)
"if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end"
```

*Testcontainers-based integration test verifying the distributed lock acquire and release flow*
<img width="798" height="636" alt="redis lock integration test" src="https://github.com/user-attachments/assets/1e75d1ea-2930-4d0b-a41c-02073f906a68" />


*The Lua script prevents any thread other than the lock owner from releasing it — a critical safety guarantee*
<img width="1300" height="736" alt="Redis lock is not released by a random thread" src="https://github.com/user-attachments/assets/987fa6a9-d8d4-4191-8900-ec7f8539bbec" />


---

### 5. Idempotency

Every bid request requires a client-supplied `Idempotency-Key` header. The server stores a mapping of `key → bidId`. A duplicate request returns the original response without re-processing — essential for network retry safety.

*Two identical POST requests sent with the same `Idempotency-Key` header*
<img width="1046" height="707" alt="22  idempotency key for the same request sent twice" src="https://github.com/user-attachments/assets/46e1b9b2-63f4-4cbf-b4e8-567435e574ca" />


*The server returns the cached result — the bid is NOT processed twice*
<img width="412" height="238" alt="23  The request has sent twice, but for the same idempotency the same is not happening twice" src="https://github.com/user-attachments/assets/7136c484-23c7-419b-86e1-00e6b27e9f6e" />


*A new unique key correctly triggers a fresh bid request*
<img width="847" height="717" alt="24  New Idempotency key" src="https://github.com/user-attachments/assets/de410189-dbeb-4174-8e96-8962dfb08a06" />


*Database record confirming the new bid was persisted under the new key*
<img width="438" height="243" alt="25  New bid record with new idempotency key " src="https://github.com/user-attachments/assets/9dcc3b60-a2bb-4aec-b4cf-dd66cc41f118" />


---

### 6. Rate Limiting with Bucket4j

```java
Bandwidth limit = Bandwidth.builder()
    .capacity(4)
    .refillGreedy(4, Duration.ofSeconds(4))
    .build();
```

4 tokens per user, refilled every 4 seconds. Excess requests receive a `429 Too Many Requests` response.


*20-request flood test showing throttling kicks in after the 4th consecutive request per user*
<img width="1045" height="717" alt="rate limiting test for bidsafe create bids - with 20 iterations - 4 request capacity for 4 seconds" src="https://github.com/user-attachments/assets/1c57d0a1-500a-4f76-a025-05e1334d7d9c" />


---

## ⚡ WebSocket Real-Time Bid Updates

When a bid is accepted, BidSafe immediately broadcasts the update to all subscribers via STOMP over WebSocket. Open `websocket-test.html` from the repo root in a browser to test this interactively.

*Connected to web socket- subscribed to topic*
<img width="702" height="258" alt="1 connected to web socket- subscribed to topic" src="https://github.com/user-attachments/assets/8652b849-a963-41fa-bca0-3a899008d1b4" />

*Bid updated*
<img width="817" height="438" alt="2 Bid updated" src="https://github.com/user-attachments/assets/c3fadd36-5408-45a3-b533-466dd66439da" />

*Bid update recieved from the subscription topic*
<img width="848" height="453" alt="3  Bid update recieved from the subscription topic" src="https://github.com/user-attachments/assets/b30cbe25-53e4-4a29-aa8e-7c1edeb37cea" />

*Bid update recieved from the user 2*
<img width="966" height="461" alt="4  Bid update recieved from the user 2" src="https://github.com/user-attachments/assets/e91d9d90-f84c-433a-89c2-058797063b62" />

*Updated bid table*
<img width="412" height="267" alt="5  updated bid table" src="https://github.com/user-attachments/assets/7e807ea2-0bbb-41a2-9649-fcd1214db9a9" />

*The possible race condition*
<img width="308" height="321" alt="6 The possible race condition" src="https://github.com/user-attachments/assets/b7cedf2a-a572-429d-9b23-5e5356d027d8" />

*The STOMP message broker starting up successfully inside Spring Boot*
<img width="1452" height="267" alt="4 0 message broker runnning" src="https://github.com/user-attachments/assets/211fdc86-55ad-45f1-b736-7752b9ef9ba0" />

*Client connected to WebSocket and subscribed to `/topic/auctions/1`*
<img width="767" height="372" alt="5 connected subscribed to the topic auction 1" src="https://github.com/user-attachments/assets/8c62e0f4-9241-4c2e-ab7b-834e9557aa44" />

*A bid is placed via REST; the client waits for the WebSocket push*
<img width="906" height="370" alt="6 After connection updating a bid, waiting for socket delivery" src="https://github.com/user-attachments/assets/06318fc4-f0bd-4cc5-aa2b-8523cd03538c" />


*Real-time bid updates delivered to two simultaneously connected users*
<img width="1011" height="372" alt="7 websocket delivered for bids made by 2 users" src="https://github.com/user-attachments/assets/19e366ab-add6-4ab5-b658-d452e3f98e2b" />


---

## 🧪 Running the Tests

### Concurrent Bidding (Race Condition Demo)
Simulates multiple threads bidding simultaneously to reproduce and verify the fix for race conditions.
```bash
./mvnw test -Dtest=BidConcurrencyTest
```
Expected: Only **one bid** accepted per auction round when pessimistic locking is active.

### Optimistic Locking
```bash
./mvnw test -Dtest=OptimisticLockingTest
```
Expected: `OptimisticLockingFailureException` thrown for concurrent version conflicts.

### Pessimistic Locking
```bash
./mvnw test -Dtest=PessimisticLockingTest
```

### Redis Distributed Lock
```bash
./mvnw test -Dtest=RedisLockIntegrationTest
```

### Idempotency (same request sent twice)
```bash
# Send the same bid twice with the same Idempotency-Key
curl -X POST http://localhost:8080/api/auctions/1/bids \
  -H "Authorization: Bearer <token>" \
  -H "Idempotency-Key: same-key-for-both-requests" \
  -H "Content-Type: application/json" \
  -d '{"amount": 2000.00}'
```

### Rate Limiting
```bash
# Send 20 rapid requests — only 4 per 4 seconds are allowed per user
for i in {1..20}; do
  curl -X POST http://localhost:8080/api/auctions/1/bids \
    -H "Authorization: Bearer <token>" \
    -H "Idempotency-Key: key-$i" \
    -H "Content-Type: application/json" \
    -d '{"amount": '$((1000 + i * 10))'.00}'
done
```

---

## 📊 Monitoring Stack Setup

BidSafe ships with a full Prometheus + Grafana observability stack.

```bash
# Start the monitoring stack (from the /monitoring directory)
cd monitoring
docker compose up -d
```

| Service | URL | Credentials |
|---------|-----|-------------|
| Spring Actuator | `http://localhost:8080/actuator` | — |
| Prometheus metrics endpoint | `http://localhost:8080/actuator/prometheus` | — |
| Prometheus UI | `http://localhost:9090` | — |
| Grafana Dashboard | `http://localhost:3000` | `admin / admin` |

### Actuator Health & JVM Metrics

*Spring Actuator `/health` showing MySQL, Redis, and disk status*
<img width="753" height="517" alt="34 actuator health end points" src="https://github.com/user-attachments/assets/e06f1d3a-2f79-4738-a13e-0c844eb3329e" />


*Actuator startup timing metrics*
<img width="1050" height="592" alt="39 Application startup time" src="https://github.com/user-attachments/assets/cc1c1305-ea1a-4f3e-af90-7c22563009db" />


*`jvm_memory_used_bytes` queried from Actuator and Grafana*
<img width="1045" height="707" alt="36  JVM memory used" src="https://github.com/user-attachments/assets/29efdbf1-b9ac-4deb-958c-9e5a5abb4e4d" />


*HTTP request counts and response time data exposed by Actuator*
<img width="1037" height="722" alt="38 HTTP info about the requests" src="https://github.com/user-attachments/assets/c85beaf4-1ec7-4d32-ba66-fa9b4c46a3f3" />


### Custom BidSafe Metrics (Micrometer → Prometheus)

BidSafe exposes domain-specific counters via Micrometer:

```promql
# Total bids received
bids_received_total

# Bid acceptance rate over time
rate(bids_accepted_total[1m])

# Bid rejection ratio
bids_rejected_total / bids_received_total

# Redis lock acquisition failures
redis_locks_failed_total

# HTTP request rate
rate(http_server_requests_seconds_count[1m])
```

*Custom `bids.received`, `bids.accepted`, `bids.rejected` counters exposed through the Actuator endpoint*
<img width="1300" height="736" alt="Custom bidsafe metrics" src="https://github.com/user-attachments/assets/63ea3a82-f5e9-4f34-bce3-a793ea54a49e" />


*Raw `/actuator/prometheus` output in Prometheus text format, ready for scraping*
<img width="1300" height="736" alt="metrics exposed in the prometheus format for prometheus scrapping" src="https://github.com/user-attachments/assets/7869e7dc-4c5e-4ec5-ac1e-6f0a4773692f" />


*BidSafe custom domain metrics appearing in the Prometheus UI*
<img width="1300" height="736" alt="Custom metrics from prometheus" src="https://github.com/user-attachments/assets/e6710a76-c8a5-4aed-a30d-4c0da6fbb5e5" />

### Grafana Dashboards

*Full Grafana monitoring dashboard showing JVM, HTTP, and custom bid metrics*
<img width="1300" height="736" alt="Grafana dashboard" src="https://github.com/user-attachments/assets/a61dd8a7-040d-4c65-ad57-ed74843eec02" />


*Custom Grafana panels visualizing bid accept/reject rates and Redis lock counters*
<img width="1300" height="736" alt="grafana dashboard with custom metric graphs" src="https://github.com/user-attachments/assets/c607d9ba-2612-4e60-9439-e6a552362b5d" />
<img width="1300" height="736" alt="grafana dashboard with custom metric graphs2" src="https://github.com/user-attachments/assets/ced4e81d-ec2e-4a51-b2f9-d5f946270776" />


*Time-series graph comparing accepted and rejected bid counts in real time*
<img width="1300" height="736" alt="bid accepted vsrejected graph" src="https://github.com/user-attachments/assets/8a493e13-dbff-42e5-b097-dd4a888af1d6" />


*Derived metric panel showing the live bid rejection percentage*
<img width="1300" height="736" alt="Bid rejection ratio" src="https://github.com/user-attachments/assets/597aaa98-9955-4341-8674-889bfd4b6638" />


*Real-time bid ingestion rate (bids per second)*
<img width="1300" height="736" alt="bid recieving rate" src="https://github.com/user-attachments/assets/d399183e-1513-4bc3-aa26-83f00624bf98" />


*Per-endpoint HTTP request counters scraped by Prometheus*
<img width="1300" height="736" alt="http_requests prometheus monitoring" src="https://github.com/user-attachments/assets/22e50de6-7f9b-4853-9ea9-b17d59de2c37" />


### Structured Logging

*Structured log entries for user authentication events*
<img width="1441" height="722" alt="31  Logs for user login" src="https://github.com/user-attachments/assets/9eb727ec-4c1f-43e4-b091-216dc39856bc" />


*Structured log entries for auction lifecycle events*
<img width="1376" height="687" alt="32  Logs for auction creation" src="https://github.com/user-attachments/assets/911cda97-c00d-4873-92ed-1f5e863bbc20" />


*End-to-end log trace: user register → login → auction create → bid (accepted & rejected) → Redis lock acquire*
<img width="1457" height="712" alt="33 logs for user register, login, auction creation, bid creation (fail   pass) lock acquire for concurrency protection" src="https://github.com/user-attachments/assets/73e5c3d9-828d-4145-80fd-bc419c594f7e" />


---

## 🛠️ Tech Stack

### Core Backend

| Technology | Version | Role |
|---|---|---|
| **Java** | 17 (LTS) | Primary language |
| **Spring Boot** | 4.1.1 | Application framework |
| **Spring MVC** | — | REST API layer |
| **Spring Data JPA** | — | ORM & repository layer |
| **Spring Security** | — | Authentication & Authorization |
| **Spring WebSocket** | — | Real-time STOMP messaging |
| **Spring Actuator** | — | Production health & metrics endpoints |
| **Hibernate** | — | JPA provider, Optimistic/Pessimistic locking |
| **Lombok** | — | Boilerplate reduction |

### Concurrency & Distributed Systems

| Technology | Role |
|---|---|
| **MySQL Pessimistic Locking** | `SELECT FOR UPDATE` — serializes concurrent bid transactions at DB level |
| **JPA `@Version` / Optimistic Locking** | Detects stale writes without blocking reads |
| **Redis (Spring Data Redis)** | Distributed lock via atomic `SET NX PX` + Lua script for safe release |
| **Bucket4j** | Token-bucket rate limiting per user (4 req / 4 sec) |
| **Idempotency Keys** | Deduplicates duplicate bid submissions via `IdempotencyRecord` table |

### Security

| Technology | Role |
|---|---|
| **Spring Security** | Filter chain, CORS, stateless session management |
| **JWT (JJWT 0.12.6)** | Stateless token-based auth |
| **BCrypt** | Password hashing |
| **Jakarta Validation** | Request body validation (`@Valid`, `@NotNull`, `@Positive`) |

### Observability

| Technology | Role |
|---|---|
| **Micrometer** | Metrics facade — custom counters for bids, locks, auctions |
| **Prometheus (micrometer-registry-prometheus)** | Metrics scraping and time-series storage |
| **Grafana** | Dashboard visualization (JVM, HTTP, custom bid metrics) |
| **SLF4J / Logback** | Structured logging for full audit trail |

### Infrastructure & DevOps

| Technology | Role |
|---|---|
| **Docker** | Application containerization |
| **Docker Compose** | Multi-service orchestration (app + MySQL + Redis) |
| **Docker Hub** | Public image registry (`isuru071/bidsafe`) |
| **MySQL 8.0** | Primary relational database |
| **Redis 7 (Alpine)** | In-memory data store for distributed locking |
| **Maven** | Build & dependency management |
| **Testcontainers** | Integration testing with real DB & Redis containers |

---

## 💼 Real-World Industry Relevance

BidSafe directly mirrors the concurrency challenges faced in production systems at scale:

| Industry | Use Case | BidSafe Equivalent |
|---|---|---|
| **E-Commerce** | Flash sale / limited stock checkout | Concurrent bidding with pessimistic locking |
| **FinTech / Banking** | Trade order matching engines | Idempotency + optimistic locking on order state |
| **Telecommunications** | SIM / number reservation systems | Redis distributed lock for cross-node safety |
| **Government / ERP** | e-Procurement auction platforms | Full audit trail, JWT auth, structured logging |
| **Gaming / SaaS** | Leaderboard / ranked-match systems | Rate limiting (Bucket4j) to prevent API abuse |
| **Cloud-Native Microservices** | Any horizontally-scaled service | Redis distributed lock, Docker, actuator health probes |

---

## 📋 Prerequisites Summary

| Requirement | Docker Hub Setup | Source Build |
|---|---|---|
| Docker + Docker Compose | Required | Required |
| Java 17+ | Not needed | Required |
| Maven 3.9+ | Not needed | Required |
| MySQL 8 (local) | Docker handles it | Docker handles it |
| Redis 7 (local) | Docker handles it | Docker handles it |

---

## 📁 Project Structure

```
BidSafe/
├── src/
│   ├── main/
│   │   ├── java/bid/safe/lumio/BidSafe/
│   │   │   ├── config/          # SecurityConfig, WebSocketConfig
│   │   │   ├── controller/      # REST & WebSocket controllers
│   │   │   ├── dto/             # Request/Response DTOs
│   │   │   ├── exception/       # Global exception handlers
│   │   │   ├── metrics/         # BidSafeMetrics (Micrometer counters)
│   │   │   ├── model/           # JPA entities (Auction @Version, Bid, User...)
│   │   │   ├── ratelimit/       # Bucket4j rate limiting
│   │   │   ├── repository/      # Spring Data JPA repositories
│   │   │   ├── security/        # JWT filter + service
│   │   │   └── service/         # BidService, RedisLockService, AuthService
│   │   └── resources/
│   │       └── application.properties
│   └── test/                    # Concurrency, locking & integration tests
├── monitoring/
│   ├── docker-compose.yml       # Prometheus + Grafana stack
│   └── prometheus/
│       └── prometheus.yml       # Scrape config for BidSafe
├── Dockerfile                   # Multi-stage build (Maven build -> JRE runtime)
├── compose.yaml                 # Full app stack (app + MySQL + Redis)
└── websocket-test.html          # Browser-based WebSocket test client
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m 'feat: add your feature'`
4. Push to the branch and open a Pull Request

---

## 📄 License

This project is open source. See the repository for license details.

---

<div align="center">

**Built with ❤️ to demonstrate production-grade Java concurrency engineering**

[![Docker Hub](https://img.shields.io/badge/Docker%20Hub-isuru071%2Fbidsafe-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://hub.docker.com/repository/docker/isuru071/bidsafe/general)

</div>
