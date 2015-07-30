package com.adaptris.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An interface for making a "hint" for the UI about what type of input field this is.
 * 
 * @author lchan
 * @since 3.0.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InputFieldHint {
  /**
   * The style associated with this InputField.
   */
  String style() default "";

}
