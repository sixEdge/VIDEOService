package com.gzf.video.core.dispatcher;

import com.gzf.video.core.controller.action.Action;

import java.util.List;
import java.util.Map;

public interface Dispatcher {

    Action doDispatch(final String path, final Map<String, List<String>> parameters, final boolean get_or_post);

}
