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

package com.adaptris.core.util;

import org.apache.commons.lang.StringUtils;

public class Args {

  /**
   * Convenience to throw an {@link IllegalArgumentException} if the associated argument null.
   * 
   * @param argument the argument.
   * @param member the member name associated with this argument.
   * @return the argument
   */
  public static <T> T notNull(final T argument, final String member) {
    if (argument == null) {
      throw new IllegalArgumentException(member + " may not be null");
    }
    return argument;
  }

  /**
   * Convenience to throw an {@link IllegalArgumentException} if the associated argument is the
   * empty string (or null) as defined by {@link StringUtils#isEmpty(String)}.
   * 
   * @param argument the argument.
   * @param member the member name associated with this argument.
   * @return the argument
   */
  public static String notEmpty(String argument, final String member) {
    if (StringUtils.isEmpty(argument)) {
      throw new IllegalArgumentException(member + " is empty/null");
    }
    return argument;
  }

  /**
   * Convenience to throw an {@link IllegalArgumentException} if the associated argument is the
   * blank string as defined by {@link StringUtils#isBlank(String)}.
   * 
   * @param argument the argument.
   * @param member the member name associated with this argument.
   * @return the argument
   */
  public static String notBlank(final String argument, final String member) {
    if (StringUtils.isBlank(argument)) {
      throw new IllegalArgumentException(member + " may not be blank/empty/null");
    }
    return argument;
  }
}

