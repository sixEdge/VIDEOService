package com.gzf.video.core.annotation.action;

import java.lang.annotation.*;

/**
 * Custom request parameters.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ReqParam {

    /**
     * The name will reflects to custom request parameter.
     * <br />
     * e.g. request path: {@code "/path/to/{where}/{when}"}, request parameter: {@code tid=val}
     * <br />
     * custom request parameter key {@code "tid"} can be the {@code value()}.
     */
    String value();

}
