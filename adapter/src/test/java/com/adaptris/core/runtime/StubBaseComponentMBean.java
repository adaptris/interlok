package com.adaptris.core.runtime;

import com.adaptris.core.CoreException;
import com.adaptris.core.util.JmxHelper;

public abstract class StubBaseComponentMBean implements BaseComponentMBean {

  @Override
  public void registerMBean() throws CoreException {
    try {
      JmxHelper.register(createObjectName(), this);
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }

  @Override
  public void unregisterMBean() throws CoreException {
    try {
      JmxHelper.unregister(createObjectName());
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }
}
