package com.gzf.video.core.annotation.action;

import java.lang.annotation.*;

/**
 * Custom path parameters.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PathParam {

    /**
     * The name will reflects to custom path parameter.
     * <br />
     * e.g. request path: {@code "/path/to/{where}/{when}"}
     * <br />
     * custom path parameter {@code "where" or "when"} can be the {@code value()}.
     */
    String value();

}
