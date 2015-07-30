package com.adaptris.core;

import static com.adaptris.core.runtime.AdapterComponentMBean.ID_PREFIX;
import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_LOG_HANDLER_TYPE;

import java.io.IOException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.adaptris.core.runtime.AdapterManager;
import com.adaptris.core.runtime.ChildRuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponent;
import com.adaptris.core.util.JmxHelper;


/**
 * Wraps functionality exposing {@link FileLogHandler} to JMX.
 *
 * @author lchan
 *
 */
public class FileLogHandlerJmx implements FileLogHandlerJmxMBean, ChildRuntimeInfoComponent {

  private transient AdapterManager parent;
  private transient FileLogHandler wrappedComponent;
  private transient ObjectName myObjectName = null;

  FileLogHandlerJmx(AdapterManager p, FileLogHandler e) throws MalformedObjectNameException {
    parent = p;
    wrappedComponent = e;
    initMembers();
  }

  private void initMembers() throws MalformedObjectNameException {
    // Builds up a name com.adaptris:type=LogHandler,adapter=<adapter-id>
    // There can be only one LogHandler per adapter.
    myObjectName = ObjectName.getInstance(JMX_LOG_HANDLER_TYPE + parent.createObjectHierarchyString() + ID_PREFIX
        + wrappedComponent.getClass().getSimpleName());
  }

  @Override
  public ObjectName getParentObjectName() throws MalformedObjectNameException {
    return parent.createObjectName();
  }

  @Override
  public String getParentId() {
    return parent.getUniqueId();
  }

  @Override
  public ObjectName createObjectName() throws MalformedObjectNameException {
    return myObjectName;
  }

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

  @Override
  public RuntimeInfoComponent getParentRuntimeInfoComponent() {
    return parent;
  }

  @Override
  public void cleanupLogfiles() throws IOException {
    wrappedComponent.clean();
  }
}
