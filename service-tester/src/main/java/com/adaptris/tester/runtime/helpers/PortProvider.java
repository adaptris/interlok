package com.adaptris.tester.runtime.helpers;

public interface PortProvider {

  void initPort();

  int getPort();

  void releasePort();
}
