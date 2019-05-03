/*
    Copyright Adaptris

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.adaptris.core.services.conditional.conditions;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.conditional.conditions.ConditionExpression;
import com.adaptris.core.util.LifecycleHelper;

public class ConditionExpressionTest {

  private ConditionExpression condition;
  private AdaptrisMessage message;
  
  @Before
  public void setUp() throws Exception {
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
    condition = new ConditionExpression();
    LifecycleHelper.initAndStart(condition);
  }

  @After
  public void tearDown() throws Exception {
    LifecycleHelper.stopAndClose(condition);
  }

  
  @Test
  public void testMetadataExists() throws Exception {
    condition.setAlgorithm("%message{key1} > %message{key2}");
    message.addMessageHeader("key1", "10");
    message.addMessageHeader("key2", "5");
    
    assertTrue(condition.evaluate(message));
  }
  
  @Test
  public void testMetadataDoesNotExist() throws Exception {
    condition.setAlgorithm("%message{key1} == 10");
    try {
      condition.evaluate(message);
      fail("Metadata does not exist, expect an exception");
    } catch (ServiceException ex) {
      // expected.
    }
  }
  
}
