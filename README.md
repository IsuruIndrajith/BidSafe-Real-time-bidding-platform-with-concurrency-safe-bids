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
│   REST Clients (Postman / curl)  ·  WebSocket Clients          │
└───────────────────────┬─────────────────────┬───────────────────┘
                        │ HTTP/JWT             │ WS/STOMP
┌───────────────────────▼─────────────────────▼───────────────────┐
│                   SPRING BOOT APPLICATION                        │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────────┐   │
│  │ Auth         │  │ Bid          │  │ WebSocket           │   │
│  │ Controller   │  │ Controller   │  │ Controller          │   │
│  └──────┬───────┘  └──────┬───────┘  └─────────────────────┘   │
│         │                 │                                      │
│  ┌──────▼─────────────────▼────────────────────────────────┐   │
│  │                   SERVICE LAYER                          │   │
│  │  AuthService · BidService · RedisLockService            │   │
│  │  RateLimitService (Bucket4j) · BidSafeMetrics           │   │
│  └──────┬───────────────────────┬────────────────────────┬─┘   │
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

![Docker Hub — published BidSafe image](../SS/docker%20hub%20-%20uploaded%20image%20details%20-%20bidsafe_latest.png)
*The BidSafe image published to Docker Hub*

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

![Containers running in the same Docker network](../SS/recreate%20the%20containers%20in%20the%20same%20docker%20network.png)
*App, MySQL and Redis containers running in the same Docker network*

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

![Logs for user login](../SS/31.%20Logs%20for%20user%20login.png)
*Structured log output captured during user login*

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

![Creating a bid](../SS/1.created%20a%20bid.png)
*A successful bid placed via the REST API*

![Database state after multiple bids](../SS/3.%20data%20base%20after%20multiple%20bids%20are%20made.png)
*MySQL bid table after several bids have been submitted*

**Input validation is enforced on all requests:**

![Validation — zero and negative bid values rejected](../SS/26.%20validated%20bid%20request%20value%20for%200%20and%20negative%20values.png)
*Bid amounts of 0 or negative are rejected with a validation error*

![Global exception handling test](../SS/28.Successfull%20gloabal%20exception%20handling%20test.png)
*Global exception handler returning consistent error responses*

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

![Bid concurrency test with multiple threads](../SS/9.Bid%20concurrency%20test%20with%20multiple%20threads.png)
*JUnit test spawning multiple concurrent threads to bid on the same auction*

![Actual race condition observed](../SS/11.%20actual%20race%20condition.png)
*The race window captured — threads reading the same stale highest-bid value simultaneously*

![Race condition with 5 concurrent threads](../SS/12.%20race%20condition%20demostrated%20for%205%20concurrent%20threads.png)
*Console output showing 5 threads entering the validation check at the same time before any write completes*

---

### 2. Pessimistic Locking (Primary Defense)

`AuctionRepository.findByIdForUpdate()` issues a `SELECT ... FOR UPDATE` that acquires a row-level lock in MySQL. Only one thread processes a bid at a time per auction.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM Auction a WHERE a.id = :id")
Optional<Auction> findByIdForUpdate(@Param("id") Long id);
```

![MySQL locking behavior under concurrent updates](../SS/16.%20MySQL%20database%20also%20has%20its%20own%20locking%20behavior%20while%20processing%20concurrent%20updates..png)
*MySQL's row-level locking visible during simultaneous concurrent update attempts*

![Pessimistic locking test result](../SS/21.Pessimistic%20locking%20test.png)
*Integration test confirming `SELECT FOR UPDATE` serializes all concurrent transactions correctly*

![Concurrency test — accepted vs rejected results](../SS/10.%20placed%20concurrency%20bids%20through%20a%20test%20and%20got%20accepted%20and%20rejected%20numbers.png)
*After enabling pessimistic locking — only the winning bid is accepted; all others are cleanly rejected*

---

### 3. Optimistic Locking (Second Layer)

The `Auction` entity carries a `@Version Long version` field. Hibernate automatically increments this on every update. Any concurrent write with a stale version throws `OptimisticLockingFailureException`.

```java
@Version
private Long version;
```

![Adding @Version to the Auction entity](../SS/13.%20Implemented%20versions%20for%20auction%20model%20for%20optimistic%20locking.png)
*Implementing the `@Version` field on the `Auction` JPA entity*

![Version field set up in the model](../SS/14.%20setup%20versions%20in%20auction%20model.png)
*Version column visible in the Auction model and corresponding database table*

![Race window after adding @Version](../SS/15.race%20window%20after%20the%20versions%20.png)
*The race window narrows — stale-version writes are now detected before they corrupt data*

![Optimistic locking test with version check](../SS/17.%20optimisticLocking%20test%20-%20with%20checking%20version.png)
*Test verifying that `OptimisticLockingFailureException` is thrown on a conflicting concurrent write*

![New version after update](../SS/18.new%20version%20after%20the%20update.png)
*Version counter incremented after each successful auction update*

![OptimisticLocking exception handled](../SS/19.OptimisticLocking%20exception%20handled.png)
*Exception handler returning a structured 409 Conflict response to the client*

---

### 4. Redis Distributed Lock (Cross-Instance Safety)

For horizontally-scaled deployments, a Redis-based distributed lock ensures cross-JVM mutual exclusion. The acquire is atomic (`SET NX PX`) and the release uses a Lua script so only the lock owner can delete it.

```java
// Atomic acquire (SET NX PX)
redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, expiration);

// Safe release via Lua script (only the owner can release)
"if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end"
```

![Redis lock integration test](../SS/redis%20lock%20integration%20test.png)
*Testcontainers-based integration test verifying the distributed lock acquire and release flow*

![Redis lock cannot be released by a random thread](../SS/Redis%20lock%20is%20not%20released%20by%20a%20random%20thread.png)
*The Lua script prevents any thread other than the lock owner from releasing it — a critical safety guarantee*

---

### 5. Idempotency

Every bid request requires a client-supplied `Idempotency-Key` header. The server stores a mapping of `key → bidId`. A duplicate request returns the original response without re-processing — essential for network retry safety.

![Same bid submitted twice with the same key](../SS/22.%20idempotency%20key%20for%20the%20same%20request%20sent%20twice.png)
*Two identical POST requests sent with the same `Idempotency-Key` header*

![Second request deduplicated — no double processing](../SS/23.%20The%20request%20has%20sent%20twice%2C%20but%20for%20the%20same%20idempotency%20the%20same%20is%20not%20happening%20twice.png)
*The server returns the cached result — the bid is NOT processed twice*

![New idempotency key creates a fresh bid](../SS/24.%20New%20Idempotency%20key.png)
*A new unique key correctly triggers a fresh bid request*

![New bid record persisted for new key](../SS/25.%20New%20bid%20record%20with%20new%20idempotency%20key%20.png)
*Database record confirming the new bid was persisted under the new key*

---

### 6. Rate Limiting with Bucket4j

```java
Bandwidth limit = Bandwidth.builder()
    .capacity(4)
    .refillGreedy(4, Duration.ofSeconds(4))
    .build();
```

4 tokens per user, refilled every 4 seconds. Excess requests receive a `429 Too Many Requests` response.

![Rate limiting test — 20 requests, 4 allowed per 4 seconds](../SS/rate%20limiting%20test%20for%20bidsafe%20create%20bids%20-%20with%2020%20iterations%20-%204%20request%20capacity%20for%204%20seconds.png)
*20-request flood test showing throttling kicks in after the 4th consecutive request per user*

---

## ⚡ WebSocket Real-Time Bid Updates

When a bid is accepted, BidSafe immediately broadcasts the update to all subscribers via STOMP over WebSocket. Open `websocket-test.html` from the repo root in a browser to test this interactively.

![WebSocket message broker running](../SS/4.0%20message%20broker%20runnning.png)
*The STOMP message broker starting up successfully inside Spring Boot*

![Connected and subscribed to auction topic](../SS/5.connected%20subscribed%20to%20the%20topic%20auction%201.png)
*Client connected to WebSocket and subscribed to `/topic/auctions/1`*

![Waiting for socket delivery after bid update](../SS/6.After%20connection%20updating%20a%20bid%2C%20waiting%20for%20socket%20delivery.png)
*A bid is placed via REST; the client waits for the WebSocket push*

![WebSocket updates delivered to both users](../SS/7.websocket%20delivered%20for%20bids%20made%20by%202%20users.png)
*Real-time bid updates delivered to two simultaneously connected users*

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

![Actuator health endpoints](../SS/34%20actuator%20health%20end%20points.png)
*Spring Actuator `/health` showing MySQL, Redis, and disk status*

![Application startup time](../SS/39.Application%20startup%20time.png)
*Actuator startup timing metrics*

![JVM memory used](../SS/36.%20JVM%20memory%20used.png)
*`jvm_memory_used_bytes` queried from Actuator and Grafana*

![HTTP request info from Actuator](../SS/38%20HTTP%20info%20about%20the%20requests.png)
*HTTP request counts and response time data exposed by Actuator*

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

![Custom BidSafe metrics via Actuator](../SS/Custom%20bidsafe%20metrics.png)
*Custom `bids.received`, `bids.accepted`, `bids.rejected` counters exposed through the Actuator endpoint*

![Metrics in Prometheus scrape format](../SS/metrics%20exposed%20in%20the%20prometheus%20format%20for%20prometheus%20scrapping.png)
*Raw `/actuator/prometheus` output in Prometheus text format, ready for scraping*

![Custom metrics visible in Prometheus](../SS/Custom%20metrics%20from%20prometheus.png)
*BidSafe custom domain metrics appearing in the Prometheus UI*

![PromQL query for BidSafe metrics](../SS/PromQL%20query%20for%20bidsafe.png)
*Writing PromQL to query real-time bid throughput and rejection rates*

### Grafana Dashboards

![Grafana main dashboard](../SS/Grafana%20dashboard.png)
*Full Grafana monitoring dashboard showing JVM, HTTP, and custom bid metrics*

![Grafana dashboard with custom metric graphs](../SS/grafana%20dashboard%20with%20custom%20metric%20graphs.png)
*Custom Grafana panels visualizing bid accept/reject rates and Redis lock counters*

![Bid accepted vs rejected graph](../SS/bid%20accepted%20vsrejected%20graph.png)
*Time-series graph comparing accepted and rejected bid counts in real time*

![Bid rejection ratio panel](../SS/Bid%20rejection%20ratio.png)
*Derived metric panel showing the live bid rejection percentage*

![Bid receiving rate](../SS/bid%20recieving%20rate.png)
*Real-time bid ingestion rate (bids per second)*

![HTTP requests Prometheus monitoring](../SS/http_requests%20prometheus%20monitoring.png)
*Per-endpoint HTTP request counters scraped by Prometheus*

### Structured Logging

![Logs for user login and registration](../SS/31.%20Logs%20for%20user%20login.png)
*Structured log entries for user authentication events*

![Logs for auction creation](../SS/32.%20Logs%20for%20auction%20creation.png)
*Structured log entries for auction lifecycle events*

![Full lifecycle logs — register, login, bid, lock acquire](../SS/33.logs%20for%20user%20register%2C%20login%2C%20auction%20creation%2C%20bid%20creation%20(fail%20%26%20pass)%20lock%20acquire%20for%20concurrency%20protection.png)
*End-to-end log trace: user register → login → auction create → bid (accepted & rejected) → Redis lock acquire*

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
