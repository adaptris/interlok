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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;

public class DynamicBasicAuthorizationHeaderTest {


  @Test
  public void testUsername() throws Exception {
    DynamicBasicAuthorizationHeader auth = new DynamicBasicAuthorizationHeader();
    assertNull(auth.getUsername());
    auth.setUsername("hello");
    assertEquals("hello", auth.getUsername());
  }

  @Test
  public void testPassword() throws Exception {
    DynamicBasicAuthorizationHeader auth = new DynamicBasicAuthorizationHeader();
    assertNull(auth.getPassword());
    auth.setPassword("hello");
    assertEquals("hello", auth.getPassword());
  }

  @Test
  public void testDoAuth_NoUserPassword() throws Exception {
    DynamicBasicAuthorizationHeader auth = new DynamicBasicAuthorizationHeader();
    try {
      auth.setup("http://localhost:8080", AdaptrisMessageFactory.getDefaultInstance().newMessage(), null);
      fail();
    } catch (CoreException expected) {

    }
  }
}
