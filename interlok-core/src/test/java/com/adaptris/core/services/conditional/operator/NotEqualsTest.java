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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;

public class NotEqualsTest {

  private NotEquals operator;
  
  private AdaptrisMessage message;
  
  @BeforeEach
  public void setUp() throws Exception {
    operator = new NotEquals();
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
  }
  
  @Test
  public void testStringEquals() {
    String object = new String("test");
    operator.setValue(object);
    
    assertFalse(operator.apply(message, "test"));
  }
  
  @Test
  public void testStringNotEquals() {
    String object = new String("test");
    operator.setValue(object);
    
    assertTrue(operator.apply(message, "xxxx"));
  }
  
  @Test
  public void testToString() {
    assertTrue(operator.toString().contains("not equals"));
  }
}
