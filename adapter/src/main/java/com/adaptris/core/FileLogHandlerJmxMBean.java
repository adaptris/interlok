package com.adaptris.core;

import java.io.IOException;

import com.adaptris.core.runtime.ChildRuntimeInfoComponentMBean;

public interface FileLogHandlerJmxMBean extends ChildRuntimeInfoComponentMBean {

  void cleanupLogfiles() throws IOException;

}
