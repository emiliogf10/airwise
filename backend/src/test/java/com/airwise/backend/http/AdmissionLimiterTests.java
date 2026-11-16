package com.airwise.backend.http;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AdmissionLimiterTests {
    @Test void perAddressAndGlobalPermitsAreAtomicAndReleasedExactlyOnce() {
        AtomicLong time = new AtomicLong();
        AdmissionLimiter limiter = new AdmissionLimiter(time::get);
        var one = limiter.acquire("one").permit();
        var two = limiter.acquire("one").permit();
        assertThat(limiter.acquire("one").retryAfterSeconds()).isEqualTo(1);
        one.close(); one.close();
        var replacement = limiter.acquire("one").permit();
        assertThat(replacement).isNotNull();
        var others = new java.util.ArrayList<AdmissionLimiter.Permit>();
        for (int i = 0; i < 6; i++) others.add(limiter.acquire("other-" + i).permit());
        assertThat(others).doesNotContainNull();
        assertThat(limiter.acquire("ninth").permit()).isNull();
        two.close(); replacement.close(); others.forEach(AdmissionLimiter.Permit::close);
        assertThat(limiter.acquire("ninth").permit()).isNotNull();
    }

    @Test void sixtyRequestsExhaustAddressAndMonotonicTimeRefillsOnePerSecond() {
        AtomicLong time = new AtomicLong();
        AdmissionLimiter limiter = new AdmissionLimiter(time::get);
        for (int i = 0; i < 60; i++) {
            var result = limiter.acquire("one");
            assertThat(result.permit()).isNotNull(); result.permit().close();
        }
        assertThat(limiter.acquire("one").retryAfterSeconds()).isEqualTo(1);
        time.set(999_999_999L);
        assertThat(limiter.acquire("one").permit()).isNull();
        time.set(1_000_000_000L);
        var refilled = limiter.acquire("one").permit();
        assertThat(refilled).isNotNull(); refilled.close();
        assertThat(limiter.acquire("one").permit()).isNull();
    }

    @Test void globalBudgetIsSharedAcrossAddresses() {
        AtomicLong time = new AtomicLong();
        AdmissionLimiter limiter = new AdmissionLimiter(time::get);
        for (int i = 0; i < 300; i++) {
            var permit = limiter.acquire("ip-" + i).permit();
            assertThat(permit).isNotNull(); permit.close();
        }
        assertThat(limiter.acquire("another").permit()).isNull();
        time.set(200_000_000L);
        assertThat(limiter.acquire("another").permit()).isNotNull();
    }

    @Test void boundedMapDoesNotEvictAnActiveAddressAtIdleTtl() {
        AtomicLong time = new AtomicLong();
        AdmissionLimiter limiter = new AdmissionLimiter(time::get);
        var active = limiter.acquire("active").permit();
        for (int i = 0; i < 9999; i++) {
            var permit = limiter.acquire("ip-" + i).permit();
            if (permit != null) permit.close();
        }
        assertThat(limiter.addressCount()).isEqualTo(10_000);
        assertThat(limiter.acquire("new").permit()).isNull();
        time.set(600_000_000_000L);
        var afterCleanup = limiter.acquire("new").permit();
        assertThat(afterCleanup).isNotNull();
        assertThat(limiter.addressCount()).isEqualTo(2);
        active.close(); afterCleanup.close();
    }
}
