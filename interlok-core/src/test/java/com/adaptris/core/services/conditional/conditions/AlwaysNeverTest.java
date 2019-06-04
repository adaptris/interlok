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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessageFactory;

public class AlwaysNeverTest {
  
  @Test
  public void testAlways() throws Exception {
    ConditionAlways condition = new ConditionAlways();
    assertTrue(condition.evaluate(AdaptrisMessageFactory.getDefaultInstance().newMessage()));
  }

  @Test
  public void testNever() throws Exception {
    ConditionNever condition = new ConditionNever();
    assertFalse(condition.evaluate(AdaptrisMessageFactory.getDefaultInstance().newMessage()));
  }
}
