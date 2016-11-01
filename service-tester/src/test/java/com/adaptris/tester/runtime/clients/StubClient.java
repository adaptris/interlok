package com.adaptris.tester.runtime.clients;

import com.adaptris.tester.runtime.ServiceTestException;
import com.adaptris.tester.runtime.messages.TestMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.IOException;

@XStreamAlias("stub-test-client")
public class StubClient implements TestClient {
  @Override
  public void init() throws ServiceTestException {

  }

  @Override
  public TestMessage applyService(String xml, TestMessage message) throws ServiceTestException {
    return message;
  }

  @Override
  public void close() throws IOException {

  }
}
