package com.adaptris.core.runtime;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;



/**
 * Basic interface of MBeans that contain child member components.
 *
 * @author lchan
 */
public interface ChildComponentMBean extends AdapterComponentMBean {

  /**
   * Get the parent's uniqueid.
   *
   * @return the uniqueid of the parent (e.g. workflows will return the parent channel-id, and channels the adapter-id).
   */
  String getParentId();

  /**
   * Get the parents ObjectName representation.
   *
   * @return the Objectname that represents the parent management bean.
   * @throws MalformedObjectNameException
   */
  ObjectName getParentObjectName() throws MalformedObjectNameException;
}
