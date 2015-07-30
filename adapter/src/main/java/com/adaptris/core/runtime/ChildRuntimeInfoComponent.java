package com.adaptris.core.runtime;


public interface ChildRuntimeInfoComponent extends ChildRuntimeInfoComponentMBean {

  RuntimeInfoComponent getParentRuntimeInfoComponent();

}
