package com.adaptris.core.runtime;


public interface ParentRuntimeInfoComponent extends ParentRuntimeInfoComponentMBean {

  boolean addChildJmxComponent(ChildRuntimeInfoComponent comp);

  boolean removeChildJmxComponent(ChildRuntimeInfoComponent comp);
}
