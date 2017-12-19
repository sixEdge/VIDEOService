package com.gzf.video.core.dispatcher;

import com.gzf.video.core.controller.action.Action;

import java.util.*;

class ActionMapper {

    private final HashMap<String, Action> mapper = new HashMap<>();


    ActionMapper() {}


    void put(final String path, final Action action) {
        mapper.put(path, action);
    }

    Action get(final String path) {
        return mapper.get(path);
    }

    boolean isConflicting(final String path) {
        return get(path) != null;
    }
}
