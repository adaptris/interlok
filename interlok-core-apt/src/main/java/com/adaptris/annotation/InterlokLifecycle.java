package com.adaptris.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Interface applied to a field that allows us to auto request a lifecycle operation.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InterlokLifecycle {

}
