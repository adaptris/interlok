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

package com.adaptris.core.services.conditional.operator;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.services.conditional.Operator;
import com.adaptris.core.services.conditional.operator.IsNull;

public class IsNullTest {
  
  private Operator operator;
  
  private AdaptrisMessage message;
  
  @Before
  public void setUp() throws Exception {
    operator = new IsNull();
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
  }
  
  @Test
  public void testExists() {
    assertFalse(operator.apply(message, "1"));
  }
  
  @Test
  public void testNotExists() {
    assertTrue(operator.apply(message, null));
  }

  @Test
  public void testToString() {
    assertTrue(operator.toString().contains("is null"));
  }
}
