package com.gzf.video.core.cache;

import org.ehcache.Cache;

/**
 * Only one {@link EhcacheProvider} for whole application.
 */
public class EhcacheProviderMetric {

    // -----------------
    private static Cache<String, String> loginCache;
    private static Cache<String, String> articleCache;
    private static final EhcacheProvider cacheProvider = new EhcacheProvider();
    // -----------------

    public static Cache<String, String> getLoginCache() {
        if (loginCache == null) {
            loginCache = cacheProvider.initCache("login", String.class, String.class);
        }
        return loginCache;
    }

    public static Cache<String, String> getArticleCache() {
        if (articleCache == null) {
            articleCache = cacheProvider.initCache("article", String.class, String.class);
        }
        return articleCache;
    }

    private EhcacheProviderMetric() {}
}
