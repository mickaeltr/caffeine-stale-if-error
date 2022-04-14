package com.example.demo;

import java.io.Closeable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
public class CachedRepository implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(CachedRepository.class);

    private int counter;

    @Cacheable("getInteger")
    public Integer getInteger() {
        if (counter == 2) {
            LOG.error("Error {}", counter);
            throw new RuntimeException("Oops: " + counter++);
        }

        LOG.info("Success {}", counter);
        return counter++;
    }

    @Override
    public void close() {
        counter = 0;
    }
}
