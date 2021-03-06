package com.gzf.video.core.session;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public class Session extends AttributeSession {

    private static final Clock CLOCK = Clock.system(ZoneId.of("GMT"));

//    private final Instant creationTime = Instant.now(CLOCK);

    private volatile Instant lastAccessTime = Instant.now(CLOCK);

    private volatile Duration maxIdleTime = Duration.ofHours(2L);

    private volatile boolean isAlive = true;  // alive or expired


    private final String sessionId;


    public Session(final String sessionId) {
        this.sessionId = sessionId;
    }


    public String getSessionId() {
        return sessionId;
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
        if (isAlive) {
            if (checkExpired(currentTime)) {
                isAlive = false;
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
