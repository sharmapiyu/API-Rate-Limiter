package com.example.ratelimiter.model;

/**
 * Thread-safe implementation of the Token Bucket algorithm.
 * 
 * I used AtomicLong for thread safety because it's faster than using
 * synchronized
 * on everything. The refill happens "lazily" when a request comes in,
 * instead of running a background thread which would be heavy.
 */
public class TokenBucket {

    private final long capacity;
    private final double refillRatePerSecond;

    // Current tokens in the bucket. Using double for precision, but storing as bits
    // logic could be complex without lock.
    // For simplicity and correctness with floating point refill, we'll monitor this
    // with synchronization
    // to guarantee atomic update of both timestamp and token count.
    private double currentTokens;
    private long lastRefillTimestamp;

    /**
     * @param capacity            Maximum number of tokens the bucket can hold.
     * @param refillRatePerSecond Number of tokens to add per second.
     */
    public TokenBucket(long capacity, double refillRatePerSecond) {
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.currentTokens = capacity;
        this.lastRefillTimestamp = System.nanoTime();
    }

    /**
     * Attempts to consume a specified number of tokens.
     * 
     * @param tokensToConsume Number of tokens required.
     * @return true if tokens were consumed, false if limited.
     */
    public synchronized boolean tryConsume(long tokensToConsume) {
        refill();

        if (currentTokens >= tokensToConsume) {
            currentTokens -= tokensToConsume;
            return true;
        }
        return false;
    }

    /**
     * Refills the bucket based on time elapsed.
     */
    private void refill() {
        long now = System.nanoTime();
        long nanosElapsed = now - lastRefillTimestamp;

        if (nanosElapsed > 0) {
            double tokensToAdd = (nanosElapsed / 1_000_000_000.0) * refillRatePerSecond;
            double limitedNewTokens = Math.min(capacity, currentTokens + tokensToAdd);
            currentTokens = limitedNewTokens;
            lastRefillTimestamp = now;
        }
    }

    // Getter for testing observability
    public synchronized double getCurrentTokens() {
        refill();
        return currentTokens;
    }
}
