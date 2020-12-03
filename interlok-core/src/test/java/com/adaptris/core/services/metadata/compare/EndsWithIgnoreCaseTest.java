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

package com.adaptris.core.services.metadata.compare;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EndsWithIgnoreCaseTest {

  private EndsWithIgnoreCase operator;

  private AdaptrisMessage message;

  @Before
  public void setUp() throws Exception {
    operator = new EndsWithIgnoreCase();
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
  }

  @Test
  public void testTrue() {
    operator.setValue("metadata-starts-with-test");

    assertTrue(operator.apply(message, "TEST"));
  }

  @Test
  public void testFalse() {
    operator.setValue("metadata-starts-with-test");

    assertFalse(operator.apply(message, "xxxx"));
  }
}
