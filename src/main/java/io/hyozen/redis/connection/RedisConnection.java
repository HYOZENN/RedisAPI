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

public class RedisConnection {

    private final RedisConfig config;
    private final Logger logger;
    private JedisPool jedisPool;
    private final List<ExecutorService> executorServices = new ArrayList<>();
    private final ExecutorService publisherService = Executors.newSingleThreadExecutor();

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
        ExecutorService executorService = Executors.newSingleThreadExecutor();
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
            try (Jedis jedis = jedisPool.getResource()) {
                if (config.getPassword() != null && !config.getPassword().isEmpty()) {
                    jedis.auth(config.getPassword());
                }
                jedis.publish(channel, message);
            } catch (Exception e) {
                logger.error("Redis publish error: {}", e.getMessage(), e);
            }
        });
    }

    public Jedis getJedis() {
        Jedis jedis = jedisPool.getResource();
        if (config.getPassword() != null && !config.getPassword().isEmpty()) {
            jedis.auth(config.getPassword());
        }
        return jedis;
    }

    public RedisConfig getConfig() {
        return config;
    }
}
