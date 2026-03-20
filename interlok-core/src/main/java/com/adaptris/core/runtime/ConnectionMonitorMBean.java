package com.adaptris.core.runtime;

import com.adaptris.core.ComponentState;

/**
 * Read-only runtime information for a connection.
 */
public interface ConnectionMonitorMBean extends ChildRuntimeInfoComponentMBean {

  String getUniqueId();

  ComponentState getComponentState();
}
