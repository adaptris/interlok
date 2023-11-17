package com.adaptris.core.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class InputFieldExpressionTest {


  @Test
  public void testIsExpression() throws Exception {
    assertTrue(InputFieldExpression.isExpression("%message{myKey}"));
    assertTrue(InputFieldExpression.isExpression("%sysprop{MY_SYS_PROP}"));
    assertTrue(InputFieldExpression.isExpression("%env{MY_ENV_VAR}"));
    assertTrue(InputFieldExpression.isExpression("%payload"));
    assertTrue(InputFieldExpression.isExpression("%payload{id:1}"));
    assertTrue(InputFieldExpression.isExpression("XXX_%payload{xpath:/Document/Header}"));
    assertFalse(InputFieldExpression.isExpression("hello world"));
  }


}
