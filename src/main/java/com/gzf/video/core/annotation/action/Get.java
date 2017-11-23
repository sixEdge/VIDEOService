package com.gzf.video.core.annotation.action;

import java.lang.annotation.*;

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

    String method = "GET";

}
