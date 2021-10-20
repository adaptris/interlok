package com.adaptris.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface InterlokAlias {
  /**
   *
   * The alias (which should match the corresponding {@code XStreamAlias}
   */
  String value();
}
