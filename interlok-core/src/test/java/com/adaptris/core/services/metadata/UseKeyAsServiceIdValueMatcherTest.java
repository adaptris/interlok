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

import org.junit.jupiter.api.Test;

public class UseKeyAsServiceIdValueMatcherTest {


  @Test
  public void testMatcher() throws Exception {
    UseKeyAsServiceIdValueMatcher matcher = new UseKeyAsServiceIdValueMatcher();
    assertEquals("key", matcher.getNextServiceId("key", null));
  }

  @Test
  public void testMatcherReturnsNull() throws Exception {
    UseKeyAsServiceIdValueMatcher matcher = new UseKeyAsServiceIdValueMatcher();
    assertNull(matcher.getNextServiceId(null, null));
  }
}
