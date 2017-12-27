package com.gzf.video.core.dispatcher;

import com.gzf.video.core.controller.action.Action;

public interface Dispatcher {

    Action doDispatch(final String path, final boolean get_or_post);

    void init();
}
