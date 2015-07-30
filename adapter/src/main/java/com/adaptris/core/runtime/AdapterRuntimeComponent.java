package com.adaptris.core.runtime;

import java.util.Collection;

import com.adaptris.core.StateManagedComponent;


/**
 * Base interface that exposes management functionality for an Adapter component.
 *
 * @author lchan
 */
public interface AdapterRuntimeComponent<S extends StateManagedComponent> extends AdapterComponentMBean {

  /**
   * Get the component that this implementation manages.
   *
   * @return my component.
   */
  S getWrappedComponent();

  /**
   * Get all the descendants of this ParentComponent implementation.
   *
   * @return all the children, and children's children, etc...
   */
  Collection<BaseComponentMBean> getAllDescendants();
}
