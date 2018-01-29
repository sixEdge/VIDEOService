package com.gzf.video.core.dispatcher;

import com.gzf.video.core.controller.action.Action;
import com.gzf.video.core.http.request.HttpMethod;

public interface Dispatcher {

    Action doDispatch(final String path, final HttpMethod method);

    void init();
}
