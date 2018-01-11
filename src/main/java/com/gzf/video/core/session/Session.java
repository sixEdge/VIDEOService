package com.gzf.video.core.session;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class Session extends ConcurrentHashMap<String, Object> {

    private static final Clock CLOCK = Clock.system(ZoneId.of("GMT"));


//    private final Instant creationTime = Instant.now(CLOCK);

    private volatile Instant lastAccessTime = Instant.now(CLOCK);

    private volatile Duration maxIdleTime = Duration.ofHours(2L);

    private volatile boolean state = true;  // alive or expired


    private final String sessionId;

    private final AtomicReference<String> userId = new AtomicReference<>();


    public Session(final String sessionId) {
        this.sessionId = sessionId;
    }


    public String getSessionId() {
        return sessionId;
    }

    public String getUserId() {
        return userId.get();
    }

    public void setUserId(final String userId) {
        this.userId.compareAndSet(null, userId);  // only update when userId is null
    }

    public Instant getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(final Instant currentTime) {
        this.lastAccessTime = currentTime;
    }


    public boolean isExpired() {
        return isExpired(Instant.now(CLOCK));
    }

    public boolean isExpired(final Instant currentTime) {
        if (state) {
            if (checkExpired(currentTime)) {
                state = false;
                return true;
            }
            return false;
        }
        return true;
    }

    private boolean checkExpired(final Instant currentTime) {
        return !maxIdleTime.isNegative() && currentTime.minus(maxIdleTime).isAfter(lastAccessTime);
    }
}
