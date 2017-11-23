package com.gzf.video.core.controller.action;

import com.gzf.video.core.request.Request;
import io.netty.handler.codec.http.FullHttpResponse;

@FunctionalInterface
public interface Action {

    FullHttpResponse doAction(Request rw);

}
