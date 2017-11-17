package com.gzf.video.core.session;

import com.gzf.video.core.ConfigManager;
import com.sun.istack.internal.NotNull;
import com.typesafe.config.Config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SessionManager {

    private static final Config SESSION_CONFIG =
            ConfigManager.loadConfigFromModule(ConfigManager.coreModule, "sessionConfig");

    public static final String SESSION_ID = SESSION_CONFIG.getString("sessionId");
    public static final long SESSION_ID_MAX_AGE = SESSION_CONFIG.getLong("sessionIdMaxAge");
    public static final String SESSION_ID_PATH = SESSION_CONFIG.getString("sessionIdPath");

    public static final String USER_ID = SESSION_CONFIG.getString("userId");
    public static final String RSA_PUBLIC_KEY = SESSION_CONFIG.getString("rsaPublicKey");


    private final ConcurrentHashMap<String, Session> SESSION_MAP =
            new ConcurrentHashMap<>(1024);


    private static SessionManager INSTANCE;


    static {
        Session.setInitialCapacity(SESSION_CONFIG.getInt("initSessionSize"));
    }

    SessionManager() {}


    /**
     * Should only be invoked in static context.
     *
     * @return the single-instance of {@link SessionManager}
     */
    public synchronized static SessionManager getINSTANCE() {
        if (INSTANCE == null) {
            try {
                // in Java9, Class#newInstance() has been marked as @Deprecated, so we use this
                Constructor constructor =
                        Class.forName(SESSION_CONFIG.getString("sessionCacheProvider"))
                                .getConstructor();
                INSTANCE = (SessionManager) constructor.newInstance();
            } catch ( ClassNotFoundException
                    | IllegalAccessException
                    | InstantiationException
                    | NoSuchMethodException
                    | InvocationTargetException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        return INSTANCE;
    }



    // cached-session

    /**
     * Create session id contacting with user id in cache.
     *
     * @param sessionId session id
     * @param userId user id
     */
    public abstract void createLoginCache(final String sessionId, final String userId);

    /**
     * Destroy the user id contacting with the specified session id from cache.
     *
     * @param sessionId session id
     */
    public abstract void destroyLoginCache(final String sessionId);

    /**
     * Get the user id contacting with the specified session id from cache,
     * if the user id does not exist, simply return null.
     *
     * @param sessionId session id
     * @return user id
     */
    public abstract String getLoginUserIdCache(final String sessionId);

    /**
     * Weather there is a cached-session contacting with the specified session id.
     *
     * @param sessionId session id
     * @return weather contains the disk cached-session
     */
    public abstract boolean containLoginCache(final String sessionId);





    // internal-session implementation

    /**
     * Used to create session for a login user. <br />
     *
     * @return a new session associated with the specified session id,
     *         or {@code null} if there was already a session mapping for the session id
     *
     * @throws NullPointerException if the specified session id or value is null
     */
    public Session createSession(final String sessionId, final String userId) {
        Session session = new Session(userId);
        if (SESSION_MAP.putIfAbsent(sessionId, session) == null) {
            return session;
        }
        return null;
    }

    /**
     * Remove the session associates with the specified session id.
     *
     * @param sessionId session id
     * @return the session that been removed, or null if it didn't exist
     */
    public Session destroySession(@NotNull final String sessionId) {
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
     * @param createIfAbsent weather create a new session if it absents
     * @return the session associates with the specified session id,
     *         can be null if {@code createIfAbsent} is false
     */
    public Session getSession(final String sessionId, final boolean createIfAbsent) {
        Session session = SESSION_MAP.get(sessionId);
        if (session == null && createIfAbsent) {
            session = new Session();
            SESSION_MAP.put(sessionId, session);
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
}
