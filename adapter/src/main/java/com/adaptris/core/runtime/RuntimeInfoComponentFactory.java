package com.adaptris.core.runtime;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.CoreException;

public abstract class RuntimeInfoComponentFactory {

  protected static final Logger log = LoggerFactory.getLogger(RuntimeInfoComponentFactory.class);

  private static final Collection<RuntimeInfoComponentFactory> factories = new ArrayList<RuntimeInfoComponentFactory>();

  public static final void registerComponentFactory(RuntimeInfoComponentFactory factory) {
    synchronized (factories) {
      factories.add(factory);
    }
  }

  public static final RuntimeInfoComponent create(ParentRuntimeInfoComponent parent, AdaptrisComponent c) throws CoreException {
    RuntimeInfoComponent result = null;
    try {
      for (RuntimeInfoComponentFactory f : factories) {
        if (f.isSupported(c)) {
          result = f.createComponent(parent, c);
          break;
        }
      }
    }
    catch (Exception e) {
    }
    if (result == null) {
      log.trace("No RuntimeInfoComponent for " + c.getClass());
    }
    return result;
  }

  protected abstract boolean isSupported(AdaptrisComponent e);

  protected abstract RuntimeInfoComponent createComponent(ParentRuntimeInfoComponent parent, AdaptrisComponent e) throws Exception;

}
