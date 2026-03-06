package io.hyozen.redis.cache;

import io.hyozen.redis.connection.RedisConnection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.Optional;

/**
 * Redis cache implementation using RedisConnection. Keys are prefixed with namespace if set.
 */
public class RedisCacheImpl implements RedisCache {

    private final RedisConnection connection;
    private final String keyPrefix;
    private final Long defaultTtlSeconds;

    public RedisCacheImpl(RedisConnection connection, String keyPrefix, Long defaultTtlSeconds) {
        this.connection = connection;
        this.keyPrefix = keyPrefix != null ? keyPrefix : "";
        this.defaultTtlSeconds = defaultTtlSeconds;
    }

    private String fullKey(String key) {
        return keyPrefix + key;
    }

    @Override
    public Optional<String> get(String key) {
        try (Jedis jedis = connection.getJedis()) {
            String value = jedis.get(fullKey(key));
            return Optional.ofNullable(value);
        }
    }

    @Override
    public void set(String key, String value, Long ttlSeconds) {
        try (Jedis jedis = connection.getJedis()) {
            String full = fullKey(key);
            jedis.set(full, value);
            long ttl = ttlSeconds != null && ttlSeconds > 0 ? ttlSeconds : (defaultTtlSeconds != null ? defaultTtlSeconds : 0);
            if (ttl > 0) {
                jedis.expire(full, (int) ttl);
            }
        }
    }

    @Override
    public void set(String key, String value) {
        set(key, value, defaultTtlSeconds);
    }

    @Override
    public void delete(String key) {
        try (Jedis jedis = connection.getJedis()) {
            jedis.del(fullKey(key));
        }
    }

    @Override
    public boolean exists(String key) {
        try (Jedis jedis = connection.getJedis()) {
            return jedis.exists(fullKey(key));
        }
    }

    @Override
    public void deleteByPattern(String pattern) {
        String fullPattern = keyPrefix.isEmpty() ? pattern : keyPrefix + pattern;
        try (Jedis jedis = connection.getJedis()) {
            ScanParams params = new ScanParams().match(fullPattern).count(100);
            String cursor = ScanParams.SCAN_POINTER_START;
            do {
                ScanResult<String> result = jedis.scan(cursor, params);
                cursor = result.getCursor();
                for (String k : result.getResult()) {
                    jedis.del(k);
                }
            } while (!ScanParams.SCAN_POINTER_START.equals(cursor));
        }
    }
}
