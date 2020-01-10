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
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;

public class JdbcObjectMetadataParameterTest extends NullableParameterCase {
  
  private static final Integer METADATA_VALUE = new Integer(999);
  private static final String METADATA_KEY = "PARAM_METADATA_KEY";
  private AdaptrisMessage message;
  
  @Before
  public void setUp() throws Exception {    
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
  }
  
  @Override
  protected JdbcObjectMetadataParameter createParameter() {
    return new JdbcObjectMetadataParameter();
  }

  @Test
  public void testNoMetadataKeyForInputParam() throws Exception {    
    JdbcObjectMetadataParameter param = new JdbcObjectMetadataParameter();
    try {
      param.applyInputParam(message);
    } catch(JdbcParameterException ex) {
      // expected, pass
    }
  }

  @Test
  public void testNoMetadataKeyForOutputParam() throws Exception {    
    JdbcObjectMetadataParameter param = new JdbcObjectMetadataParameter();
    try {
      param.applyOutputParam(null, message);
    } catch(JdbcParameterException ex) {
      // expected, pass
    }
  }

  @Test
  public void testMetadataDoesNotExistInputParam() throws Exception {
    JdbcObjectMetadataParameter param = new JdbcObjectMetadataParameter();
    param.setMetadataKey("DOES_NOT_EXIST_KEY");
    try {
      param.applyInputParam(message);
    } catch(JdbcParameterException ex) {
      // expected, pass
    }
  }

  @Test
  public void testMetadataDoesNotExistOutputParam() throws Exception {
    JdbcObjectMetadataParameter param = new JdbcObjectMetadataParameter();
    param.setMetadataKey("DOES_NOT_EXIST_KEY");
    param.applyOutputParam(null, message);
    // expected, pass
  }

  @Test
  public void testMetadataAlreadyExistsOutputParam() throws Exception {
    message.addObjectHeader(METADATA_KEY, METADATA_VALUE);
    assertEquals(message.getObjectHeaders().get(METADATA_KEY), METADATA_VALUE);
    
    JdbcObjectMetadataParameter param = new JdbcObjectMetadataParameter();
    param.setMetadataKey(METADATA_KEY);
    
    param.applyOutputParam("NEW_PARAM_METADATA_VALUE", message);
    
    assertEquals(message.getObjectHeaders().get(METADATA_KEY), "NEW_PARAM_METADATA_VALUE");
  }

  @Test
  public void testMetadataAppliedInputParam() throws Exception {
    message.addObjectHeader(METADATA_KEY, METADATA_VALUE);
    
    JdbcObjectMetadataParameter param = new JdbcObjectMetadataParameter();
    param.setMetadataKey(METADATA_KEY);
    Object appliedInputParam = param.applyInputParam(message);
    
    assertEquals(appliedInputParam, METADATA_VALUE);
  }

  @Test
  public void testMetadataAppliedOutputParam() throws Exception {    
    JdbcObjectMetadataParameter param = new JdbcObjectMetadataParameter();
    param.setMetadataKey(METADATA_KEY);
    
    param.applyOutputParam(METADATA_VALUE, message);
    
    assertTrue(message.getObjectHeaders().containsKey(METADATA_KEY));
    assertEquals(message.getObjectHeaders().get(METADATA_KEY), METADATA_VALUE);
  }

}
