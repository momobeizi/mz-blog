package com.mz.blog.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private final Map<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();

    @Value("${app.security.login-failure-limit:5}")
    private int limit;

    @Value("${app.security.login-failure-window-minutes:15}")
    private long windowMinutes;

    public boolean isBlocked(String key) {
        Deque<Instant> queue = attempts.get(key);
        if (queue == null) {
            return false;
        }
        synchronized (queue) {
            prune(queue);
            return queue.size() >= limit;
        }
    }

    public void failed(String key) {
        Deque<Instant> queue = attempts.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (queue) {
            prune(queue);
            queue.addLast(Instant.now());
        }
    }

    public void success(String key) {
        attempts.remove(key);
    }

    private void prune(Deque<Instant> queue) {
        Instant threshold = Instant.now().minusSeconds(windowMinutes * 60);
        while (!queue.isEmpty() && queue.peekFirst().isBefore(threshold)) {
            queue.removeFirst();
        }
    }
}
