package com.gzf.video.core.session.storage;

import com.gzf.video.core.ConfigManager;
import com.gzf.video.core.async.AsyncTask;
import com.gzf.video.core.session.Session;
import com.gzf.video.core.session.SessionIdGenerator;
import com.typesafe.config.Config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public abstract class SessionStorage implements LoginStateCache {

    private static final Config SESSION_CONFIG =
            ConfigManager.loadConfigFromModule(ConfigManager.coreModule, "sessionConfig");

    public static final String SESSION_ID = SESSION_CONFIG.getString("sessionId");
    public static final long SESSION_ID_MAX_AGE = SESSION_CONFIG.getLong("sessionIdMaxAge");
    public static final String SESSION_ID_PATH = SESSION_CONFIG.getString("sessionIdPath");

    public static final String USER_ID = SESSION_CONFIG.getString("userId");
    public static final String RSA_PRIVATE_KEY = SESSION_CONFIG.getString("rsaPrivateKey");


    private final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>(1024);


    private static SessionStorage INSTANCE;


    SessionStorage() {}


    /**
     * Should only be invoked in static context.
     *
     * @return the single-instance of {@link SessionStorage}
     */
    public synchronized static SessionStorage getINSTANCE() {
        if (INSTANCE == null) {
            try {
                // in Java9, Class#newInstance() has been marked as @Deprecated, so we use this
                Constructor constructor =
                        Class.forName(SESSION_CONFIG.getString("sessionCacheProvider"))
                                .getConstructor();
                INSTANCE = (SessionStorage) constructor.newInstance();
            } catch ( ClassNotFoundException
                    | IllegalAccessException
                    | InstantiationException
                    | NoSuchMethodException
                    | InvocationTargetException e) {
                throw new Error(e);
            }
        }
        return INSTANCE;
    }


    // internal-session implementation

    private static final SessionIdGenerator SESSION_ID_GENERATOR = new SessionIdGenerator();

    /**
     * Create a session. <br />
     *
     * @return a session associated with the random session id,
     */
    public Session createSession() {
        String sessionId = SESSION_ID_GENERATOR.generateSessionId();
        Session newSession = new Session(sessionId);

        SESSION_MAP.put(sessionId, newSession);

        return newSession;
    }

    /**
     * Create a session. <br />
     *
     * @param sessionId session id
     *
     * @return a session associated with the specified session id,
     */
    public Session createSession(final String sessionId) {
        return SESSION_MAP.put(sessionId, new Session(sessionId));
    }

    /**
     * Remove the session associates with the specified session id.
     *
     * @param sessionId session id
     * @return the session that been removed, or null if it didn't exist
     */
    public Session destroySession(final String sessionId) {
        return SESSION_MAP.remove(sessionId);
    }

    /**
     * <em>Will not create a new session.</em>
     *
     * @param sessionId session id
     * @return user id if it exists, otherwise null
     */
    public String getUserId(final String sessionId) {
        Session session = SESSION_MAP.get(sessionId);
        return session == null
                ? null
                : session.getUserId();
    }

    /**
     * Get the current session associated with the specified session id.<br />
     *
     * @param sessionId session id
     * @param createIfAbsent weather create a new session if session absents
     * @return the session associates with the specified session id,
     *         can be null if {@code createIfAbsent} is false
     */
    public Session getSession(final String sessionId, final boolean createIfAbsent) {
        Session session = retrieveSession(sessionId);
        if (session == null && createIfAbsent) {
            return createSession();
        }
        return session;
    }

    /**
     * Get the current session associated with the specified session id.<br />
     * If no session find, create one, put it into the internal session map, then return it.
     *
     * @param sessionId session id
     * @return the session associates with the specified session id,
     *         never be null
     */
    public Session getSession(final String sessionId) {
        return getSession(sessionId, true);
    }

    public Object getFromSession(final String sessionId, final String key) {
        Session session = getSession(sessionId);
        return session.get(key);
    }

    public Object addToSession(final String sessionId, final String key, final Object val) {
        Session session = getSession(sessionId);
        return session.put(key, val);
    }




    /**
     * Minimum period between expiration checks.
     */
    private static final Duration EXPIRATION_CHECK_PERIOD = Duration.ofMinutes(2L);

    private static final Clock CLOCK = Clock.system(ZoneId.of("GMT"));


    private volatile Instant nextExpirationCheckTime = Instant.now(CLOCK).plus(EXPIRATION_CHECK_PERIOD);

    private final ReentrantLock expirationCheckLock = new ReentrantLock();


    private Session retrieveSession(final String sessionId) {

        Instant currentTime = Instant.now(CLOCK);

        if (!SESSION_MAP.isEmpty() && !currentTime.isBefore(nextExpirationCheckTime)) {
            checkExpiredSessions(currentTime);
        }

        Session session = SESSION_MAP.get(sessionId);
        if (session == null) {
            return null;
        } else if (session.isExpired(currentTime)) {
            SESSION_MAP.remove(sessionId);
            return null;
        } else {
            session.setLastAccessTime(currentTime);
            return session;
        }
    }

    private void checkExpiredSessions(final Instant currentTime) {
        AsyncTask.execute(() -> {
            if (expirationCheckLock.tryLock()) {
                try {
                    Iterator<Session> iterator = SESSION_MAP.values().iterator();
                    while (iterator.hasNext()) {
                        Session session = iterator.next();
                        if (session.isExpired(currentTime)) {
                            iterator.remove();
                            session.clear();
                        }
                    }
                } finally {
                    nextExpirationCheckTime = currentTime.plus(EXPIRATION_CHECK_PERIOD);
                    expirationCheckLock.unlock();
                }
            }
        });
    }
}
