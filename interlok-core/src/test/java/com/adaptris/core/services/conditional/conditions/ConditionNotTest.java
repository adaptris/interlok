package com.adaptris.core.services.conditional.conditions;

import com.adaptris.core.AdaptrisMessageFactory;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
