package bid.safe.lumio.BidSafe;

import bid.safe.lumio.BidSafe.metrics.BidSafeMetrics;
import bid.safe.lumio.BidSafe.service.RedisLockService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BidSafeMetricsTest {

    @Test
    void redisLockServiceEmitsMetricsForAcquiredAndFailedLocks() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        BidSafeMetrics metrics = new BidSafeMetrics(meterRegistry);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent("lock:1", "owner", Duration.ofSeconds(5)))
                .thenReturn(Boolean.TRUE);
        when(valueOperations.setIfAbsent("lock:2", "owner", Duration.ofSeconds(5)))
                .thenReturn(Boolean.FALSE);

        RedisLockService service = new RedisLockService(redisTemplate, metrics);

        assertTrue(service.tryLock("lock:1", "owner", Duration.ofSeconds(5)));
        assertEquals(1d, meterRegistry.find("redis.locks.acquired").counter().count());

        assertFalse(service.tryLock("lock:2", "owner", Duration.ofSeconds(5)));
        assertEquals(1d, meterRegistry.find("redis.locks.failed").counter().count());
    }
}
