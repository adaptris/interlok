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

package com.adaptris.core.services.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class RegexpValueMatcherTest {

  @BeforeEach
  public void setUp() throws Exception {

  }

  @AfterEach
  public void tearDown() throws Exception {

  }

  @Test
  public void testMatch() throws Exception {
    RegexpValueMatcher matcher = new RegexpValueMatcher();
    assertEquals("FromApple", matcher.getNextServiceId("steve.jobs@apple.com", create()));
  }

  @Test
  public void testNoMatch() throws Exception {
    RegexpValueMatcher matcher = new RegexpValueMatcher();
    assertNull(matcher.getNextServiceId("the.queen@buckinghampalace.gov.uk", create()));
  }

  private static KeyValuePairSet create() {
    KeyValuePairSet result = new KeyValuePairSet();
    result.addKeyValuePair(new KeyValuePair("^.*google.com$", "FromGoogle"));
    result.addKeyValuePair(new KeyValuePair("^.*microsoft.com$", "FromMicrosoft"));
    result.addKeyValuePair(new KeyValuePair("^.*apple.com$", "FromApple"));
    return result;
  }
}
