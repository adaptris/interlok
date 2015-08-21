package com.adaptris.interlok.management;

import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.types.StandardMessage;

public interface MessageProcessor {
  void processAsync(StandardMessage sm) throws InterlokException;

  StandardMessage process(StandardMessage sm) throws InterlokException;
}