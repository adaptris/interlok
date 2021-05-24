package com.adaptris.core.services.conditional.conditions;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.services.conditional.Condition;

import lombok.Getter;
import lombok.Setter;

public abstract class ConditionImpl implements Condition {
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @Getter
  @Setter
  @InputFieldDefault(value = "false")
  /**
   * Log the details of the conditions source and the result of the evaluation.
   */
  private Boolean additionalLogging;
  
  protected void logCondition(String message, Object... args) {
    if(additionalLogging()) {      
      log.trace(message, args);
    }
  }
  
  private boolean additionalLogging() {
    return BooleanUtils.toBooleanDefaultIfNull(getAdditionalLogging(), false);
  }
}
