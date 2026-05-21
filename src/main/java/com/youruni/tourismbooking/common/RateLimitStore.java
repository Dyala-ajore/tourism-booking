package com.youruni.tourismbooking.common;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
public class RateLimitStore {
    private static class RequestInfo {
        AtomicInteger count;
        long windowStartTime;
        RequestInfo(long windowStartTime) {
            this.count = new AtomicInteger(1);
            this.windowStartTime = windowStartTime;
        }
        synchronized boolean incrementAndCheck(long currentTime, int limit, long windowDurationMs) {
            if (currentTime - windowStartTime >= windowDurationMs) {
                this.count = new AtomicInteger(1);
                this.windowStartTime = currentTime;
                return true; 
            }
            int newCount = count.incrementAndGet();
            return newCount <= limit;
        }
        synchronized void reset() {
            count = new AtomicInteger(0);
            windowStartTime = System.currentTimeMillis();
        }
    }
    private final ConcurrentHashMap<String, RequestInfo> store = new ConcurrentHashMap<>();
    private final int requestLimit;
    private final long windowDurationMs;
    public RateLimitStore(int requestLimit, long windowDurationMs) {
        this.requestLimit = requestLimit;
        this.windowDurationMs = windowDurationMs;
    }
    public boolean allowRequest(String key) {
        long currentTime = System.currentTimeMillis();
        RequestInfo info = store.computeIfAbsent(key, k -> new RequestInfo(currentTime));
        return info.incrementAndCheck(currentTime, requestLimit, windowDurationMs);
    }
    public int getRequestCount(String key) {
        RequestInfo info = store.get(key);
        if (info == null) {
            return 0;
        }
        return info.count.get();
    }
    public void resetKey(String key) {
        RequestInfo info = store.get(key);
        if (info != null) {
            info.reset();
        }
    }
    public void clear() {
        store.clear();
    }
}