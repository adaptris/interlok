package com.adaptris.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface InterlokLicence {

  public enum Type {
    /**
     * No License Required.
     *
     */
    NONE,
    /**
     * Basic license Required
     *
     */
    BASIC,
    /**
     * Standard license Required
     *
     */
    STANDARD,
    /**
     * Enterprise license Required
     *
     */
    ENTERPRISE;
  }

  /**
   *
   * The required licensing
   */
  Type value();
}
