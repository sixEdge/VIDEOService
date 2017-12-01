package com.gzf.video.core.controller.action;

import com.gzf.video.core.http.request.Request;
import com.gzf.video.core.http.response.Response;

@FunctionalInterface
public interface Action {

    Response doAction(Request rw);
}
