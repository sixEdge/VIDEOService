package com.gzf.video.core.controller.action.method;

import java.lang.annotation.*;

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

    String method = "POST";

}
