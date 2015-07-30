package com.adaptris.core.runtime;


public interface HierarchicalMBean {

  /**
   * Create an ObjectName key/value pair hierarchy.
   *
   * @return a hierarchy based on the tree.
   */
  String createObjectHierarchyString();
}
