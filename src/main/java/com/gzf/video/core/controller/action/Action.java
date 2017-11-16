package com.gzf.video.core.controller.action;

import io.netty.handler.codec.http.FullHttpResponse;

@FunctionalInterface
public interface Action {

    FullHttpResponse doAction(RequestWrapper rw);

}
