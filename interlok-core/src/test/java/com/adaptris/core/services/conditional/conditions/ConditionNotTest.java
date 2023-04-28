package com.adaptris.core.services.conditional.conditions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessageFactory;

public class ConditionNotTest {
  @Test
  public void testWrappedTrue() throws Exception {
    ConditionNot condition = new ConditionNot();
    condition.setCondition(new CaseDefault());
    assertFalse(condition.evaluate(AdaptrisMessageFactory.getDefaultInstance().newMessage()));
  }

  @Test
  public void testWrappedFalse() throws Exception {
    ConditionNot condition = new ConditionNot();
    condition.setCondition(new ConditionNever());
    assertTrue(condition.evaluate(AdaptrisMessageFactory.getDefaultInstance().newMessage()));
  }
}
