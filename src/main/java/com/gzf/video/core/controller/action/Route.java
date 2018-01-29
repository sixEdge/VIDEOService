package com.gzf.video.core.controller.action;

import com.gzf.video.core.http.request.HttpMethod;

import java.lang.annotation.*;

import static com.gzf.video.core.http.request.HttpMethod.GET;
import static com.gzf.video.util.StringUtil.EMPTY_STRING;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Route {

    /** Request method */
    HttpMethod method() default GET;

    /** Request url */
    String url() default "/";
}
