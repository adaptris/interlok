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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;

import junit.framework.TestCase;

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
