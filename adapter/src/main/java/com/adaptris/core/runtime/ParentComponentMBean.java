package com.adaptris.core.runtime;

import java.util.Collection;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Basic interface of MBeans that contain child member components.
 *
 * @author lchan
 */
public interface ParentComponentMBean extends BaseComponentMBean {

  /**
   * Get the list of {@link ObjectName} instances that map to all the immeidate child components of this parent.
   *
   * @return a list of {@link ObjectName} instances.
   */
  Collection<ObjectName> getChildren() throws MalformedObjectNameException;

}
