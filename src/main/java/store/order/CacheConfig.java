package store.order;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import store.product.ProductOut;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    public static final String PRODUCTS = "products";
    public static final String EXCHANGES = "exchanges";

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Value("${store.cache.products-ttl}")
    private Duration productsTtl;

    @Value("${store.cache.exchanges-ttl}")
    private Duration exchangesTtl;

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return builder -> builder
            .enableStatistics() // exposes cache hits/misses to prometheus
            .withCacheConfiguration(PRODUCTS, config(ProductOut.class, productsTtl))
            .withCacheConfiguration(EXCHANGES, config(ExchangeOut.class, exchangesTtl));
    }

    private <T> RedisCacheConfiguration config(Class<T> type, Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
            .prefixCacheNameWith("order:")
            .entryTtl(ttl)
            .disableCachingNullValues()
            .serializeValuesWith(SerializationPair.fromSerializer(new JacksonJsonRedisSerializer<>(type)));
    }

    // if redis is down, log and fall back to calling the services directly
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {

            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                log.warn("Cache get failed [{}::{}]: {}", cache.getName(), key, e.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                log.warn("Cache put failed [{}::{}]: {}", cache.getName(), key, e.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                log.warn("Cache evict failed [{}::{}]: {}", cache.getName(), key, e.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                log.warn("Cache clear failed [{}]: {}", cache.getName(), e.getMessage());
            }

        };
    }

}
