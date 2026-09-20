package bid.safe.lumio.BidSafe.service;

import bid.safe.lumio.BidSafe.metrics.BidSafeMetrics;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class RedisLockService {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT =
            new DefaultRedisScript<>(
                    "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                            "return redis.call('del', KEYS[1]) " +
                            "else return 0 end",
                    Long.class
            );

    private final StringRedisTemplate redisTemplate;
    private final BidSafeMetrics bidSafeMetrics;

    public RedisLockService(StringRedisTemplate redisTemplate, BidSafeMetrics bidSafeMetrics) {
        this.redisTemplate = redisTemplate;
        this.bidSafeMetrics = bidSafeMetrics;
    }

    public boolean tryLock(String lockKey, String lockValue, Duration expiration) {
        try {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, expiration);
            if (Boolean.TRUE.equals(acquired)) {
                bidSafeMetrics.incrementRedisLocksAcquired();
                return true;
            }

            bidSafeMetrics.incrementRedisLocksFailed();
            return false;
        } catch (Exception ex) {
            bidSafeMetrics.incrementRedisLocksFailed();
            throw ex;
        }
    }

    public boolean unlock(String lockKey, String lockValue) {
        Long result = redisTemplate.execute(
                UNLOCK_SCRIPT,
                List.of(lockKey),
                lockValue
        );
        return result != null && result == 1L;
    }
}
