package io.hyozen.redis.api;

import io.hyozen.redis.cache.RedisCache;
import io.hyozen.redis.config.RedisConfig;
import io.hyozen.redis.connection.RedisConnection;
import io.hyozen.redis.handler.RedisChannelRegistrar;

/**
 * Main entry point for the Redis API: lifecycle, pub/sub, and cache.
 * <p>
 * Typical usage: call {@link #connect(RedisConfig)}, then {@link #registerChannelHandler(RedisChannelRegistrar)}
 * for each set of channel handlers, then use {@link #publish(String, String)} and {@link #getCache()} or
 * {@link #createCache(String, Long)}. On shutdown, call {@link #disconnect()}.
 * </p>
 * <p>
 * After {@link #disconnect()}, {@link #publish} and {@link #registerChannelHandler} throw
 * {@link IllegalStateException}. {@link #getCache()} and {@link #createCache} also throw when not connected.
 * </p>
 */
public interface RedisService {

    /**
     * Connects to Redis and starts the subscriber for the configured channel patterns.
     *
     * @param config connection and subscription configuration (must pass validation)
     */
    void connect(RedisConfig config);

    /**
     * Disconnects from Redis and releases all resources (subscriber, executors, pool).
     * Idempotent: safe to call multiple times.
     */
    void disconnect();

    /**
     * Publishes a message to a channel (asynchronous).
     *
     * @param channel the channel name
     * @param message the message payload
     * @throws IllegalStateException if not connected
     */
    void publish(String channel, String message);

    /**
     * Registers handlers for pub/sub channels. Call after {@link #connect(RedisConfig)}.
     *
     * @param registrar callback that fills channel-to-handler maps
     * @throws IllegalStateException if not connected
     */
    void registerChannelHandler(RedisChannelRegistrar registrar);

    /**
     * Returns whether the service is currently connected to Redis.
     */
    boolean isConnected();

    /**
     * Advanced use only. Returns the low-level Redis connection.
     * The returned connection's {@link RedisConnection#getJedis()} returns a resource that
     * <strong>must be closed by the caller</strong> (e.g. try-with-resources). Do not disconnect
     * the connection yourself; use {@link #disconnect()} on this service.
     *
     * @return the connection, or null if not connected
     */
    RedisConnection getConnection();

    /**
     * Returns a cache view with no key prefix and no default TTL. Same instance is returned on each call.
     *
     * @throws IllegalStateException if not connected
     */
    RedisCache getCache();

    /**
     * Creates a cache view with the given key prefix and optional default TTL (seconds).
     *
     * @param prefix            key prefix (namespace), e.g. "messaging:session:"
     * @param defaultTtlSeconds default TTL in seconds for set operations, or null for no expiry
     * @return a new cache instance
     * @throws IllegalStateException if not connected
     */
    RedisCache createCache(String prefix, Long defaultTtlSeconds);
}
