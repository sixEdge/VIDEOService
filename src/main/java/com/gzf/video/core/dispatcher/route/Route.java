package com.gzf.video.core.dispatcher.route;

import com.gzf.video.core.http.request.HttpMethod;

import java.lang.annotation.*;

import static com.gzf.video.core.http.request.HttpMethod.GET;

/**
 * Sign a method in a controller class, and this method will be an action.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Route {

    /** Request method */
    HttpMethod method() default GET;

    /** Request url */
    String url() default "/";
}
