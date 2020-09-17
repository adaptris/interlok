package com.adaptris.interlok.util;

public class Closer {


  public static void closeQuietly(AutoCloseable... closeables) {
    if (closeables != null) {
      for (AutoCloseable c : closeables) {
        try {
          if (c != null)
            c.close();
        } catch (Exception e) {
        }
      }
    }
  }
}
