package com.example.demo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

@SpringBootTest
class CacheTest {

    @Autowired
    CachedRepository cachedRepository;

    @Autowired
    CacheManager cacheManager;

    @AfterEach
    void afterEach() {
        cachedRepository.close();
        cacheManager.getCache("getInteger").clear();
    }

    @Test
    void test_stale_while_revalidate() throws InterruptedException {
        Assertions.assertEquals(0, cachedRepository.getInteger()); // New value
        Thread.sleep(20);
        Assertions.assertEquals(0, cachedRepository.getInteger()); // Not expired value

        Thread.sleep(1000);

        Assertions.assertEquals(0, cachedRepository.getInteger()); // Expired value + Reload
        Thread.sleep(20);
        Assertions.assertEquals(1, cachedRepository.getInteger()); // New value

        Thread.sleep(1000);

        Assertions.assertEquals(1, cachedRepository.getInteger()); // Expired value + Failed to reload
        Thread.sleep(20);
        Assertions.assertEquals(1, cachedRepository.getInteger()); // Expired value + Reload
        Thread.sleep(20);
        Assertions.assertEquals(3, cachedRepository.getInteger()); // New value
        Thread.sleep(20);
        Assertions.assertEquals(3, cachedRepository.getInteger()); // Not expired value

        Thread.sleep(1000);

        Assertions.assertEquals(3, cachedRepository.getInteger()); // Expired value + Reload
    }

    @Test
    void test_stale_if_error() throws InterruptedException {
        Assertions.assertEquals(0, cachedRepository.getInteger()); // New value
        Thread.sleep(20);
        Assertions.assertEquals(0, cachedRepository.getInteger()); // Not expired value

        Thread.sleep(1000);

        Assertions.assertEquals(1, cachedRepository.getInteger()); // New value
        Thread.sleep(20);
        Assertions.assertEquals(1, cachedRepository.getInteger()); // Not expired value

        Thread.sleep(1000);

        Assertions.assertEquals(1, cachedRepository.getInteger()); // Failed to reload = Expired value +
        Thread.sleep(20);
        Assertions.assertEquals(3, cachedRepository.getInteger()); // New value
        Thread.sleep(20);
        Assertions.assertEquals(3, cachedRepository.getInteger()); // Not expired value
        Thread.sleep(20);
        Assertions.assertEquals(3, cachedRepository.getInteger()); // Not expired value

        Thread.sleep(1000);

        Assertions.assertEquals(4, cachedRepository.getInteger()); // New value
    }
}
