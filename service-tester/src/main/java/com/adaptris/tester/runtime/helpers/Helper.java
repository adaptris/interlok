package com.adaptris.tester.runtime.helpers;


import com.adaptris.tester.runtime.ServiceTestException;
import com.adaptris.tester.runtime.messages.TestMessage;

import java.io.Closeable;
import java.util.Map;

public interface Helper extends Closeable {

  void init() throws ServiceTestException;

  Map<String, String> getHelperProperties();
}
