/*
 * Copyright 2019 Adaptris Ltd.
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
package com.adaptris.core.services.conditional;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.ServiceException;

public class MaxLoopBehaviourTest {

  @Test
  public void testOnMaxNoOp() throws Exception {
    OnMaxNoOp behaviour = new OnMaxNoOp();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    behaviour.onMax(msg);
  }

  @Test(expected = ServiceException.class)
  public void testOnMaxThrowException() throws Exception {
    OnMaxThrowException behaviour = new OnMaxThrowException();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    behaviour.onMax(msg);
  }

  @Test
  public void testOnMaxStopProcessing() throws Exception {
    OnMaxStopProcessing behaviour = new OnMaxStopProcessing();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    behaviour.onMax(msg);
    assertEquals(CoreConstants.STOP_PROCESSING_VALUE, msg.getMetadataValue(CoreConstants.STOP_PROCESSING_KEY));
    assertEquals(CoreConstants.STOP_PROCESSING_VALUE, msg.getMetadataValue(CoreConstants.KEY_WORKFLOW_SKIP_PRODUCER));
  }
}
