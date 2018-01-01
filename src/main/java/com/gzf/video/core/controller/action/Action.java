package com.gzf.video.core.controller.action;

import com.gzf.video.core.http.HttpExchange;
import com.gzf.video.core.http.response.Response;

@FunctionalInterface
public interface Action {

    Response doAction(HttpExchange ex);
}
