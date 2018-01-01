package com.gzf.video.core.cache;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import sun.misc.Cleaner;

import java.util.Objects;

class EhcacheProvider {

    private CacheManager cacheManager;

    private static final XmlConfiguration xmlConfiguration =
            new XmlConfiguration(
                    Objects.requireNonNull(
                            EhcacheProvider.class.getClassLoader().getResource("ehcache.xml")));

    {
        cacheManager = CacheManagerBuilder.newCacheManager(xmlConfiguration);
        cacheManager.init();
        Cleaner.create(this, cacheManager::close);
    }


    <K, V> Cache<K, V> initCache(final String cacheName, Class<K> keyClass, Class<V> valClass) {
        return cacheManager.getCache(cacheName, keyClass, valClass);
    }

    private static final EhcacheProvider INSTANCE = new EhcacheProvider();

    public static EhcacheProvider getINSTANCE() {
        return INSTANCE;
    }

    private EhcacheProvider() {}
}
