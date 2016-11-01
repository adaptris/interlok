package com.adaptris.tester.runtime.clients;


import com.adaptris.tester.runtime.ServiceTestException;
import com.adaptris.tester.runtime.messages.TestMessage;

import java.io.Closeable;

public interface TestClient extends Closeable {

  public void init() throws ServiceTestException;
  public TestMessage applyService(String xml, TestMessage message) throws Exception;

}
