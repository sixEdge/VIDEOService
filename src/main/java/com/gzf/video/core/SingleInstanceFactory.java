package com.gzf.video.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple single-instance factory.
 * @param <K> key
 * @param <V> bean instance
 */
public abstract class SingleInstanceFactory<K, V> {

    private final Map<K, V> mapper = new HashMap<>();

    public void put(final K key, final V bean) {
        mapper.put(key, bean);
    }

    public V get(final K key) {
        return mapper.get(key);
    }

    public boolean isConflicting(final K key) {
        return get(key) != null;
    }

    public Map<K, V> getMapper() {
        return mapper;
    }

    public static <K0, V0> SingleInstanceFactory<K0, V0> newBeanFactory() {
        return new SingleInstanceFactory<K0, V0>() {};
    }
}
