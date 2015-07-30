package com.adaptris.core.runtime;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.adaptris.core.CoreException;

/**
 * Basic JMX component mbean interface.
 *
 *
 */
public interface BaseComponentMBean {
  /**
   * Create the object name representation of the mbean.
   *
   * @return the object name.
   * @throws MalformedObjectNameException on exception.
   */
  ObjectName createObjectName() throws MalformedObjectNameException;

  /**
   * Register this component (and all children) against the default Platform MBeanServer.
   *
   * @throws CoreException wrapping any exception
   *
   * @see java.lang.management.ManagementFactory#getPlatformMBeanServer()
   */
  void registerMBean() throws CoreException;

  /**
   * Unregister this component (and all children) from the default Platform MBeanServer.
   *
   * @throws CoreException wrapping any exception
   *
   * @see java.lang.management.ManagementFactory#getPlatformMBeanServer()
   */
  void unregisterMBean() throws CoreException;
}
