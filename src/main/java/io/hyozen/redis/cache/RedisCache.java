package io.hyozen.redis.cache;

import java.util.Optional;

/**
 * Redis-backed key/value cache with optional key prefix (namespace) and TTL.
 * <p>
 * All keys are prefixed with the cache's namespace if one was specified at creation.
 * Implementations are thread-safe for concurrent get/set/delete/exists calls.
 * </p>
 */
public interface RedisCache {

    /**
     * Gets a value by key (within this cache's namespace if any).
     *
     * @param key the key (without namespace prefix)
     * @return the value, or empty if absent or expired
     */
    Optional<String> get(String key);

    /**
     * Sets a value with optional TTL in seconds. If ttlSeconds is null, uses default TTL or no expiry.
     *
     * @param key        the key
     * @param value      the value
     * @param ttlSeconds TTL in seconds, or null to use default or no expiry
     */
    void set(String key, String value, Long ttlSeconds);

    /**
     * Sets a value with no explicit TTL (uses default TTL if configured for this cache).
     *
     * @param key   the key
     * @param value the value
     */
    void set(String key, String value);

    /**
     * Deletes a key.
     *
     * @param key the key
     */
    void delete(String key);

    /**
     * Returns true if the key exists.
     *
     * @param key the key
     * @return true if the key exists
     */
    boolean exists(String key);

    /**
     * Deletes all keys matching the pattern (e.g. "messaging:session:*"). Use with care on large key sets.
     *
     * @param pattern glob-style pattern (relative to this cache's prefix if any)
     */
    void deleteByPattern(String pattern);
}
