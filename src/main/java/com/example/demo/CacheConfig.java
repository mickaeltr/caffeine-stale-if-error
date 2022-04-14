package com.example.demo;

import java.lang.reflect.Method;
import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

    private static final Duration CACHE_DURATION = Duration.ofSeconds(1);

    @Bean
    @Override
    public CacheManager cacheManager() {
        var cacheManager = new CaffeineCacheManager();
        var caffeine = Caffeine.newBuilder().refreshAfterWrite(CACHE_DURATION);
        cacheManager.setCaffeine(caffeine);
        cacheManager.setCacheLoader(key -> {
            var cacheKey = (CacheKey) key;
            return cacheKey.method.invoke(cacheKey.target, cacheKey.params);
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
