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

