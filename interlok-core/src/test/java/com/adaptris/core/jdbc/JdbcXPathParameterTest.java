/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
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

  @Before
  public void setUp() throws Exception {
    message = DefaultMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
  }

  @Override
  protected JdbcXPathParameter createParameter() {
    return new JdbcXPathParameter();
  }

  @Test
  public void testNoXPathInputParam() throws Exception {
    JdbcXPathParameter parameter = new JdbcXPathParameter();
    
    try {
      parameter.applyInputParam(message);
      fail();
    } catch (JdbcParameterException ex) {
      // expected, pass.
    }
  }

  @Test
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

  @Test
  public void testNoXPathOutputParam() throws Exception {
    JdbcXPathParameter parameter = new JdbcXPathParameter();
    
    try {
      parameter.applyOutputParam(null, message);
      fail();
    } catch(JdbcParameterException ex) {
      // expected, pass.
    }
  }

  @Test
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

  @Test
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

  @Test
  public void testNoNodeXPathInputParameter() throws Exception {
    JdbcXPathParameter parameter = new JdbcXPathParameter();
    parameter.setXpath("/head/body/element999");
    assertEquals("", parameter.applyInputParam(message));
  }

  @Test
  public void testNoNodeXPathOutputParameter() throws Exception {
    assertEquals(message.getContent(), XML_PAYLOAD);
    
    JdbcXPathParameter parameter = new JdbcXPathParameter();
    parameter.setXpath("/head/body/element999");
    
    try {
      parameter.applyOutputParam("BLAH", message);
      fail();
    } catch(JdbcParameterException ex) {
      // expected
    }
  }

  @Test
  public void testXPathInputParameter() throws Exception {
    JdbcXPathParameter parameter = new JdbcXPathParameter();
    parameter.setXpath("/head/body/element1");
    
    assertEquals(parameter.applyInputParam(message), ELEMENT_ONE_VALUE);
  }

  @Test
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
