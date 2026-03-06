package io.hyozen.redis.config;

import java.util.Collections;
import java.util.List;

public final class RedisConfig {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 6379;
    public static final String DEFAULT_PASSWORD = "";
    public static final int DEFAULT_TIMEOUT_MS = 2000;
    public static final int DEFAULT_POOL_SIZE = 8;
    public static final List<String> DEFAULT_PATTERNS = Collections.singletonList("messaging:*");

    private final String host;
    private final int port;
    private final String password;
    private final int timeoutMs;
    private final int poolSize;
    private final List<String> subscribePatterns;

    private RedisConfig(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.password = builder.password != null ? builder.password : DEFAULT_PASSWORD;
        this.timeoutMs = builder.timeoutMs > 0 ? builder.timeoutMs : DEFAULT_TIMEOUT_MS;
        this.poolSize = builder.poolSize > 0 ? builder.poolSize : DEFAULT_POOL_SIZE;
        this.subscribePatterns = builder.subscribePatterns != null && !builder.subscribePatterns.isEmpty()
                ? List.copyOf(builder.subscribePatterns)
                : DEFAULT_PATTERNS;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public List<String> getSubscribePatterns() {
        return subscribePatterns;
    }

    public String[] getSubscribePatternsArray() {
        return subscribePatterns.toArray(new String[0]);
    }

    public static final class Builder {
        private String host = DEFAULT_HOST;
        private int port = DEFAULT_PORT;
        private String password = DEFAULT_PASSWORD;
        private int timeoutMs = DEFAULT_TIMEOUT_MS;
        private int poolSize = DEFAULT_POOL_SIZE;
        private List<String> subscribePatterns = DEFAULT_PATTERNS;

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder timeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public Builder poolSize(int poolSize) {
            this.poolSize = poolSize;
            return this;
        }

        public Builder subscribePatterns(List<String> subscribePatterns) {
            this.subscribePatterns = subscribePatterns;
            return this;
        }

        public RedisConfig build() {
            return new RedisConfig(this);
        }
    }
}
