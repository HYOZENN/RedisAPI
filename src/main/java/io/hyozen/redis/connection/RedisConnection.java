package io.hyozen.redis.connection;

import io.hyozen.redis.config.RedisConfig;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RedisConnection {

    private static final int SHUTDOWN_TIMEOUT_SECONDS = 10;

    private final RedisConfig config;
    private final Logger logger;
    private volatile JedisPool jedisPool;
    private final List<ExecutorService> executorServices = new ArrayList<>();
    private final AtomicInteger subscriberIndex = new AtomicInteger(0);
    private final ExecutorService publisherService = Executors.newSingleThreadExecutor(
            r -> {
                Thread t = new Thread(r, "redisapi-publisher");
                t.setDaemon(false);
                return t;
            });

    public RedisConnection(RedisConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    private Jedis createJedis() {
        Jedis jedis = new Jedis(config.getHost(), config.getPort());
        if (config.getPassword() != null && !config.getPassword().isEmpty()) {
            jedis.auth(config.getPassword());
        }
        return jedis;
    }

    public void connect() {
        if (jedisPool != null) {
            jedisPool.close();
        }
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(config.getPoolSize());
        jedisPool = new JedisPool(
                poolConfig,
                config.getHost(),
                config.getPort(),
                config.getTimeoutMs(),
                config.getPassword()
        );
        try (Jedis jedis = jedisPool.getResource()) {
            // validate connection
        }
    }

    public void shutdown() {
        executorServices.forEach(ExecutorService::shutdown);
        publisherService.shutdown();
        for (ExecutorService es : executorServices) {
            try {
                if (!es.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    es.shutdownNow();
                    es.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                es.shutdownNow();
            }
        }
        try {
            if (!publisherService.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                publisherService.shutdownNow();
                publisherService.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            publisherService.shutdownNow();
        }
        disconnect();
    }

    public void disconnect() {
        if (jedisPool != null) {
            try {
                jedisPool.close();
            } catch (Exception e) {
                logger.error("Error closing Redis connection", e);
            } finally {
                jedisPool = null;
            }
        }
    }

    public boolean isConnected() {
        return jedisPool != null && !jedisPool.isClosed();
    }

    public void psubscribe(JedisPubSub sub, String... patterns) {
        int index = subscriberIndex.getAndIncrement();
        ExecutorService executorService = Executors.newSingleThreadExecutor(
                r -> {
                    Thread t = new Thread(r, "redisapi-subscriber-" + index);
                    t.setDaemon(false);
                    return t;
                });
        executorServices.add(executorService);
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try (Jedis jedis = createJedis()) {
                    jedis.psubscribe(sub, patterns);
                } catch (Exception e) {
                    logger.error("Redis subscribe error, retrying in 1s...", e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
    }

    public void publish(String channel, String message) {
        publisherService.submit(() -> {
            JedisPool pool = jedisPool;
            if (pool == null) {
                logger.debug("Publish skipped: not connected");
                return;
            }
            try (Jedis jedis = pool.getResource()) {
                if (config.getPassword() != null && !config.getPassword().isEmpty()) {
                    jedis.auth(config.getPassword());
                }
                jedis.publish(channel, message);
            } catch (Exception e) {
                logger.error("Redis publish error: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * Returns a pooled Jedis instance. The caller must close it (e.g. try-with-resources).
     * @throws IllegalStateException if not connected
     */
    public Jedis getJedis() {
        JedisPool pool = jedisPool;
        if (pool == null) {
            throw new IllegalStateException("Not connected");
        }
        Jedis jedis = pool.getResource();
        if (config.getPassword() != null && !config.getPassword().isEmpty()) {
            jedis.auth(config.getPassword());
        }
        return jedis;
    }

    public RedisConfig getConfig() {
        return config;
    }
}
