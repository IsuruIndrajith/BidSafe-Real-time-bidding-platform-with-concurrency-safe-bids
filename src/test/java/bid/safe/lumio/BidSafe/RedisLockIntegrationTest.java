package bid.safe.lumio.BidSafe;

import bid.safe.lumio.BidSafe.metrics.BidSafeMetrics;
import bid.safe.lumio.BidSafe.service.RedisLockService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisLockIntegrationTest {

    private StringRedisTemplate redisTemplate;
    private RedisLockService redisLockService;

    @BeforeEach
    void setUp() {
        RedisStandaloneConfiguration config =
                new RedisStandaloneConfiguration("localhost", 6379);

        LettuceConnectionFactory connectionFactory =
                new LettuceConnectionFactory(config);
        connectionFactory.afterPropertiesSet();

        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisLockService = new RedisLockService(redisTemplate, new BidSafeMetrics(new SimpleMeterRegistry()));

        redisTemplate.delete("auction:lock:integration-test");
        redisTemplate.delete("auction:lock:owner-check");
    }

    @AfterEach
    void tearDown() {
        redisTemplate.delete("auction:lock:integration-test");
        redisTemplate.delete("auction:lock:owner-check");
    }

    @Test
    void shouldAcquireAndReleaseLockWithMatchingOwnerToken() {
        String lockKey = "auction:lock:integration-test";
        String lockValue = UUID.randomUUID().toString();

        assertTrue(redisLockService.tryLock(lockKey, lockValue, Duration.ofSeconds(5)));
        assertNotNull(redisTemplate.opsForValue().get(lockKey));
        assertEquals(lockValue, redisTemplate.opsForValue().get(lockKey));

        assertTrue(redisLockService.unlock(lockKey, lockValue));
        assertNull(redisTemplate.opsForValue().get(lockKey));
    }

    @Test
    void shouldRejectUnlockAttemptFromDifferentOwner() {
        String lockKey = "auction:lock:owner-check";
        String ownerOne = UUID.randomUUID().toString();
        String ownerTwo = UUID.randomUUID().toString();

        assertTrue(redisLockService.tryLock(lockKey, ownerOne, Duration.ofSeconds(5)));
        assertFalse(redisLockService.unlock(lockKey, ownerTwo));
        assertEquals(ownerOne, redisTemplate.opsForValue().get(lockKey));

        assertTrue(redisLockService.unlock(lockKey, ownerOne));
        assertNull(redisTemplate.opsForValue().get(lockKey));
    }
}
