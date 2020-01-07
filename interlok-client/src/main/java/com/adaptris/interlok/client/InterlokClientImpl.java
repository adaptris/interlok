package com.adaptris.interlok.client;

import java.util.Map;

import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.types.DefaultSerializableMessage;

public abstract class InterlokClientImpl implements InterlokClient {

  public void processAsync(MessageTarget f, String payload, Map<String, String> hdrs) throws InterlokException {
    processAsync(f, new DefaultSerializableMessage().withPayload(payload).withMessageHeaders(hdrs));
  }
}
