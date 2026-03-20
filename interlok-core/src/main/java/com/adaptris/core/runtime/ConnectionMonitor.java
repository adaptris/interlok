package com.adaptris.core.runtime;

import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_CONNECTION_TYPE;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.ComponentState;

public class ConnectionMonitor extends ChildRuntimeInfoComponentImpl implements ConnectionMonitorMBean {

  private transient RuntimeInfoComponent parent;
  private transient AdaptrisConnection wrappedComponent;
  private transient String objectNameId;

  private ConnectionMonitor() {
    super();
  }

  public ConnectionMonitor(RuntimeInfoComponent owner, AdaptrisConnection component) {
    this();
    parent = owner;
    wrappedComponent = component;
    objectNameId = wrappedComponent.getUniqueId();
  }

  @Override
  protected String getType() {
    return JMX_CONNECTION_TYPE;
  }

  @Override
  protected String uniqueId() {
    return objectNameId;
  }

  @Override
  public RuntimeInfoComponent getParentRuntimeInfoComponent() {
    return parent;
  }

  @Override
  public ComponentState getComponentState() {
    return wrappedComponent.retrieveComponentState();
  }

  @Override
  public String getUniqueId() {
    return wrappedComponent.getUniqueId();
  }

  String connectionId() {
    return wrappedComponent.getUniqueId();
  }

  void appendObjectNameSuffix(String suffix) {
    objectNameId = wrappedComponent.getUniqueId() + suffix;
  }
}
