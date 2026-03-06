package io.hyozen.redis.cache;

import java.util.Optional;

public interface RedisCache {

    // Gets a value by key (within this cache's namespace if any).
    Optional<String> get(String key);

    // Sets a value with optional TTL in seconds. If ttlSeconds is null, uses default or no expiry.
    void set(String key, String value, Long ttlSeconds);

    // Sets a value with no TTL (or default TTL if configured).
    void set(String key, String value);

    // Deletes a key.
    void delete(String key);

    // Returns true if the key exists.
    boolean exists(String key);

     // Deletes all keys matching the pattern (e.g. "messaging:session:*"). Use with care.
    void deleteByPattern(String pattern);
}
