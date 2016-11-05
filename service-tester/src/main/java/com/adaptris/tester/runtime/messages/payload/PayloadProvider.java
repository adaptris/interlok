package com.adaptris.tester.runtime.messages.payload;

import com.adaptris.tester.runtime.messages.MessageException;

public abstract class PayloadProvider {

  public void init() throws MessageException {

  }

  public abstract String getPayload();
}
