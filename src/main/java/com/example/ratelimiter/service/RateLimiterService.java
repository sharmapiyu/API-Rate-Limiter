package com.example.ratelimiter.service;

import com.example.ratelimiter.model.TokenBucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, TokenBucket> paramBuckets = new ConcurrentHashMap<>();

    @Value("${ratelimiter.capacity:10}")
    private long capacity;

    @Value("${ratelimiter.refill-rate:1.0}")
    private double refillRate;

    public TokenBucket resolveBucket(String apiKey) {
        return paramBuckets.computeIfAbsent(apiKey, this::createNewBucket);
    }

    private TokenBucket createNewBucket(String apiKey) {
        return new TokenBucket(capacity, refillRate);
    }
}
