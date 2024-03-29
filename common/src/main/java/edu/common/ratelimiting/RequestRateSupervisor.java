package edu.common.ratelimiting;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestRateSupervisor {
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    // todo: настроить и сделать так чтобы можно было задать это в конструкторе или через properties
    // 20 tokens per minute
    private final Bandwidth DEFAULT_BANDWIDTH = Bandwidth.classic(
        20,
        Refill.intervally(
            20, Duration.ofMinutes(1)
        )
    );

    public Bucket resolveBucket(String ipAddress) {
        return cache.computeIfAbsent(ipAddress, this::newBucket);
    }

    private Bucket newBucket(String apiKey) {
        return Bucket.builder()
            .addLimit(DEFAULT_BANDWIDTH)
            .build();
    }
}
