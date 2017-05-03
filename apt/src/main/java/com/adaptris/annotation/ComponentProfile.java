/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that aids the UI with presentation around the short description and type of component.
 * 
 * @author lchan
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface ComponentProfile {
  /**
   * The summary or short description associated with this component profile.
   */
  String summary() default "";
  
  /** The tags to be associated with this profile.
   * 
   */
  String tag() default "";

  /**
   * Returns an array of classes that are contextually related with this component.
   * <p>For a producer, this would be the list of connections that you should associate with it.</p>
   * @return an array of classes
   * @since 3.2.0
   */
  Class[]recommended() default {};

  /**
   * Returns an array of strings that contains metadata keys that may be created or have an effect on the behaviour of this
   * component.
   * 
   * @return a string array.
   * @since 3.4.1
   */
  String[] metadata() default {};

  /**
   * Whether or not this class can be used as the first service with a {@code BranchingServiceCollection}.
   * 
   * @return true if isBranching() will return true (at runtime).
   * @since 3.6.2
   */
  boolean branchSelector() default true;
}
