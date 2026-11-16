package com.airwise.backend.http;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;

/** One atomic, bounded in-memory budget per instance. No forwarded headers, DNS or persistence. */
final class AdmissionLimiter {
    private static final long SECOND = 1_000_000_000L;
    private static final long TTL = 600 * SECOND;
    private final LongSupplier ticker;
    private final Map<String, Bucket> addresses = new HashMap<>();
    private final Bucket global;
    private long lastCleanup;

    AdmissionLimiter(LongSupplier ticker) {
        this.ticker = ticker; long now = ticker.getAsLong();
        global = new Bucket(300, now); lastCleanup = now;
    }
    synchronized Result acquire(String remoteAddress) {
        long now = ticker.getAsLong();
        if (now - lastCleanup >= SECOND) {
            addresses.values().removeIf(b -> b.active == 0 && now - b.touched >= TTL);
            lastCleanup = now;
        }
        Bucket ip = addresses.get(remoteAddress);
        if (ip == null) {
            if (addresses.size() >= 10_000) return new Result(null, 1);
            ip = new Bucket(60, now); addresses.put(remoteAddress, ip);
        }
        refill(ip, now, 60, 1); refill(global, now, 300, 5);
        ip.touched = now;
        long retry = Math.max(waitSeconds(ip.tokens, 1), waitSeconds(global.tokens, 5));
        // Every external attempt consumes the available tokens, even if concurrency is saturated.
        if (ip.tokens >= 1) ip.tokens--;
        if (global.tokens >= 1) global.tokens--;
        if (retry > 0 || ip.active >= 2 || global.active >= 8) return new Result(null, Math.max(1, retry));
        ip.active++; global.active++;
        return new Result(new Permit(this, ip), 0);
    }
    private void refill(Bucket b, long now, int capacity, int rate) {
        b.tokens = Math.min(capacity, b.tokens + Math.max(0, now - b.updated) / (double) SECOND * rate);
        b.updated = now;
    }
    private static long waitSeconds(double tokens, int rate) {
        return tokens >= 1 ? 0 : Math.max(1, (long) Math.ceil((1 - tokens) / rate));
    }
    private synchronized void release(Bucket b) { b.active--; global.active--; b.touched = ticker.getAsLong(); }
    synchronized int addressCount() { return addresses.size(); }
    record Result(Permit permit, long retryAfterSeconds) { }
    static final class Permit implements AutoCloseable {
        private final AdmissionLimiter owner;
        private final Bucket bucket;
        private final AtomicBoolean released = new AtomicBoolean();
        Permit(AdmissionLimiter owner, Bucket bucket) { this.owner = owner; this.bucket = bucket; }
        @Override public void close() { if (released.compareAndSet(false, true)) owner.release(bucket); }
    }
    private static final class Bucket {
        double tokens; long updated; long touched; int active;
        Bucket(int tokens, long now) { this.tokens = tokens; this.updated = now; this.touched = now; }
    }
}
