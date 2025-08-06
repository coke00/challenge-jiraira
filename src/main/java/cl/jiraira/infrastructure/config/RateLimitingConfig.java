package cl.jiraira.infrastructure.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class RateLimitingConfig {

    @Getter
    @Value("${rate-limit.requests-per-minute:3}")
    private int requestsPerMinute;

    private final Map<String, RequestCounter> rateLimiters = new ConcurrentHashMap<>();

    public boolean isAllowed(String clientId) {
        RequestCounter counter = rateLimiters.computeIfAbsent(clientId, k -> new RequestCounter());
        return counter.tryAcquire(requestsPerMinute);
    }

    private static class RequestCounter {
        private LocalDateTime windowStart = LocalDateTime.now();
        private final AtomicInteger count = new AtomicInteger(0);

        public synchronized boolean tryAcquire(int maxRequests) {
            LocalDateTime now = LocalDateTime.now();

            if (ChronoUnit.MINUTES.between(windowStart, now) >= 1) {
                windowStart = now;
                count.set(0);
            }

            if (count.get() < maxRequests) {
                count.incrementAndGet();
                return true;
            }

            return false;
        }
    }
}
