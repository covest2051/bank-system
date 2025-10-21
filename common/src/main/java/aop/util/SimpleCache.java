package aop.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SimpleCache {
    private final ConcurrentHashMap<Integer, CacheEntry> cache = new ConcurrentHashMap<>();
    @Value("${cache.lifetime.duration}")
    private long cacheLifetimeDuration;

    public Object get(int key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) return null;

        if (System.currentTimeMillis() - entry.timestamp > cacheLifetimeDuration) {
            cache.remove(key);
            log.info("Cache expired for key {}", key);
            return null;
        }
        return entry.value;
    }

    public void put(int key, Object value) {
        cache.put(key, new CacheEntry(value, System.currentTimeMillis()));
        log.info("Cached new entry for key {}", key);
    }

    private void startCleanupTask() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            cache.entrySet().removeIf(e -> now - e.getValue().timestamp > cacheLifetimeDuration);
        }, cacheLifetimeDuration, cacheLifetimeDuration, TimeUnit.MILLISECONDS);
    }

    private static class CacheEntry {
        Object value;
        long timestamp;

        CacheEntry(Object value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }
}
