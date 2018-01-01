package com.gzf.video.core.cache;

import org.ehcache.Cache;

public class EhcacheProviderMetric {

    // -----------------
    private Cache<String, String> loginCache;
    private Cache<String, String> articleCache;
    private final EhcacheProvider cacheProvider = EhcacheProvider.getINSTANCE();
    // -----------------

    public synchronized Cache<String, String> getLoginCache() {
        if (loginCache == null) {
            loginCache = cacheProvider.initCache("login", String.class, String.class);
        }
        return loginCache;
    }

    public synchronized Cache<String, String> getArticleCache() {
        if (articleCache == null) {
            articleCache = cacheProvider.initCache("article", String.class, String.class);
        }
        return articleCache;
    }

    private static final EhcacheProviderMetric INSTANCE = new EhcacheProviderMetric();

    public static EhcacheProviderMetric getINSTANCE() {
        return INSTANCE;
    }

    private EhcacheProviderMetric() {}
}
