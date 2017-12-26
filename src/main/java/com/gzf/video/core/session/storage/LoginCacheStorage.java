package com.gzf.video.core.session.storage;

public interface LoginCacheStorage {
    /**
     * Create session id contacting with user id in cache.
     *
     * @param sessionId session id
     * @param userId user id
     */
    void createLoginCache(final String sessionId, final String userId);

    /**
     * Destroy the user id contacting with the specified session id from cache.
     *
     * @param sessionId session id
     */
    void destroyLoginCache(final String sessionId);

    /**
     * Get the user id contacting with the specified session id from cache,
     * if the user id does not exist, simply return null.
     *
     * @param sessionId session id
     * @return user id
     */
    String getLoginUserIdCache(final String sessionId);

    /**
     * Weather there is a cached-session contacting with the specified session id.
     *
     * @param sessionId session id
     * @return weather contains the disk cached-session
     */
    boolean containLoginCache(final String sessionId);
}
