package com.gzf.video.core.bean.inject;

import java.lang.annotation.*;

/**
 * Auto-inject.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {
}
