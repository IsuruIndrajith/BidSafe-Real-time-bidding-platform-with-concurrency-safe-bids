package bid.safe.lumio.BidSafe.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BidSafeMetrics {

    private final Counter bidsReceived;
    private final Counter bidsAccepted;
    private final Counter bidsRejected;

    private final Counter auctionsCreated;

    private final Counter redisLocksAcquired;
    private final Counter redisLocksFailed;

    public BidSafeMetrics(MeterRegistry meterRegistry) {

        this.bidsReceived = Counter.builder("bids.received")
                .description("Number of bid requests received")
                .register(meterRegistry);

        this.bidsAccepted = Counter.builder("bids.accepted")
                .description("Number of bids accepted")
                .register(meterRegistry);

        this.bidsRejected = Counter.builder("bids.rejected")
                .description("Number of bids rejected")
                .register(meterRegistry);

        this.auctionsCreated = Counter.builder("auctions.created")
                .description("Number of auctions created")
                .register(meterRegistry);

        this.redisLocksAcquired = Counter.builder("redis.locks.acquired")
                .description("Number of Redis locks successfully acquired")
                .register(meterRegistry);

        this.redisLocksFailed = Counter.builder("redis.locks.failed")
                .description("Number of Redis lock acquisition failures")
                .register(meterRegistry);
    }

    public void incrementBidsReceived() {
        bidsReceived.increment();
    }

    public void incrementBidsAccepted() {
        bidsAccepted.increment();
    }

    public void incrementBidsRejected() {
        bidsRejected.increment();
    }

    public void incrementAuctionsCreated() {
        auctionsCreated.increment();
    }

    public void incrementRedisLocksAcquired() {
        redisLocksAcquired.increment();
    }

    public void incrementRedisLocksFailed() {
        redisLocksFailed.increment();
    }
}