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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MetadataElement;

@SuppressWarnings("deprecation")
public class JdbcMetadataParameterTest extends NullableParameterCase {
  
  private static final String METADATA_VALUE = "PARAM_METADATA_VALUE";
  private static final String METADATA_KEY = "PARAM_METADATA_KEY";
  private AdaptrisMessage message;
  private MetadataElement metadata;
  
  @Before
  public void setUp() throws Exception {
    metadata = new MetadataElement(METADATA_KEY, METADATA_VALUE);
    
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
    message.addMetadata(metadata);
  }
  
  @Override
  protected JdbcMetadataParameter createParameter() {
    return new JdbcMetadataParameter();
  }

  @Test
  public void testNoMetadataKeyForInputParam() throws Exception {
    message.removeMetadata(metadata);
    
    JdbcMetadataParameter param = new JdbcMetadataParameter();
    try {
      param.applyInputParam(message);
    } catch(JdbcParameterException ex) {
      // expected, pass
    }
  }

  @Test
  public void testNoMetadataKeyForOutputParam() throws Exception {
    message.removeMetadata(metadata);
    
    JdbcMetadataParameter param = new JdbcMetadataParameter();
    try {
      param.applyOutputParam(null, message);
    } catch(JdbcParameterException ex) {
      // expected, pass
    }
  }

  @Test
  public void testMetadataDoesNotExistInputParam() throws Exception {
    JdbcMetadataParameter param = new JdbcMetadataParameter();
    param.setMetadataKey("DOES_NOT_EXIST_KEY");
    try {
      param.applyInputParam(message);
    } catch(JdbcParameterException ex) {
      // expected, pass
    }
  }

  @Test
  public void testMetadataDoesNotExistOutputParam() throws Exception {
    JdbcMetadataParameter param = new JdbcMetadataParameter();
    param.setMetadataKey("DOES_NOT_EXIST_KEY");
    param.applyOutputParam("BLAH", message);
    // expected, pass
  }

  @Test
  public void testMetadataAlreadyExistsOutputParam() throws Exception {
    assertEquals(message.getMetadataValue(METADATA_KEY), METADATA_VALUE);
    
    JdbcMetadataParameter param = new JdbcMetadataParameter();
    param.setMetadataKey(METADATA_KEY);
    
    param.applyOutputParam("NEW_PARAM_METADATA_VALUE", message);
    
    assertEquals(message.getMetadataValue(METADATA_KEY), "NEW_PARAM_METADATA_VALUE");
  }

  @Test
  public void testMetadataAppliedInputParam() throws Exception {
    JdbcMetadataParameter param = new JdbcMetadataParameter();
    param.setMetadataKey(METADATA_KEY);
    Object appliedInputParam = param.applyInputParam(message);
    
    assertEquals(appliedInputParam, METADATA_VALUE);
  }

  @Test
  public void testMetadataAppliedOutputParam() throws Exception {
    message.removeMetadata(metadata);
    assertFalse(message.containsKey(METADATA_KEY));
    
    JdbcMetadataParameter param = new JdbcMetadataParameter();
    param.setMetadataKey(METADATA_KEY);
    
    param.applyOutputParam(METADATA_VALUE, message);
    
    assertTrue(message.containsKey(METADATA_KEY));
    assertEquals(message.getMetadataValue(METADATA_KEY), METADATA_VALUE);
  }
}
