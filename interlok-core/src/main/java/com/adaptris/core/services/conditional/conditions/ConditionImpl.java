package com.adaptris.core.services.conditional.conditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.services.conditional.Condition;

public abstract class ConditionImpl implements Condition {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

}
