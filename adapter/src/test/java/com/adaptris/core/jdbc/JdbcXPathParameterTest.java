package com.adaptris.core.jdbc;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class JdbcXPathParameterTest extends NullableParameterCase {
  
  private AdaptrisMessage message;
  
  private static final String ELEMENT_ONE_VALUE = "Element1Value";
  private static final String ELEMENT_TWO_VALUE = "Element2Value";
  private static final String ELEMENT_99_VALUE = "99";
  
  private static final String XML_PAYLOAD = 
        "<head>" +
  		"<body>" +
  		"<element1>" + ELEMENT_ONE_VALUE + "</element1>" + 
  		"<element2>" + ELEMENT_TWO_VALUE + "</element2>" +
  		"</body>" +
  		"</head>";
    
  public void setUp() throws Exception {
    message = DefaultMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
  }

  @Override
  protected JdbcXPathParameter createParameter() {
    return new JdbcXPathParameter();
  }

  public void testNoXPathInputParam() throws Exception {
    JdbcXPathParameter parameter = new JdbcXPathParameter();
    
    try {
      parameter.applyInputParam(message);
      fail();
    } catch (JdbcParameterException ex) {
      // expected, pass.
    }
  }
  
  public void testSetNamespaceContext() {
    JdbcXPathParameter parameter = new JdbcXPathParameter();
    assertNull(parameter.getNamespaceContext());
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair("hello", "world"));
    parameter.setNamespaceContext(kvps);
    assertEquals(kvps, parameter.getNamespaceContext());
    parameter.setNamespaceContext(null);
    assertNull(parameter.getNamespaceContext());
  }

  public void testNoXPathOutputParam() throws Exception {
    JdbcXPathParameter parameter = new JdbcXPathParameter();
    
    try {
      parameter.applyOutputParam(null, message);
      fail();
    } catch(JdbcParameterException ex) {
      // expected, pass.
    }
  }
  
  public void testIllegalXPathInputParam() throws Exception {
    JdbcXPathParameter parameter = new JdbcXPathParameter();
    parameter.setXpath("/##~[]{}");
    
    try {
      parameter.applyInputParam(message);
      fail();
    } catch(JdbcParameterException ex) {
      // expected, pass.
    }
  }
  
  public void testIllegalXPathOutputParam() throws Exception {
    JdbcXPathParameter parameter = new JdbcXPathParameter();
    parameter.setXpath("/##~[]{}");
    
    try {
      parameter.applyOutputParam(null, message);
      fail();
    } catch(JdbcParameterException ex) {
      // expected, pass.
    }
  }
  
  public void testNoNodeXPathInputParameter() throws Exception {
    JdbcXPathParameter parameter = new JdbcXPathParameter();
    parameter.setXpath("/head/body/element999");
    assertEquals("", parameter.applyInputParam(message));
  }
  
  public void testNoNodeXPathOutputParameter() throws Exception {
    assertEquals(message.getStringPayload(), XML_PAYLOAD);
    
    JdbcXPathParameter parameter = new JdbcXPathParameter();
    parameter.setXpath("/head/body/element999");
    
    try {
      parameter.applyOutputParam("BLAH", message);
      fail();
    } catch(JdbcParameterException ex) {
      // expected
    }
  }
  
  public void testXPathInputParameter() throws Exception {
    JdbcXPathParameter parameter = new JdbcXPathParameter();
    parameter.setXpath("/head/body/element1");
    
    assertEquals(parameter.applyInputParam(message), ELEMENT_ONE_VALUE);
  }
  
  public void testXPathOutputParameter() throws Exception {
    JdbcXPathParameter parameter = new JdbcXPathParameter();
    parameter.setXpath("/head/body/element1/");
    
    try {
      parameter.applyOutputParam(ELEMENT_99_VALUE, message);
      fail();
    } catch(JdbcParameterException ex) {
      // expected
    }  
  }

}
