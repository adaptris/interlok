/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.http.client.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.adaptris.core.http.client.ExactMatch;
import com.adaptris.core.http.client.RangeMatch;

public class StatusMatchTest {

  @Test
  public void testExactMatch() throws Exception {
    ExactMatch match = new ExactMatch(200, "200 OK");
    assertEquals(200, match.getStatusCode());
    assertEquals("200 OK", match.getServiceId());
    assertEquals("200 OK", match.serviceId());
    assertTrue(match.matches(200));
    assertFalse(match.matches(201));
  }

  @Test
  public void testRangeMatch() throws Exception {
    RangeMatch match = new RangeMatch(200, 299, "200 OK");
    assertEquals(200, match.getLower());
    assertEquals(299, match.getUpper());
    assertEquals("200 OK", match.getServiceId());
    assertEquals("200 OK", match.serviceId());
    assertTrue(match.matches(200));
    assertTrue(match.matches(201));
    assertFalse(match.matches(400));
    assertFalse(match.matches(300));
  }
}
