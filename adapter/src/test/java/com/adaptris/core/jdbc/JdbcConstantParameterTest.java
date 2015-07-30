package com.adaptris.core.jdbc;

import junit.framework.TestCase;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;

public class JdbcConstantParameterTest extends TestCase {
  
  private static final String CONSTANT_VALUE = "999";
  private AdaptrisMessage message;
  
  public void setUp() throws Exception {    
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
  }
  
  public void testNoConstantSetInputParam() throws Exception {
    JdbcConstantParameter parameter = new JdbcConstantParameter();
    
    try {
      parameter.applyInputParam(message);
    } catch (JdbcParameterException ex) {
      // expected , pass
    }
  }
  
  public void testOutputParam() throws Exception {
    JdbcConstantParameter parameter = new JdbcConstantParameter();
    
    try {
      parameter.applyOutputParam(null, message);
    } catch (JdbcParameterException ex) {
      // expected , pass.  You may not use ConstantParameter with an output param
    }
  }

  public void testConstantInputParam() throws Exception {
    JdbcConstantParameter parameter = new JdbcConstantParameter();
    parameter.setConstant(CONSTANT_VALUE);
    
    assertEquals(parameter.applyInputParam(message), CONSTANT_VALUE);
  }
}
