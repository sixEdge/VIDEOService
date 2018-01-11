package com.gzf.video.core.controller.action.method;

import io.netty.handler.codec.http.HttpMethod;

import java.lang.annotation.*;

import static io.netty.handler.codec.http.HttpMethod.GET;

/**
 * <em>For GET-Action</em>
 * <br />
 * The request url to access this action method.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Get {

    /** Request url */
    String value();

    HttpMethod method = GET;
}
