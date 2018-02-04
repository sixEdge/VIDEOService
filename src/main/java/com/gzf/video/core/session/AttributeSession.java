package com.gzf.video.core.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AttributeSession {

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    private volatile String userId;


    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }


    public Object getAttribute(final String key) {
        return attributes.get(key);
    }

    public Object putAttribute(final String key, final Object value) {
        return attributes.put(key, value);
    }

    public Object removeAttribute(final String key) {
        return attributes.remove(key);
    }


    /**
     * Clear user id and attributes.
     */
    public void clear() {
        userId = null;
        attributes.clear();
    }
}
