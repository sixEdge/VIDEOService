package com.gzf.video.core.session;

import java.util.HashMap;

public class Session extends HashMap<String, Object> {

    private static int initialCapacity = 16; // default

    private String userId;


    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }


    public Session() {
        super(initialCapacity, 1f);
    }

    public Session(final String userId) {
        super(initialCapacity, 1f);
        this.userId = userId;
    }

    public static void setInitialCapacity(final int initialCapacity) {
        Session.initialCapacity = initialCapacity;
    }
}
