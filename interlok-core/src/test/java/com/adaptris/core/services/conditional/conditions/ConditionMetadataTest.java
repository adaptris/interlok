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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.services.conditional.operator.IsNull;
import com.adaptris.core.services.conditional.operator.NotNull;
import com.adaptris.core.services.metadata.compare.EndsWith;
import com.adaptris.core.util.LifecycleHelper;

public class ConditionMetadataTest {

  private ConditionMetadata condition;
  private AdaptrisMessage message;

  @BeforeEach
  public void setUp() throws Exception {
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
    condition = new ConditionMetadata();
    condition.setAdditionalLogging(true);
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
  public void testMetadataExists() throws Exception {
    condition.setMetadataKey("key1");
    condition.setOperator(new NotNull());
    message.addMessageHeader("key1", "value1");

    assertTrue(condition.evaluate(message));
  }

  @Test
  public void testMetadataDoesNotExist() throws Exception {
    condition.setMetadataKey("key1");
    condition.setOperator(new IsNull());

    assertTrue(condition.evaluate(message));
  }

  @Test
  public void testMetadataNotSet() throws Exception {
    assertFalse(condition.evaluate(message));
  }

  @Test
  public void testMetadataEndsWith() throws Exception {
    condition.setMetadataKey("key1");
    EndsWith endsWith = new EndsWith();
    endsWith.setValue("value");
    condition.setOperator(endsWith);
    message.addMessageHeader("key1", "some-value");

    assertTrue(condition.evaluate(message));
  }
}
