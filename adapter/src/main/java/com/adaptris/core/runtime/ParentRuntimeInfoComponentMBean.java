package com.adaptris.core.runtime;

import java.util.Collection;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public interface ParentRuntimeInfoComponentMBean extends RuntimeInfoComponent {

  Collection<ObjectName> getChildRuntimeInfoComponents() throws MalformedObjectNameException;

}
