package com.adaptris.util;

public abstract class NumberUtils {

  // I wonder why there doesn't seem to be a commons-lang equivalent to this.
  public static int toIntDefaultIfNull(Integer configured, int theDefault) {
    return configured != null ? configured.intValue() : theDefault;
  }

  // I wonder why there doesn't seem to be a commons-lang equivalent to this.
  public static long toLongDefaultIfNull(Long configured, long theDefault) {
    return configured != null ? configured.longValue() : theDefault;
  }

}
