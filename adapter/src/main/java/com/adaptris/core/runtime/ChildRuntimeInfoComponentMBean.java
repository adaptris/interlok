package com.adaptris.core.runtime;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public interface ChildRuntimeInfoComponentMBean extends BaseComponentMBean, RuntimeInfoComponent {

  /**
   * Get the parents ObjectName representation.
   *
   * @return the Objectname that represents the parent management bean.
   * @throws MalformedObjectNameException
   */
  ObjectName getParentObjectName() throws MalformedObjectNameException;

  /**
   * Get the parent's uniqueid.
   *
   * @return the uniqueid of the parent (e.g. a message-digester will return the adapter-id, and interceptors the workflow-id).
   */
  String getParentId();
}
