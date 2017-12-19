package com.gzf.video.core.controller;

import java.lang.annotation.*;

/**
 * Specific the request path to access this controller. <br />
 * Only can be used on controller classes.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controller {

    /**
     * The request path to access this controller
     */
    String value() default "";

}
