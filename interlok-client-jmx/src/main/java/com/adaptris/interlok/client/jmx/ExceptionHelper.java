package com.adaptris.interlok.client.jmx;

import com.adaptris.interlok.InterlokException;

class ExceptionHelper {

  static void rethrowInterlokException(Throwable e) throws InterlokException {
    rethrowInterlokException(e.getMessage(), e);
  }

  static void rethrowInterlokException(String msg, Throwable e) throws InterlokException {
    if (e instanceof InterlokException) {
      throw (InterlokException) e;
    }
    throw new InterlokException(msg, e);
  }
}
