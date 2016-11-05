package com.adaptris.tester.runtime.messages.metadata;

import com.adaptris.tester.runtime.messages.MessageException;

import java.util.Map;

public abstract class MetadataProvider {

  public void init()throws MessageException {

  }

  public abstract Map<String, String> getMessageHeaders();
}
