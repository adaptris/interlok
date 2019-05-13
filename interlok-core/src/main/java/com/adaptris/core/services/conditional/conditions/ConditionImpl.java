package com.adaptris.core.services.conditional.conditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.conditional.Condition;

public abstract class ConditionImpl implements Condition {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @Override
  public void init() throws CoreException {
    // Override as required.
  }

  @Override
  public void start() throws CoreException {
    // Override as required.
  }

  @Override
  public void stop() {
    // Override as required.
  }

  @Override
  public void close() {
    // Override as required.
  }

}
