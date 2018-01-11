package com.gzf.video.core.session.storage;

import com.gzf.video.core.cache.EhcacheProviderMetric;
import org.ehcache.Cache;

public class EhcacheSessionStorage extends SessionStorage {

    private final Cache<String, String> loginCache = EhcacheProviderMetric.getLoginCache();


    @Override
    public void createLoginCache(final String sessionId, final String userId) {
        loginCache.put(sessionId, userId);
    }

    @Override
    public void destroyLoginCache(final String sessionId) {
        loginCache.remove(sessionId);
    }

    @Override
    public String getLoginUserIdCache(final String sessionId) {
        return loginCache.get(sessionId);
    }

    @Override
    public boolean containLoginCache(final String sessionId) {
        return loginCache.containsKey(sessionId);
    }
}
