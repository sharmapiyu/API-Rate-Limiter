package com.example.ratelimiter.interceptor;

import com.example.ratelimiter.exception.RateLimitExceededException;
import com.example.ratelimiter.model.TokenBucket;
import com.example.ratelimiter.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    public RateLimitInterceptor(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // Use IP address as client identifier if API key is missing
        String apiKey = request.getHeader("X-API-KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = request.getRemoteAddr();
        }

        TokenBucket tokenBucket = rateLimiterService.resolveBucket(apiKey);

        if (!tokenBucket.tryConsume(1)) {
            throw new RateLimitExceededException("Rate limit exceeded. Try again later.");
        }

        return true;
    }
}
