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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;

public class CompareTimestampsTest {

  private CompareTimestamps operator;

  private AdaptrisMessage message;

  @BeforeEach
  public void setUp() throws Exception {
    operator = new CompareTimestamps();
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
  }

  @Test
  public void testTrue() {
    operator.setValue("2020-12-03T10:11:29+0000");

    assertTrue(operator.apply(message, "2020-12-03T11:11:29+0100"));
  }

  @Test
  public void testFalse() {
    operator.setValue("metadata-equals-test");

    assertFalse(operator.apply(message, "2020-12-03T11:11:29+0000"));
  }
}
