package com.adaptris.core.jms;

import com.adaptris.core.CoreException;

public class MockNoOpConnectionErrorHandler extends JmsConnectionErrorHandlerImpl {

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
  }

}
