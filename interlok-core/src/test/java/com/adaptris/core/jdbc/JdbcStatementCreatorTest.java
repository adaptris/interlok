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
import com.adaptris.core.services.jdbc.ConfiguredSQLStatement;
import com.adaptris.core.services.jdbc.MetadataSQLStatement;

import junit.framework.TestCase;

public class JdbcStatementCreatorTest extends TestCase {
  
  private AdaptrisMessage message;
  
  public void setUp() throws Exception {    
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
  }
  
  public void testConfiguredStatementCreator() throws Exception {
    ConfiguredSQLStatement cs = new ConfiguredSQLStatement();
    cs.setStatement("some statement");
    
    assertEquals("some statement", cs.createStatement(message));
  }
  
  public void testMetadataStatementCreator() throws Exception {
    MetadataSQLStatement ms = new MetadataSQLStatement();
    ms.setMetadataKey("someKey");
    
    message.addMetadata("someKey", "some statement");
    
    assertEquals("some statement", ms.createStatement(message));
  }
  
}
