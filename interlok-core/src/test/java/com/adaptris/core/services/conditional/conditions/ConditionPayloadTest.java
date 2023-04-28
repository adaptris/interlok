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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.services.conditional.operator.IsNull;
import com.adaptris.core.services.conditional.operator.NotNull;
import com.adaptris.core.util.LifecycleHelper;

public class ConditionPayloadTest {

  private ConditionPayload condition;
  private AdaptrisMessage message;

  @BeforeEach
  public void setUp() throws Exception {
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
    condition = new ConditionPayload();
    LifecycleHelper.initAndStart(condition);
  }

  @AfterEach
  public void tearDown() throws Exception {
    LifecycleHelper.stopAndClose(condition);
  }

  @Test
  public void testNoOperator() throws Exception {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      condition.operator();
    });
  }

  @Test
  public void testPayloadExists() throws Exception {
    condition.setOperator(new NotNull());
    message.setContent("some content", message.getContentEncoding());

    assertTrue(condition.evaluate(message));
  }

  @Test
  public void testPayloadDoesNotExist() throws Exception {
    condition.setOperator(new IsNull());

    assertTrue(condition.evaluate(message));
  }

}
