package com.gzf.video.core.controller.action.method;

import io.netty.handler.codec.http.HttpMethod;

import java.lang.annotation.*;

import static io.netty.handler.codec.http.HttpMethod.POST;

/**
 * <em>For POST-Action</em>
 * <br />
 * The request url to access this action method.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Post {

    /** Request url */
    String value();

    HttpMethod method = POST;

}
