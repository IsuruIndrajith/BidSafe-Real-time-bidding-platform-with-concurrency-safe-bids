package bid.safe.lumio.BidSafe.service;

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

    public RedisLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryLock(String lockKey, String lockValue, Duration expiration) {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, expiration);
        return Boolean.TRUE.equals(acquired);
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
