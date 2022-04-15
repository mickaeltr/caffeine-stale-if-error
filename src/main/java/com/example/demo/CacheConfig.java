package com.example.demo;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

    private static final Duration CACHE_DURATION = Duration.ofSeconds(1);

    @Bean
    @Override
    public CacheManager cacheManager() {
        var cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder().refreshAfterWrite(CACHE_DURATION));
        cacheManager.setCacheLoader(new CacheLoader<>() {
            @Override
            public Object load(Object key) throws Exception {
                var cacheKey = (CacheKey) key;
                return cacheKey.method.invoke(cacheKey.target, cacheKey.params);
            }

            @Override
            public CompletableFuture<Object> asyncReload(Object key, Object oldValue, Executor executor) {
                try {
                    return CompletableFuture.completedFuture(load(key));
                } catch (Exception e) {
                    return CompletableFuture.failedFuture(e);
                }
            }
        });
        return cacheManager;
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return CacheKey::new;
    }

    private static class CacheKey extends SimpleKey {
        private final Object target;
        private final Method method;
        private final Object[] params;

        private CacheKey(Object target, Method method, Object... params) {
            super(params);
            this.target = target;
            this.method = method;
            this.params = params;
        }
    }
}
