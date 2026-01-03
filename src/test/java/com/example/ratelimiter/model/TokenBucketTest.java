package com.example.ratelimiter.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TokenBucketTest {

    @Test
    void testCapacityAndRefill() throws InterruptedException {
        // Capacity 10, refill 10 per second (1 per 100ms)
        TokenBucket bucket = new TokenBucket(10, 10);

        // Consume all
        assertTrue(bucket.tryConsume(10));
        assertFalse(bucket.tryConsume(1));

        // Wait 150ms -> should gain at least 1 token
        Thread.sleep(150);
        assertTrue(bucket.tryConsume(1));
    }

    @Test
    void testBurst() {
        TokenBucket bucket = new TokenBucket(5, 1);
        assertTrue(bucket.tryConsume(5));
        assertFalse(bucket.tryConsume(1));
    }
}
