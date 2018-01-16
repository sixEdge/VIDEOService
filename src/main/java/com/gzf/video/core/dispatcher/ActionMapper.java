package com.gzf.video.core.dispatcher;

import com.gzf.video.core.SingleInstanceFactory;
import com.gzf.video.core.controller.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionMapper extends SingleInstanceFactory<String, Action> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void put(final String path, final Action action) {
        if (super.isConflicting(path))
            logger.warn("Conflicting action, two actions are both mapped to the same path {}.", path);

        super.put(path, action);
    }
}
