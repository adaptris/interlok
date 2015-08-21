package com.adaptris.interlok.management;

import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.types.SerializableMessage;

public interface MessageProcessor {
  void processAsync(SerializableMessage sm) throws InterlokException;

  SerializableMessage process(SerializableMessage sm) throws InterlokException;
}