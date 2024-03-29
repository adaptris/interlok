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

package com.adaptris.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class GuidGeneratorTest {

  @Test
  public void testGetUUID() throws Exception {
    GuidGenerator guid = new GuidGenerator();
    assertNotNull(guid.getUUID());
  }

  @Test
  public void testCreateId() throws Exception {
    GuidGenerator guid = new GuidGenerator();
    assertNotNull(guid.create(new Object()));
  }

  @Test
  public void testCreateIdWithNull() throws Exception {
    GuidGenerator guid = new GuidGenerator();
    assertNotNull(guid.create(null));
  }

}
