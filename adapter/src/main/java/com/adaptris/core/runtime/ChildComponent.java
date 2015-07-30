package com.adaptris.core.runtime;




/**
 * Basic interface of MBeans that contain child member components.
 *
 * @author lchan
 */
public interface ChildComponent<S extends AdapterRuntimeComponent> extends ChildComponentMBean {

  /**
   * Get our direct parent component.
   *
   * @return our parent.
   */
  S getParent();

}
